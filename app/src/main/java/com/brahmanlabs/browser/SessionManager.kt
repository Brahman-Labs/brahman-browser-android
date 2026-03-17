package com.brahmanlabs.browser
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object SessionManager {
    private const val PREF_NAME = "brahman_session"
    private const val KEY_TABS = "saved_tabs"
    private const val KEY_ACTIVE_INDEX = "active_index"

    fun saveSession(context: Context, tabs: List<BrowserTab>, activeIndex: Int) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val normalTabs = tabs.filter { !it.isIncognito }
            val jsonArray = JSONArray()
            normalTabs.forEach { tab ->
                if (!tab.url.startsWith("file://")) {
                    val obj = JSONObject()
                    obj.put("url", tab.url)
                    obj.put("title", tab.title)
                    jsonArray.put(obj)
                }
            }
            prefs.edit()
                .putString(KEY_TABS, jsonArray.toString())
                .putInt(KEY_ACTIVE_INDEX, activeIndex.coerceIn(0, (normalTabs.size - 1).coerceAtLeast(0)))
                .apply()
        } catch (e: Exception) { }
    }

    fun loadSession(context: Context): Pair<List<BrowserTab>, Int> {
        return try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY_TABS, null) ?: return Pair(emptyList(), 0)
            val activeIndex = prefs.getInt(KEY_ACTIVE_INDEX, 0)
            val jsonArray = JSONArray(json)
            val tabs = mutableListOf<BrowserTab>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                tabs.add(BrowserTab(
                    url = obj.getString("url"),
                    title = obj.optString("title", "")
                ))
            }
            Pair(tabs, activeIndex.coerceIn(0, (tabs.size - 1).coerceAtLeast(0)))
        } catch (e: Exception) { Pair(emptyList(), 0) }
    }

    fun clearSession(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
