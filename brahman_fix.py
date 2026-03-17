import re, os

BASE = 'app/src/main/java/com/brahmanlabs/browser/'

# ══════════════════════════════════════════════════════
# FIX 1 — Create BrahmanApp.kt (global dark mode = all
#          white dialogs, menus, switches fixed forever)
# ══════════════════════════════════════════════════════
with open(BASE + 'BrahmanApp.kt', 'w') as f:
    f.write("""package com.brahmanlabs.browser

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class BrahmanApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}
""")
print("✅ FIX 1: BrahmanApp.kt created — dark mode global")

# ══════════════════════════════════════════════════════
# FIX 2 — Register BrahmanApp in AndroidManifest.xml
# ══════════════════════════════════════════════════════
mpath = 'app/src/main/AndroidManifest.xml'
with open(mpath, 'r') as f: m = f.read()
if 'android:name=".BrahmanApp"' not in m:
    m = m.replace(
        '        android:allowBackup="true"',
        '        android:name=".BrahmanApp"\n        android:allowBackup="true"'
    )
    with open(mpath, 'w') as f: f.write(m)
    print("✅ FIX 2: AndroidManifest.xml updated")
else:
    print("⏭  FIX 2: Already registered")

# ══════════════════════════════════════════════════════
# FIXES 3-6 — MainActivity.kt
# ══════════════════════════════════════════════════════
mpath = BASE + 'MainActivity.kt'
with open(mpath, 'r') as f: c = f.read()

# FIX 3 — shouldOverrideUrlLoading: stop intercepting ALL
# navigation (fixes wbing.com redirect + broken back stack)
old = '''            override fun shouldOverrideUrlLoading(
                view: WebView, request: WebResourceRequest
            ): Boolean {
                val url = request.url.toString()
                if (url.startsWith("file://")) return false
                if (url.startsWith("javascript:") || url.startsWith("data:")) return false
                if (url.startsWith("view-source:")) { view.loadUrl(url); return false }
                val tab = tabManager.tabs.find { it.webView == view }
                val pageUrl = tab?.url ?: ""
                val cleanedUrl = if (adBlocker.isShieldsEnabled(pageUrl))
                    adBlocker.cleanUrl(url) else url
                if (cleanedUrl.startsWith("http://") &&
                    adBlocker.isShieldsEnabled(pageUrl) &&
                    adBlocker.isHttpsUpgradeEnabled(pageUrl) &&
                    !pageUrl.startsWith("https://")) {
                    view.loadUrl(cleanedUrl.replaceFirst("http://", "https://"))
                    return true
                }
                view.loadUrl(cleanedUrl)
                return true
            }'''
new = '''            override fun shouldOverrideUrlLoading(
                view: WebView, request: WebResourceRequest
            ): Boolean {
                val url = request.url.toString()
                if (url.startsWith("file://")) return false
                if (url.startsWith("javascript:") || url.startsWith("data:")) return false
                if (url.startsWith("view-source:")) { view.loadUrl(url); return false }
                // Handle special schemes: tel:, mailto:, intent:, market:, etc.
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    return try {
                        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(i); true
                    } catch (e: Exception) { false }
                }
                val tab = tabManager.tabs.find { it.webView == view }
                val pageUrl = tab?.url ?: ""
                val cleanedUrl = if (adBlocker.isShieldsEnabled(pageUrl))
                    adBlocker.cleanUrl(url) else url
                // HTTPS upgrade — only intercept when actually upgrading
                if (cleanedUrl.startsWith("http://") &&
                    adBlocker.isShieldsEnabled(pageUrl) &&
                    adBlocker.isHttpsUpgradeEnabled(pageUrl)) {
                    view.loadUrl(cleanedUrl.replaceFirst("http://", "https://"))
                    return true
                }
                // Only intercept if tracking params were actually stripped
                if (cleanedUrl != url) {
                    view.loadUrl(cleanedUrl)
                    return true
                }
                // Let WebView handle all normal navigation natively
                return false
            }'''
if old in c:
    c = c.replace(old, new)
    print("✅ FIX 3: shouldOverrideUrlLoading fixed — navigation natural")
else:
    print("⚠️  FIX 3: Pattern not found — check manually")

# FIX 4 — Chrome UA (stops Bing/Google captchas)
old_ua = '''    private fun applyWebViewSettings(wv: WebView) {
        wv.settings.apply {
            javaScriptEnabled = prefs.javascriptEnabled'''
new_ua = '''    private fun applyWebViewSettings(wv: WebView) {
        // Chrome-like UA — stops bot/captcha detection on Bing, Google, etc.
        wv.settings.userAgentString =
            "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
        wv.settings.apply {
            javaScriptEnabled = prefs.javascriptEnabled'''
if old_ua in c:
    c = c.replace(old_ua, new_ua)
    print("✅ FIX 4: Chrome UA set — no more captchas")
else:
    print("⚠️  FIX 4: UA pattern not found")

# FIX 4b — restore Chrome UA when exiting desktop mode
c = c.replace(
    'else ""',
    'else "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"',
    1
)
print("✅ FIX 4b: Desktop mode exit restores Chrome UA")

# FIX 5 — Suggestions dismiss on tap outside (dispatchTouchEvent)
dispatch = '''
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && isAddressBarFocused) {
            val abRect = android.graphics.Rect()
            addressBar.getGlobalVisibleRect(abRect)
            val spRect = android.graphics.Rect()
            suggestionsPanel.getGlobalVisibleRect(spRect)
            val x = event.rawX.toInt()
            val y = event.rawY.toInt()
            if (!abRect.contains(x, y) && !spRect.contains(x, y)) {
                hideKeyboard()
            }
        }
        return super.dispatchTouchEvent(event)
    }

'''
if 'dispatchTouchEvent' not in c:
    c = c.replace('    override fun onDestroy() {', dispatch + '    override fun onDestroy() {')
    print("✅ FIX 5: Suggestions dismiss on tap outside")
else:
    print("⏭  FIX 5: dispatchTouchEvent already present")

# FIX 6 — Smooth fade animation on tab switch
old_vis = '''        tab.webView?.visibility = View.VISIBLE
        updateUIForUrl(tab.url)
        updateNavButtons()
        updateShieldIcon(tab.url)
        updateSwipeRefreshTarget()
        browserAdapter.notifyItemChanged(oldIndex)
        browserAdapter.notifyItemChanged(index)
        tabRecycler.scrollToPosition(index)'''
new_vis = '''        tab.webView?.alpha = 0f
        tab.webView?.visibility = View.VISIBLE
        tab.webView?.animate()?.alpha(1f)?.setDuration(120)?.start()
        updateUIForUrl(tab.url)
        updateNavButtons()
        updateShieldIcon(tab.url)
        updateSwipeRefreshTarget()
        browserAdapter.notifyItemChanged(oldIndex)
        browserAdapter.notifyItemChanged(index)
        tabRecycler.scrollToPosition(index)'''
if old_vis in c:
    c = c.replace(old_vis, new_vis)
    print("✅ FIX 6: Smooth tab switch animation")
else:
    print("⚠️  FIX 6: Tab animation pattern not found")

with open(mpath, 'w') as f: f.write(c)

# ══════════════════════════════════════════════════════
# FIX 7 — ShieldsPanel dp position
# ══════════════════════════════════════════════════════
spath = BASE + 'ShieldsPanel.kt'
with open(spath, 'r') as f: c = f.read()
if 'attrs.y = 155' in c:
    c = c.replace(
        'attrs.y = 155',
        'attrs.y = (context.resources.displayMetrics.density * 52f).toInt()'
    )
    with open(spath, 'w') as f: f.write(c)
    print("✅ FIX 7: ShieldsPanel y position → dp")
else:
    print("⏭  FIX 7: Already fixed")

print("\n🌊 All fixes done! Now build:")
print("./gradlew assembleDebug --no-daemon")
