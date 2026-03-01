package com.brahmanlabs.browser

import android.webkit.WebView

data class BrowserTab(
    var url: String = "https://google.com",
    var title: String = "New Tab",
    var isIncognito: Boolean = false,
    var isDesktop: Boolean = false,
    var webView: WebView? = null
)
