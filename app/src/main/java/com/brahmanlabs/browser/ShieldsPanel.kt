package com.brahmanlabs.browser

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat

class ShieldsPanel(
    context: Context,
    private val currentUrl: String,
    private val adBlocker: AdBlocker,
    private val onSettingsChanged: () -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_shields)

        window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setGravity(Gravity.TOP or Gravity.END)
            val attrs = attributes
            attrs.x = 10
            attrs.y = 155
            attributes = attrs
            val w = (context.resources.displayMetrics.widthPixels * 0.93f).toInt()
            setLayout(w, WindowManager.LayoutParams.WRAP_CONTENT)
        }

        val domain = extractDomain(currentUrl)

        // Header
        val tvDomain = findViewById<TextView>(R.id.shieldsDomain)
        val tvStatus = findViewById<TextView>(R.id.shieldsStatus)
        val tvCount = findViewById<TextView>(R.id.shieldsBlockedCount)
        val tvAdvDomain = findViewById<TextView>(R.id.advancedDomain)
        val tvTrackersLevel = findViewById<TextView>(R.id.tvTrackersLevel)
        val switchShields = findViewById<SwitchCompat>(R.id.switchShields)
        val switchHttps = findViewById<SwitchCompat>(R.id.switchHttps)
        val switchScripts = findViewById<SwitchCompat>(R.id.switchBlockScripts)
        val switchForget = findViewById<SwitchCompat>(R.id.switchForgetOnClose)
        val switchCookies = findViewById<SwitchCompat>(R.id.switchBlockCookies)
        val switchFp = findViewById<SwitchCompat>(R.id.switchBlockFingerprinting)
        val advancedHeader = findViewById<LinearLayout>(R.id.advancedHeader)
        val advancedSection = findViewById<LinearLayout>(R.id.advancedSection)
        val btnAdvancedArrow = findViewById<ImageButton>(R.id.btnAdvancedArrow)
        val btnClose = findViewById<ImageButton>(R.id.btnShieldsClose)

        tvDomain.text = domain.ifEmpty { "This page" }
        tvAdvDomain.text = domain.ifEmpty { "This page" }
        tvCount.text = adBlocker.getBlockedCount().toString()
        tvTrackersLevel.text = if (adBlocker.isShieldsEnabled(currentUrl)) "Standard" else "Off"

        // Load current values
        val isEnabled = adBlocker.isShieldsEnabled(currentUrl)
        switchShields.isChecked = isEnabled
        applyStatus(tvStatus, isEnabled)

        switchHttps.isChecked = adBlocker.isHttpsUpgradeEnabled(currentUrl)
        switchScripts.isChecked = adBlocker.isScriptsBlocked(currentUrl)
        switchForget.isChecked = adBlocker.isForgetOnClose(currentUrl)
        switchCookies.isChecked = adBlocker.isCookiesBlocked(currentUrl)
        switchFp.isChecked = adBlocker.isFingerprintingBlocked(currentUrl)

        setAdvancedAlpha(isEnabled, switchHttps, switchScripts, switchForget, switchCookies, switchFp)

        // Main toggle
        switchShields.setOnCheckedChangeListener { _, checked ->
            adBlocker.setShieldsEnabled(currentUrl, checked)
            applyStatus(tvStatus, checked)
            tvTrackersLevel.text = if (checked) "Standard" else "Off"
            setAdvancedAlpha(checked, switchHttps, switchScripts, switchForget, switchCookies, switchFp)
            onSettingsChanged()
            dismiss()
        }

        // Advanced expand/collapse
        var expanded = false
        advancedHeader.setOnClickListener {
            expanded = !expanded
            advancedSection.visibility = if (expanded) View.VISIBLE else View.GONE
            btnAdvancedArrow.rotation = if (expanded) 270f else 90f
        }

        // HTTPS Upgrade
        switchHttps.setOnCheckedChangeListener { _, checked ->
            adBlocker.setHttpsUpgradeEnabled(currentUrl, checked)
            onSettingsChanged(); dismiss()
        }

        // Block Scripts
        switchScripts.setOnCheckedChangeListener { _, checked ->
            adBlocker.setScriptsBlocked(currentUrl, checked)
            onSettingsChanged(); dismiss()
        }

        // Forget on close (no reload needed)
        switchForget.setOnCheckedChangeListener { _, checked ->
            adBlocker.setForgetOnClose(currentUrl, checked)
        }

        // Block Cookies
        switchCookies.setOnCheckedChangeListener { _, checked ->
            adBlocker.setCookiesBlocked(currentUrl, checked)
            onSettingsChanged(); dismiss()
        }

        // Block Fingerprinting
        switchFp.setOnCheckedChangeListener { _, checked ->
            adBlocker.setFingerprintingBlocked(currentUrl, checked)
            onSettingsChanged(); dismiss()
        }

        btnClose.setOnClickListener { dismiss() }
    }

    private fun applyStatus(tv: TextView, enabled: Boolean) {
        tv.text = if (enabled) "Brahman Shields  UP" else "Brahman Shields  DOWN"
        tv.setTextColor(
            if (enabled) context.getColor(R.color.waterCyan)
            else context.getColor(R.color.textHint)
        )
    }

    private fun setAdvancedAlpha(enabled: Boolean, vararg switches: SwitchCompat) {
        switches.forEach {
            it.isEnabled = enabled
            it.alpha = if (enabled) 1f else 0.35f
        }
    }

    private fun extractDomain(url: String): String = try {
        Uri.parse(url).host?.removePrefix("www.") ?: ""
    } catch (e: Exception) { "" }
}
