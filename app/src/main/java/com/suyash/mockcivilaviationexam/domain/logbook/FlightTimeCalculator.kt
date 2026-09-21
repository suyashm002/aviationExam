package com.suyash.mockcivilaviationexam.domain.logbook

import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToLong

/**
 * Clock-time arithmetic for logbook entries.
 *
 * Pilots log two different durations for the same flight:
 *
 *  - **Block time** — off-blocks to on-blocks. Starts when the aircraft first
 *    moves under its own power and ends when it comes to rest at the parking
 *    position. This includes taxi, and it is the figure that counts as flight
 *    time under ICAO Annex 1 and KCAA/EASA licensing rules.
 *  - **Air time** — takeoff to landing. Wheels-up to wheels-down. Used for
 *    aircraft maintenance tracking and airtime-based rental billing.
 *
 * The difference between the two is time spent taxiing on the ground.
 */
object FlightTimeCalculator {

    private val DISPLAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * Parses a clock time typed by a pilot. Accepts "0930", "09:30" and "9:30"
     * because logbooks and ATC strips are conventionally written without a
     * separator. Returns null for anything unparseable so the caller can treat
     * the field as simply not filled in yet.
     */
    fun parse(text: String?): LocalTime? {
        val cleaned = text?.trim()?.replace(":", "") ?: return null
        if (cleaned.isEmpty()) return null
        if (!cleaned.all { it.isDigit() }) return null

        val padded = when (cleaned.length) {
            3 -> "0$cleaned"
            4 -> cleaned
            else -> return null
        }

        val hour = padded.substring(0, 2).toInt()
        val minute = padded.substring(2, 4).toInt()
        if (hour > 23 || minute > 59) return null

        return LocalTime.of(hour, minute)
    }

    fun format(time: LocalTime?): String = time?.format(DISPLAY_FORMAT) ?: ""

    /**
     * Duration between two clock times in decimal hours, rounded to the minute.
     *
     * A flight that lands after midnight has an end time numerically smaller
     * than its start time; that is treated as crossing midnight rather than as
     * a negative duration. Returns 0.0 when either time is missing.
     */
    fun durationHours(start: LocalTime?, end: LocalTime?): Double {
        if (start == null || end == null) return 0.0

        var minutes = Duration.between(start, end).toMinutes()
        if (minutes < 0) minutes += MINUTES_PER_DAY

        // Rounded to 2dp: logbooks record decimal hours to hundredths, and an
        // unrounded 1.4166666666666667 would leak into the entry form and the
        // exported PDF.
        return (minutes / 60.0 * 100).roundToLong() / 100.0
    }

    /** Decimal hours for display, e.g. 1.42 — never a raw repeating float. */
    fun formatDecimal(hours: Double): String =
        if (hours <= 0.0) "-" else "%.2f".format(hours)

    /** Ground (taxi) time: whatever block time was not spent airborne. */
    fun groundTime(blockTime: Double, airTime: Double): Double =
        (blockTime - airTime).coerceAtLeast(0.0)

    /**
     * Renders decimal hours the way a logbook page does — "1:24", not "1.4".
     * Rounds to the nearest minute so display never disagrees with the clock
     * times the pilot entered.
     */
    fun formatHoursMinutes(decimalHours: Double): String {
        if (decimalHours <= 0.0) return "0:00"
        val totalMinutes = (decimalHours * 60).roundToLong()
        return "%d:%02d".format(totalMinutes / 60, totalMinutes % 60)
    }

    private const val MINUTES_PER_DAY = 24L * 60L
}
