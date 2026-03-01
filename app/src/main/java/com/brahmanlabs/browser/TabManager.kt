package com.brahmanlabs.browser

class TabManager {
    val tabs = mutableListOf<BrowserTab>()
    var currentIndex = 0
        private set

    fun addTab(tab: BrowserTab) {
        tabs.add(tab)
        currentIndex = tabs.lastIndex
    }

    fun removeTab(index: Int) {
        if (index < 0 || index >= tabs.size) return
        tabs.removeAt(index)
        currentIndex = when {
            tabs.isEmpty() -> 0
            currentIndex >= tabs.size -> tabs.lastIndex
            else -> currentIndex
        }
    }

    fun switchToTab(index: Int) {
        if (index >= 0 && index < tabs.size) {
            currentIndex = index
        }
    }

    fun getCurrentTab(): BrowserTab? {
        return if (tabs.isEmpty()) null else tabs[currentIndex]
    }

    fun toggleIncognito() {
        getCurrentTab()?.let { it.isIncognito = !it.isIncognito }
    }

    fun toggleDesktopMode() {
        getCurrentTab()?.let { it.isDesktop = !it.isDesktop }
    }

    fun hasMultipleTabs() = tabs.size > 1
}
