path = 'app/src/main/java/com/brahmanlabs/browser/MainActivity.kt'
with open(path, 'r') as f: c = f.read()

# Replace "ask dialog" with silent auto-restore like Brave
old = '''                val (savedTabs, savedIndex) = SessionManager.loadSession(this)
                if (savedTabs.isNotEmpty()) {
                    showRestoreSessionDialog(savedTabs, savedIndex)
                } else {
                    openTab(NEW_TAB_URL)
                }'''

new = '''                val (savedTabs, savedIndex) = SessionManager.loadSession(this)
                if (savedTabs.isNotEmpty()) {
                    // Silent restore like Brave — no dialog, just restore
                    savedTabs.forEachIndexed { index, tab ->
                        val wv = createWebView()
                        tab.webView = wv
                        tabManager.addTab(tab)
                        if (index == savedIndex) wv.visibility = View.VISIBLE
                        wv.loadUrl(tab.url)
                    }
                    browserAdapter.notifyDataSetChanged()
                    val targetIndex = savedIndex.coerceIn(0, tabManager.tabs.size - 1)
                    tabManager.switchToTab(targetIndex)
                    val url = tabManager.getCurrentTab()?.url ?: NEW_TAB_URL
                    updateUIForUrl(url)
                    updateNavButtons()
                    updateShieldIcon(url)
                    updateSwipeRefreshTarget()
                    SessionManager.clearSession(this)
                } else {
                    openTab(NEW_TAB_URL)
                }'''

if old in c:
    c = c.replace(old, new)
    print("✅ Silent session restore — no more dialog")
else:
    print("⚠️  Pattern not found")

with open(path, 'w') as f: f.write(c)
