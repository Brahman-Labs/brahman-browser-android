package com.brahmanlabs.browser

import android.content.Context
import android.content.SharedPreferences

class BrahmanPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "brahman_prefs", Context.MODE_PRIVATE
    )

    companion object {
        const val KEY_SEARCH_ENGINE = "search_engine"
        const val KEY_HOMEPAGE = "homepage"
        const val KEY_TEXT_SIZE = "text_size"
        const val KEY_JAVASCRIPT = "javascript_enabled"
        const val KEY_NIGHT_MODE = "night_mode"
        const val KEY_BLOCK_AUTOPLAY = "block_autoplay"
        const val KEY_BLOCK_NOTIFICATIONS = "block_notifications"

        const val ENGINE_GOOGLE = "google"
        const val ENGINE_BING = "bing"
        const val ENGINE_DDG = "duckduckgo"

        const val TEXT_SMALL = 75
        const val TEXT_MEDIUM = 100
        const val TEXT_LARGE = 125

        @Volatile
        private var INSTANCE: BrahmanPreferences? = null

        fun getInstance(context: Context): BrahmanPreferences {
            return INSTANCE ?: synchronized(this) {
                BrahmanPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    var searchEngine: String
        get() = prefs.getString(KEY_SEARCH_ENGINE, ENGINE_GOOGLE) ?: ENGINE_GOOGLE
        set(value) = prefs.edit().putString(KEY_SEARCH_ENGINE, value).apply()

    var homepage: String
        get() = prefs.getString(KEY_HOMEPAGE, "file:///android_asset/newtab.html")
            ?: "file:///android_asset/newtab.html"
        set(value) = prefs.edit().putString(KEY_HOMEPAGE, value).apply()

    var textSize: Int
        get() = prefs.getInt(KEY_TEXT_SIZE, TEXT_MEDIUM)
        set(value) = prefs.edit().putInt(KEY_TEXT_SIZE, value).apply()

    var javascriptEnabled: Boolean
        get() = prefs.getBoolean(KEY_JAVASCRIPT, true)
        set(value) = prefs.edit().putBoolean(KEY_JAVASCRIPT, value).apply()

    var nightModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_NIGHT_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_NIGHT_MODE, value).apply()

    var blockAutoplay: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_AUTOPLAY, true)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_AUTOPLAY, value).apply()

    var blockNotifications: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_NOTIFICATIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_NOTIFICATIONS, value).apply()

    fun getSearchUrl(query: String): String {
        val encoded = query.replace(" ", "+")
        return when (searchEngine) {
            ENGINE_BING -> "https://www.bing.com/search?q=$encoded"
            ENGINE_DDG -> "https://duckduckgo.com/?q=$encoded"
            else -> "https://www.google.com/search?q=$encoded"
        }
    }
}
