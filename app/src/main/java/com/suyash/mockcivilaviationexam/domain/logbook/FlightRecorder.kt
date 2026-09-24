package com.suyash.mockcivilaviationexam.domain.logbook

import com.google.gson.Gson
import com.suyash.mockcivilaviationexam.data.cache.LogbookPreferences
import com.suyash.mockcivilaviationexam.data.local.repository.FlightTrackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

enum class FlightPhase { PREFLIGHT, TAXI_OUT, AIRBORNE, TAXI_IN, COMPLETE }

/** Snapshot of the aircraft chosen for the flight, so the log survives a later profile edit. */
data class RecorderAircraft(
    val id: Long?,
    val type: String,
    val model: String,
    val registration: String
)

/**
 * Everything the recorder knows about the flight in progress. Serialised to
 * preferences on every phase change so a killed process picks up where it left
 * off, which matters because a two-hour lesson is longer than Android is
 * willing to keep a background app alive.
 */
data class RecorderState(
    val sessionId: String = "",
    val phase: FlightPhase = FlightPhase.PREFLIGHT,
    val aircraft: RecorderAircraft? = null,
    val departure: String = "",
    val arrival: String = "",
    /** Training area or route description, e.g. "Ngong Hills area". */
    val routeVia: String = "",
    val gpsEnabled: Boolean = false,
    val gpsFixAvailable: Boolean = false,
    val offBlockAt: Long? = null,
    /** First takeoff of the session. */
    val takeoffAt: Long? = null,
    /** Most recent landing of the session. */
    val landingAt: Long? = null,
    val onBlockAt: Long? = null,
    /** Airborne millis from segments already closed by a landing. */
    val airborneMillisClosed: Long = 0,
    /** Start of the current airborne segment, when in the air. */
    val airborneSince: Long? = null,
    val landings: Int = 0,
    val autoDetectedLandings: Int = 0,
    val maxAltitudeFt: Int = 0,
    val maxGroundSpeedKt: Int = 0,
    val distanceNm: Double = 0.0,
    val pointCount: Int = 0,
    val currentAltitudeFt: Int? = null,
    val currentGroundSpeedKt: Int? = null,
    val lastFixAt: Long? = null
) {
    val isActive: Boolean get() = sessionId.isNotEmpty() && phase != FlightPhase.COMPLETE

    fun blockMillis(now: Long): Long {
        val start = offBlockAt ?: return 0
        val end = onBlockAt ?: now
        return (end - start).coerceAtLeast(0)
    }

    fun airMillis(now: Long): Long {
        val open = airborneSince?.let { (now - it).coerceAtLeast(0) } ?: 0
        return airborneMillisClosed + open
    }

    fun groundMillis(now: Long): Long = (blockMillis(now) - airMillis(now)).coerceAtLeast(0)
}

/** What the recorder hands to the entry form when the pilot finishes. */
data class RecordedFlight(
    val sessionId: String,
    val date: LocalDate,
    val aircraft: RecorderAircraft?,
    val departure: String,
    val arrival: String,
    val routeVia: String,
    val offBlock: LocalTime?,
    val takeoff: LocalTime?,
    val landing: LocalTime?,
    val onBlock: LocalTime?,
    val blockHours: Double,
    val airHours: Double,
    val landings: Int,
    val maxAltitudeFt: Int?,
    val maxGroundSpeedKt: Int?,
    val distanceNm: Double?,
    val hasTrack: Boolean
)

/**
 * The in-flight recorder. Four buttons (off blocks, takeoff, landing, on
 * blocks) drive the phase machine by hand; when GPS is on, [onFix] drives it
 * automatically through [FlightPhaseDetector] and records the track. Either
 * way the pilot can override with the buttons.
 */
class FlightRecorder(
    private val prefs: LogbookPreferences,
    private val tracks: FlightTrackRepository,
    private val clock: () -> Long = System::currentTimeMillis
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()

    private val _state = MutableStateFlow(restore())
    val state: StateFlow<RecorderState> = _state.asStateFlow()

    private var detector = newDetector()
    private val stats = TrackStats()
    private val pending = ArrayList<TrackPoint>()
    private var lastPersistAt = 0L

    init {
        // Rebuild the detector's notion of "airborne" from the restored phase.
        detector.forceAirborne(_state.value.phase == FlightPhase.AIRBORNE)
    }

    // ---- Session lifecycle -------------------------------------------------

    fun start(aircraft: RecorderAircraft?, departure: String, arrival: String, gps: Boolean, routeVia: String = "") {
        if (_state.value.isActive) return
        detector = newDetector()
        pending.clear()
        _state.value = RecorderState(
            sessionId = UUID.randomUUID().toString(),
            phase = FlightPhase.PREFLIGHT,
            aircraft = aircraft,
            departure = departure.trim(),
            arrival = arrival.trim(),
            routeVia = routeVia.trim(),
            gpsEnabled = gps
        )
        persist()
    }

    fun setRoute(departure: String, arrival: String, routeVia: String = _state.value.routeVia) {
        _state.update { it.copy(departure = departure.trim(), arrival = arrival.trim(), routeVia = routeVia.trim()) }
        persist()
    }

    fun setGpsEnabled(enabled: Boolean) {
        _state.update { it.copy(gpsEnabled = enabled, gpsFixAvailable = if (enabled) it.gpsFixAvailable else false) }
        persist()
    }

    fun markOffBlock(at: Long = clock()) {
        _state.update {
            if (it.phase != FlightPhase.PREFLIGHT) it
            else it.copy(phase = FlightPhase.TAXI_OUT, offBlockAt = at)
        }
        persist()
    }

    fun markTakeoff(at: Long = clock()) {
        _state.update { s ->
            if (s.phase == FlightPhase.AIRBORNE || s.phase == FlightPhase.COMPLETE) return@update s
            s.copy(
                phase = FlightPhase.AIRBORNE,
                // A pilot who forgot off-blocks still gets a block time.
                offBlockAt = s.offBlockAt ?: at,
                takeoffAt = s.takeoffAt ?: at,
                airborneSince = at
            )
        }
        detector.forceAirborne(true)
        persist()
    }

    fun markLanding(at: Long = clock(), countLanding: Boolean = true, auto: Boolean = false) {
        _state.update { s ->
            if (s.phase != FlightPhase.AIRBORNE) return@update s
            val since = s.airborneSince ?: at
            s.copy(
                phase = FlightPhase.TAXI_IN,
                landingAt = at,
                airborneMillisClosed = s.airborneMillisClosed + (at - since).coerceAtLeast(0),
                airborneSince = null,
                landings = if (countLanding) s.landings + 1 else s.landings,
                autoDetectedLandings = if (auto) s.autoDetectedLandings + 1 else s.autoDetectedLandings
            )
        }
        detector.forceAirborne(false)
        persist()
    }

    fun markOnBlock(at: Long = clock()) {
        _state.update { s ->
            if (s.phase == FlightPhase.COMPLETE || s.phase == FlightPhase.PREFLIGHT) return@update s
            var next = s
            if (next.phase == FlightPhase.AIRBORNE) {
                // On-blocks straight from airborne: close the segment as a landing.
                val since = next.airborneSince ?: at
                next = next.copy(
                    landingAt = at,
                    airborneMillisClosed = next.airborneMillisClosed + (at - since).coerceAtLeast(0),
                    airborneSince = null,
                    landings = next.landings + 1
                )
            }
            next.copy(phase = FlightPhase.COMPLETE, onBlockAt = at)
        }
        detector.forceAirborne(false)
        flushPoints()
        persist()
    }

    /** Touch-and-goes never slow enough to auto-detect; the pilot taps +1. */
    fun adjustLandings(delta: Int) {
        _state.update { it.copy(landings = (it.landings + delta).coerceAtLeast(0)) }
        persist()
    }

    /** Throws the session away, including any recorded track. */
    fun discard() {
        val id = _state.value.sessionId
        pending.clear()
        _state.value = RecorderState()
        prefs.activeSessionJson = null
        if (id.isNotEmpty()) scope.launch { tracks.discardSession(id) }
    }

    /**
     * Snapshot for the entry form. Leaves the state in place until [discard]
     * or [consume] so the pilot can come back if the form is abandoned.
     */
    fun summary(zone: ZoneId = ZoneId.systemDefault()): RecordedFlight {
        val s = _state.value
        val now = clock()
        fun time(ms: Long?): LocalTime? = ms?.let {
            Instant.ofEpochMilli(it).atZone(zone).toLocalTime().withSecond(0).withNano(0)
        }
        val startMs = s.offBlockAt ?: s.takeoffAt ?: now
        return RecordedFlight(
            sessionId = s.sessionId,
            date = Instant.ofEpochMilli(startMs).atZone(zone).toLocalDate(),
            aircraft = s.aircraft,
            departure = s.departure,
            arrival = s.arrival,
            routeVia = s.routeVia,
            offBlock = time(s.offBlockAt),
            takeoff = time(s.takeoffAt),
            landing = time(s.landingAt),
            onBlock = time(s.onBlockAt),
            blockHours = roundHours(s.blockMillis(now)),
            airHours = roundHours(s.airMillis(now)),
            landings = s.landings,
            maxAltitudeFt = s.maxAltitudeFt.takeIf { it > 0 },
            maxGroundSpeedKt = s.maxGroundSpeedKt.takeIf { it > 0 },
            distanceNm = s.distanceNm.takeIf { it > 0.0 }?.let { Math.round(it * 10.0) / 10.0 },
            hasTrack = s.pointCount > 0
        )
    }

    /** The entry was saved as [flightId]: re-home the track and clear the session. */
    suspend fun consume(flightId: Long) {
        val id = _state.value.sessionId
        flushPointsNow()
        if (id.isNotEmpty()) tracks.attachToFlight(id, flightId)
        pending.clear()
        _state.value = RecorderState()
        prefs.activeSessionJson = null
    }

    // ---- GPS ---------------------------------------------------------------

    fun onGpsAvailability(available: Boolean) {
        _state.update { it.copy(gpsFixAvailable = available) }
    }

    fun onFix(point: TrackPoint) {
        val s = _state.value
        if (!s.isActive || !s.gpsEnabled) return

        stats.add(point)
        pending.add(point)

        when (val event = detector.onSample(point.timestamp, point.groundSpeedKt)) {
            is FlightPhaseDetector.Event.Takeoff -> {
                if (s.phase == FlightPhase.PREFLIGHT) markOffBlock(event.at)
                markTakeoff(event.at)
            }
            is FlightPhaseDetector.Event.Landing -> markLanding(event.at, countLanding = true, auto = true)
            null -> Unit
        }

        _state.update {
            it.copy(
                gpsFixAvailable = true,
                maxAltitudeFt = maxOf(it.maxAltitudeFt, point.altitudeFt.toInt()),
                maxGroundSpeedKt = maxOf(it.maxGroundSpeedKt, point.groundSpeedKt.toInt()),
                distanceNm = stats.distanceNm,
                pointCount = it.pointCount + 1,
                currentAltitudeFt = point.altitudeFt.toInt(),
                currentGroundSpeedKt = point.groundSpeedKt.toInt(),
                lastFixAt = point.timestamp
            )
        }

        if (pending.size >= FLUSH_EVERY_POINTS) flushPoints()
        if (point.timestamp - lastPersistAt > PERSIST_EVERY_MS) persist()
    }

    // ---- Internals ---------------------------------------------------------

    private fun newDetector() = FlightPhaseDetector(
        takeoffSpeedKt = prefs.takeoffSpeedKt.toDouble(),
        landingSpeedKt = prefs.landingSpeedKt.toDouble()
    )

    private fun flushPoints() {
        if (pending.isEmpty()) return
        val batch = ArrayList(pending)
        pending.clear()
        val id = _state.value.sessionId
        if (id.isEmpty()) return
        scope.launch { tracks.appendAll(id, batch) }
    }

    private suspend fun flushPointsNow() {
        if (pending.isEmpty()) return
        val batch = ArrayList(pending)
        pending.clear()
        val id = _state.value.sessionId
        if (id.isNotEmpty()) tracks.appendAll(id, batch)
    }

    private fun persist() {
        lastPersistAt = clock()
        val s = _state.value
        prefs.activeSessionJson = if (s.sessionId.isEmpty()) null else gson.toJson(s)
    }

    private fun restore(): RecorderState {
        val json = prefs.activeSessionJson ?: return RecorderState()
        return runCatching { gson.fromJson(json, RecorderState::class.java) }
            .getOrNull()
            ?.takeIf { it.sessionId.isNotEmpty() }
            ?.copy(gpsFixAvailable = false)
            ?: RecorderState()
    }

    private fun roundHours(millis: Long): Double {
        val minutes = Math.round(millis / 60_000.0)
        return Math.round(minutes / 60.0 * 100) / 100.0
    }

    companion object {
        private const val FLUSH_EVERY_POINTS = 20
        private const val PERSIST_EVERY_MS = 30_000L
    }
}
