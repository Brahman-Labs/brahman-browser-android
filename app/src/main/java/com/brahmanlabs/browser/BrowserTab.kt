package com.brahmanlabs.browser

import android.graphics.Bitmap
import android.webkit.WebView

data class BrowserTab(
    var url: String = "file:///android_asset/newtab.html",
    var title: String = "New Tab",
    var isIncognito: Boolean = false,
    var isDesktop: Boolean = false,
    var webView: WebView? = null,
    var favicon: Bitmap? = null
)
