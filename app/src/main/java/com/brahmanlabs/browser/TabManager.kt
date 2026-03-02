package com.brahmanlabs.browser

class TabManager {
    val tabs = mutableListOf<BrowserTab>()
    private var currentIndex = 0

    fun addTab(tab: BrowserTab) {
        tabs.add(tab)
        currentIndex = tabs.lastIndex
    }

    fun getCurrentTab(): BrowserTab? {
        return if (tabs.isNotEmpty() && currentIndex in tabs.indices) tabs[currentIndex] else null
    }

    fun getCurrentTabIndex(): Int = currentIndex

    fun switchToTab(index: Int) {
        if (index in tabs.indices) currentIndex = index
    }

    fun removeTab(index: Int) {
        if (index in tabs.indices) {
            tabs.removeAt(index)
            currentIndex = when {
                tabs.isEmpty() -> 0
                currentIndex >= tabs.size -> tabs.lastIndex
                else -> currentIndex
            }
        }
    }

    fun toggleIncognito() {
        getCurrentTab()?.isIncognito = !(getCurrentTab()?.isIncognito ?: false)
    }

    fun toggleDesktopMode() {
        getCurrentTab()?.isDesktop = !(getCurrentTab()?.isDesktop ?: false)
    }
}
