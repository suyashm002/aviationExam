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

    companion object {
        private const val PREFS_NAME = "feature_flags"
        private const val KEY_USE_LOCAL_FIRST_CACHING = "use_local_first_caching"
        private const val KEY_SUBSCRIPTION_ENABLED = "subscription_enabled"
    }
}
