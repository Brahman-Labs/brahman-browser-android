package com.brahmanlabs.browser

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: BrahmanPreferences
    private lateinit var db: BrahmanDatabase
    private lateinit var btnBack: ImageButton
    private lateinit var tvSearchEngine: android.widget.TextView
    private lateinit var rowSearchEngine: LinearLayout
    private lateinit var etHomepage: EditText
    private lateinit var btnTextSmall: Button
    private lateinit var btnTextMedium: Button
    private lateinit var btnTextLarge: Button
    private lateinit var switchJavascript: SwitchCompat
    private lateinit var switchNightMode: SwitchCompat
    private lateinit var switchBlockNotifications: SwitchCompat
    private lateinit var switchBlockAutoplay: SwitchCompat
    private lateinit var rowClearHistory: LinearLayout
    private lateinit var rowClearCache: LinearLayout
    private lateinit var rowClearBookmarks: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        prefs = BrahmanPreferences.getInstance(this)
        db = BrahmanDatabase.getInstance(this)
        bindViews()
        loadCurrentSettings()
        setupListeners()
    }

    private fun bindViews() {
        btnBack = findViewById(R.id.btnSettingsBack)
        tvSearchEngine = findViewById(R.id.tvSearchEngine)
        rowSearchEngine = findViewById(R.id.rowSearchEngine)
        etHomepage = findViewById(R.id.etHomepage)
        btnTextSmall = findViewById(R.id.btnTextSmall)
        btnTextMedium = findViewById(R.id.btnTextMedium)
        btnTextLarge = findViewById(R.id.btnTextLarge)
        switchJavascript = findViewById(R.id.switchJavascript)
        switchNightMode = findViewById(R.id.switchNightMode)
        switchBlockNotifications = findViewById(R.id.switchBlockNotifications)
        switchBlockAutoplay = findViewById(R.id.switchBlockAutoplay)
        rowClearHistory = findViewById(R.id.rowClearHistory)
        rowClearCache = findViewById(R.id.rowClearCache)
        rowClearBookmarks = findViewById(R.id.rowClearBookmarks)
    }

    private fun loadCurrentSettings() {
        tvSearchEngine.text = when (prefs.searchEngine) {
            BrahmanPreferences.ENGINE_BING -> "Bing"
            BrahmanPreferences.ENGINE_DDG -> "DuckDuckGo"
            else -> "Google"
        }
        val homepage = prefs.homepage
        if (homepage == "file:///android_asset/newtab.html") {
            etHomepage.setText("")
            etHomepage.hint = "Default (New Tab page)"
        } else {
            etHomepage.setText(homepage)
        }
        updateTextSizeButtons(prefs.textSize)
        switchJavascript.isChecked = prefs.javascriptEnabled
        switchNightMode.isChecked = prefs.nightModeEnabled
        switchBlockNotifications.isChecked = prefs.blockNotifications
        switchBlockAutoplay.isChecked = prefs.blockAutoplay
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { saveHomepage(); setResult(RESULT_OK); finish() }

        rowSearchEngine.setOnClickListener {
            val engines = arrayOf("Google", "Bing", "DuckDuckGo")
            val current = when (prefs.searchEngine) {
                BrahmanPreferences.ENGINE_BING -> 1
                BrahmanPreferences.ENGINE_DDG -> 2
                else -> 0
            }
            AlertDialog.Builder(this)
                .setTitle("Search Engine")
                .setSingleChoiceItems(engines, current) { dialog, which ->
                    prefs.searchEngine = when (which) {
                        1 -> BrahmanPreferences.ENGINE_BING
                        2 -> BrahmanPreferences.ENGINE_DDG
                        else -> BrahmanPreferences.ENGINE_GOOGLE
                    }
                    tvSearchEngine.text = engines[which]
                    dialog.dismiss()
                    Toast.makeText(this, "Search engine updated", Toast.LENGTH_SHORT).show()
                }.show()
        }

        etHomepage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) { saveHomepage(); true } else false
        }

        btnTextSmall.setOnClickListener {
            prefs.textSize = BrahmanPreferences.TEXT_SMALL
            updateTextSizeButtons(BrahmanPreferences.TEXT_SMALL)
            Toast.makeText(this, "Text size: Small", Toast.LENGTH_SHORT).show()
        }
        btnTextMedium.setOnClickListener {
            prefs.textSize = BrahmanPreferences.TEXT_MEDIUM
            updateTextSizeButtons(BrahmanPreferences.TEXT_MEDIUM)
            Toast.makeText(this, "Text size: Medium", Toast.LENGTH_SHORT).show()
        }
        btnTextLarge.setOnClickListener {
            prefs.textSize = BrahmanPreferences.TEXT_LARGE
            updateTextSizeButtons(BrahmanPreferences.TEXT_LARGE)
            Toast.makeText(this, "Text size: Large", Toast.LENGTH_SHORT).show()
        }

        switchJavascript.setOnCheckedChangeListener { _, isChecked ->
            prefs.javascriptEnabled = isChecked
            Toast.makeText(this,
                if (isChecked) "JavaScript enabled" else "JavaScript disabled",
                Toast.LENGTH_SHORT).show()
        }

        switchNightMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.nightModeEnabled = isChecked
            Toast.makeText(this,
                if (isChecked) "Night Mode ON — reload pages to apply"
                else "Night Mode OFF — reload pages to apply",
                Toast.LENGTH_SHORT).show()
        }

        switchBlockNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.blockNotifications = isChecked
            Toast.makeText(this,
                if (isChecked) "Notification popups blocked" else "Notification popups allowed",
                Toast.LENGTH_SHORT).show()
        }

        switchBlockAutoplay.setOnCheckedChangeListener { _, isChecked ->
            prefs.blockAutoplay = isChecked
            Toast.makeText(this,
                if (isChecked) "Autoplay blocked" else "Autoplay allowed",
                Toast.LENGTH_SHORT).show()
        }

        rowClearHistory.setOnClickListener {
            AlertDialog.Builder(this).setTitle("Clear History")
                .setMessage("Clear all browsing history?")
                .setPositiveButton("Clear") { _, _ ->
                    lifecycleScope.launch {
                        db.historyDao().clearAll()
                        Toast.makeText(this@SettingsActivity, "History cleared", Toast.LENGTH_SHORT).show()
                    }
                }.setNegativeButton("Cancel", null).show()
        }

        rowClearCache.setOnClickListener {
            AlertDialog.Builder(this).setTitle("Clear Cache & Cookies")
                .setMessage("This will clear all cached data and cookies.")
                .setPositiveButton("Clear") { _, _ ->
                    android.webkit.WebStorage.getInstance().deleteAllData()
                    android.webkit.CookieManager.getInstance().removeAllCookies(null)
                    android.webkit.CookieManager.getInstance().flush()
                    applicationContext.cacheDir.deleteRecursively()
                    Toast.makeText(this, "Cache & cookies cleared", Toast.LENGTH_SHORT).show()
                }.setNegativeButton("Cancel", null).show()
        }

        rowClearBookmarks.setOnClickListener {
            AlertDialog.Builder(this).setTitle("Clear Bookmarks")
                .setMessage("Delete all bookmarks?")
                .setPositiveButton("Clear") { _, _ ->
                    lifecycleScope.launch {
                        db.bookmarkDao().deleteAll()
                        Toast.makeText(this@SettingsActivity, "Bookmarks cleared", Toast.LENGTH_SHORT).show()
                    }
                }.setNegativeButton("Cancel", null).show()
        }
    }

    private fun saveHomepage() {
        val input = etHomepage.text.toString().trim()
        prefs.homepage = when {
            input.isEmpty() -> "file:///android_asset/newtab.html"
            input.startsWith("http://") || input.startsWith("https://") -> input
            input.startsWith("file://") -> input
            input.contains(".") -> "https://$input"
            else -> "file:///android_asset/newtab.html"
        }
        Toast.makeText(this, "Homepage saved", Toast.LENGTH_SHORT).show()
    }

    private fun updateTextSizeButtons(size: Int) {
        val activeColor = getColor(R.color.waterCyan)
        val inactiveColor = getColor(R.color.waterFoam)
        btnTextSmall.setTextColor(if (size == BrahmanPreferences.TEXT_SMALL) activeColor else inactiveColor)
        btnTextMedium.setTextColor(if (size == BrahmanPreferences.TEXT_MEDIUM) activeColor else inactiveColor)
        btnTextLarge.setTextColor(if (size == BrahmanPreferences.TEXT_LARGE) activeColor else inactiveColor)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            saveHomepage(); setResult(RESULT_OK); finish(); return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
