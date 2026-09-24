package com.suyash.mockcivilaviationexam.data.cache

import android.content.Context
import android.content.SharedPreferences

/**
 * Per-device logbook defaults. A student at an ATO flies the same aircraft for
 * months, so the chosen aircraft, home aerodrome and instructor are remembered
 * until the pilot changes them.
 */
class LogbookPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Row id in the aircraft table, or [NO_AIRCRAFT] when none has been chosen yet. */
    var defaultAircraftId: Long
        get() = prefs.getLong(KEY_DEFAULT_AIRCRAFT, NO_AIRCRAFT)
        set(value) = prefs.edit().putLong(KEY_DEFAULT_AIRCRAFT, value).apply()

    var lastDeparture: String
        get() = prefs.getString(KEY_LAST_DEPARTURE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_DEPARTURE, value.trim()).apply()

    var lastArrival: String
        get() = prefs.getString(KEY_LAST_ARRIVAL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_ARRIVAL, value.trim()).apply()

    var lastInstructorName: String
        get() = prefs.getString(KEY_LAST_INSTRUCTOR_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_INSTRUCTOR_NAME, value.trim()).apply()

    var lastInstructorLicense: String
        get() = prefs.getString(KEY_LAST_INSTRUCTOR_LICENSE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_INSTRUCTOR_LICENSE, value.trim()).apply()

    /** PPL training is VFR; IFR time and instrument approaches stay hidden until wanted. */
    var showIfrFields: Boolean
        get() = prefs.getBoolean(KEY_SHOW_IFR, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_IFR, value).apply()

    /** "DUAL" or "SOLO" — what the last flight was, pre-selected on the next. */
    var lastRole: String
        get() = prefs.getString(KEY_LAST_ROLE, "DUAL") ?: "DUAL"
        set(value) = prefs.edit().putString(KEY_LAST_ROLE, value).apply()

    /** Where the last training area / route line was, pre-filled on the next local flight. */
    var lastRouteVia: String
        get() = prefs.getString(KEY_LAST_ROUTE_VIA, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_ROUTE_VIA, value.trim()).apply()

    /** Whether the recorder should use GPS to detect takeoff and landing. */
    var gpsAutoDetect: Boolean
        get() = prefs.getBoolean(KEY_GPS_AUTO_DETECT, true)
        set(value) = prefs.edit().putBoolean(KEY_GPS_AUTO_DETECT, value).apply()

    /** Groundspeed at which the recorder calls it a takeoff. ForeFlight uses 40 kt. */
    var takeoffSpeedKt: Int
        get() = prefs.getInt(KEY_TAKEOFF_SPEED, DEFAULT_TAKEOFF_SPEED_KT)
        set(value) = prefs.edit().putInt(KEY_TAKEOFF_SPEED, value).apply()

    /** Groundspeed below which, sustained, the recorder calls it a landing. */
    var landingSpeedKt: Int
        get() = prefs.getInt(KEY_LANDING_SPEED, DEFAULT_LANDING_SPEED_KT)
        set(value) = prefs.edit().putInt(KEY_LANDING_SPEED, value).apply()

    /** Serialised in-progress recorder session so a killed process can resume it. */
    var activeSessionJson: String?
        get() = prefs.getString(KEY_ACTIVE_SESSION, null)
        set(value) = prefs.edit().putString(KEY_ACTIVE_SESSION, value).apply()

    companion object {
        const val NO_AIRCRAFT = -1L
        const val DEFAULT_TAKEOFF_SPEED_KT = 40
        const val DEFAULT_LANDING_SPEED_KT = 30

        private const val PREFS_NAME = "logbook_prefs"
        private const val KEY_DEFAULT_AIRCRAFT = "default_aircraft_id"
        private const val KEY_LAST_DEPARTURE = "last_departure"
        private const val KEY_LAST_ARRIVAL = "last_arrival"
        private const val KEY_LAST_INSTRUCTOR_NAME = "last_instructor_name"
        private const val KEY_LAST_INSTRUCTOR_LICENSE = "last_instructor_license"
        private const val KEY_SHOW_IFR = "show_ifr_fields"
        private const val KEY_LAST_ROLE = "last_role"
        private const val KEY_LAST_ROUTE_VIA = "last_route_via"
        private const val KEY_GPS_AUTO_DETECT = "gps_auto_detect"
        private const val KEY_TAKEOFF_SPEED = "takeoff_speed_kt"
        private const val KEY_LANDING_SPEED = "landing_speed_kt"
        private const val KEY_ACTIVE_SESSION = "active_session_json"
    }
}
