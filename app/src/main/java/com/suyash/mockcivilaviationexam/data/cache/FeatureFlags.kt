package com.suyash.mockcivilaviationexam.data.cache

import android.content.Context
import android.content.SharedPreferences

class FeatureFlags(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var useLocalFirstCaching: Boolean
        get() = prefs.getBoolean(KEY_USE_LOCAL_FIRST_CACHING, true)
        set(value) = prefs.edit().putBoolean(KEY_USE_LOCAL_FIRST_CACHING, value).apply()

    var subscriptionEnabled: Boolean
        get() = prefs.getBoolean(KEY_SUBSCRIPTION_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SUBSCRIPTION_ENABLED, value).apply()

    /**
     * Shows the pilot logbook (Fly Now recorder, fleet, PDF export). Built and
     * tested, but HIDDEN in the 12 (1.2.2) release by decision on 2026-09-30;
     * flip to true (and re-add the logbook lines to the store listing) to ship.
     */
    var logbookEnabled: Boolean
        get() = prefs.getBoolean(KEY_LOGBOOK_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_LOGBOOK_ENABLED, value).apply()

    /**
     * GPS track recording and automatic takeoff/landing detection in Fly Now.
     * OFF for the 1.3.0 release: it needs the location foreground-service
     * declaration in the Play Console. The manifest permissions and the
     * <service> entry were removed too; restore both (see the comment in
     * AndroidManifest.xml) and flip this to true to ship it.
     */
    var gpsRecordingEnabled: Boolean
        get() = prefs.getBoolean(KEY_GPS_RECORDING_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_GPS_RECORDING_ENABLED, value).apply()

    companion object {
        private const val KEY_GPS_RECORDING_ENABLED = "gps_recording_enabled"
        private const val KEY_LOGBOOK_ENABLED = "logbook_enabled"
        private const val PREFS_NAME = "feature_flags"
        private const val KEY_USE_LOCAL_FIRST_CACHING = "use_local_first_caching"
        private const val KEY_SUBSCRIPTION_ENABLED = "subscription_enabled"
    }
}
