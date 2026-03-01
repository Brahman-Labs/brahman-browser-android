package com.brahmanlabs.browser

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var lockIcon: ImageView
    private lateinit var addressBar: EditText
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnIncognito: ImageButton
    private lateinit var btnDesktop: ImageButton
    private lateinit var btnMore: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnForward: ImageButton
    private lateinit var btnRefresh: ImageButton
    private lateinit var btnHome: ImageButton
    private lateinit var btnNewTab: ImageButton
    private lateinit var tabRecycler: RecyclerView

    private val tabManager = TabManager()
    private lateinit var browserAdapter: BrowserAdapter
    private lateinit var db: BrahmanDatabase

    private val NEW_TAB_URL = "file:///android_asset/newtab.html"

    private val historyLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val url = result.data?.getStringExtra("url")
            if (!url.isNullOrEmpty()) loadUrl(url)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = BrahmanDatabase.getInstance(this)

        bindViews()
        setupWebView()
        setupRecyclerView()
        setupListeners()

        val firstTab = BrowserTab(url = NEW_TAB_URL, webView = webView)
        tabManager.addTab(firstTab)
        loadUrl(NEW_TAB_URL)
    }

    private fun bindViews() {
        swipeRefresh = findViewById(R.id.swipeRefresh)
        lockIcon = findViewById(R.id.lockIcon)
        addressBar = findViewById(R.id.addressBar)
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        btnIncognito = findViewById(R.id.btnIncognito)
        btnDesktop = findViewById(R.id.btnDesktop)
        btnMore = findViewById(R.id.btnMore)
        btnBack = findViewById(R.id.btnBack)
        btnForward = findViewById(R.id.btnForward)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnHome = findViewById(R.id.btnHome)
        btnNewTab = findViewById(R.id.btnNewTab)
        tabRecycler = findViewById(R.id.tabRecycler)
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            allowFileAccess = true
        }

        // JavaScript bridge for new tab page
        webView.addJavascriptInterface(object {
            @android.webkit.JavascriptInterface
            fun loadUrl(url: String) {
                runOnUiThread { this@MainActivity.loadUrl(url) }
            }
        }, "BrahmanBridge")

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val url = request.url.toString()
                if (url == NEW_TAB_URL) return false
                loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                swipeRefresh.isRefreshing = false

                val isNewTab = url == NEW_TAB_URL || url.startsWith("file://")
                val isHttps = url.startsWith("https://")

                lockIcon.visibility = if (isHttps) View.VISIBLE else View.GONE

                val title = if (isNewTab) "New Tab" else (view.title ?: "New Tab")
                tabManager.getCurrentTab()?.title = title
                tabManager.getCurrentTab()?.url = url
                browserAdapter.notifyDataSetChanged()
                updateNavButtons()

                // Show domain in address bar
                if (isNewTab) {
                    addressBar.setText("")
                    addressBar.hint = "Search or type URL"
                } else {
                    val domain = extractDomain(url)
                    addressBar.setText(domain)
                }

                // Save history (skip new tab and incognito)
                if (!isNewTab && tabManager.getCurrentTab()?.isIncognito == false) {
                    lifecycleScope.launch {
                        db.historyDao().insert(HistoryItem(title = title, url = url))
                    }
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
            }

            override fun onReceivedIcon(view: WebView, icon: Bitmap) {
                tabManager.getCurrentTab()?.favicon = icon
                browserAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun setupRecyclerView() {
        browserAdapter = BrowserAdapter(
            tabs = tabManager.tabs,
            onTabClick = { index ->
                tabManager.switchToTab(index)
                tabManager.getCurrentTab()?.url?.let { loadUrl(it) }
            },
            onTabClose = { index ->
                tabManager.removeTab(index)
                browserAdapter.notifyDataSetChanged()
                if (tabManager.tabs.isEmpty()) {
                    addNewTab()
                } else {
                    tabManager.getCurrentTab()?.url?.let { loadUrl(it) }
                }
            }
        )
        tabRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        tabRecycler.adapter = browserAdapter
    }

    private fun setupListeners() {
        // Show full URL when address bar is focused
        addressBar.setOnFocusChangeListener { _, hasFocus ->
            val currentUrl = tabManager.getCurrentTab()?.url ?: ""
            if (hasFocus) {
                val isNewTab = currentUrl == NEW_TAB_URL || currentUrl.startsWith("file://")
                if (!isNewTab) {
                    addressBar.setText(currentUrl)
                    addressBar.selectAll()
                }
            } else {
                val isNewTab = currentUrl == NEW_TAB_URL || currentUrl.startsWith("file://")
                if (isNewTab) {
                    addressBar.setText("")
                } else {
                    addressBar.setText(extractDomain(currentUrl))
                }
            }
        }

        addressBar.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                event?.keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                val input = addressBar.text.toString().trim()
                if (input.isNotEmpty()) {
                    loadUrl(resolveUrl(input))
                }
                hideKeyboard()
                true
            } else false
        }

        btnBack.setOnClickListener { if (webView.canGoBack()) webView.goBack() }
        btnForward.setOnClickListener { if (webView.canGoForward()) webView.goForward() }
        btnRefresh.setOnClickListener { webView.reload() }
        btnHome.setOnClickListener { loadUrl(NEW_TAB_URL) }
        btnNewTab.setOnClickListener { addNewTab() }

        btnIncognito.setOnClickListener {
            tabManager.toggleIncognito()
            btnIncognito.alpha =
                if (tabManager.getCurrentTab()?.isIncognito == true) 1.0f else 0.5f
        }

        btnDesktop.setOnClickListener {
            tabManager.toggleDesktopMode()
            val isDesktop = tabManager.getCurrentTab()?.isDesktop == true
            btnDesktop.alpha = if (isDesktop) 1.0f else 0.5f
            webView.settings.userAgentString = if (isDesktop)
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36"
            else ""
            webView.reload()
        }

        btnMore.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add(0, 1, 0, "Bookmarks")
            popup.menu.add(0, 2, 0, "History")
            popup.menu.add(0, 3, 0, "Downloads")
            popup.menu.add(0, 4, 0, "Settings")
            popup.menu.add(0, 5, 0, "Share")
            popup.menu.add(0, 6, 0, "Find in page")
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> { true }
                    2 -> {
                        historyLauncher.launch(Intent(this, HistoryActivity::class.java))
                        true
                    }
                    3 -> { true }
                    4 -> { true }
                    5 -> {
                        val currentUrl = webView.url ?: ""
                        if (currentUrl.isNotEmpty() && !currentUrl.startsWith("file://")) {
                            val share = Intent(Intent.ACTION_SEND)
                            share.type = "text/plain"
                            share.putExtra(Intent.EXTRA_TEXT, currentUrl)
                            startActivity(Intent.createChooser(share, "Share URL"))
                        }
                        true
                    }
                    6 -> { true }
                    else -> false
                }
            }
            popup.show()
        }

        swipeRefresh.setOnRefreshListener { webView.reload() }
        swipeRefresh.setColorSchemeColors(
            getColor(R.color.waterCyan),
            getColor(R.color.waterCyanLight),
            getColor(R.color.waterFoam)
        )
        swipeRefresh.setProgressBackgroundColorSchemeColor(getColor(R.color.waterMid))
    }

    private fun addNewTab() {
        val newTab = BrowserTab(url = NEW_TAB_URL)
        tabManager.addTab(newTab)
        browserAdapter.notifyDataSetChanged()
        loadUrl(NEW_TAB_URL)
        tabRecycler.scrollToPosition(tabManager.tabs.lastIndex)
    }

    private fun extractDomain(url: String): String {
        return try {
            val host = Uri.parse(url).host ?: url
            host.removePrefix("www.")
        } catch (e: Exception) {
            url
        }
    }

    private fun resolveUrl(input: String): String = when {
        input.startsWith("http://") || input.startsWith("https://") -> input
        input.contains(".") && !input.contains(" ") -> "https://$input"
        else -> "https://www.google.com/search?q=${input.replace(" ", "+")}"
    }

    private fun loadUrl(url: String) {
        tabManager.getCurrentTab()?.url = url
        webView.loadUrl(url)
        val isNewTab = url == NEW_TAB_URL || url.startsWith("file://")
        if (isNewTab) {
            addressBar.setText("")
            addressBar.hint = "Search or type URL"
            lockIcon.visibility = View.GONE
        } else {
            addressBar.setText(extractDomain(url))
        }
    }

    private fun updateNavButtons() {
        btnBack.alpha = if (webView.canGoBack()) 1.0f else 0.4f
        btnForward.alpha = if (webView.canGoForward()) 1.0f else 0.4f
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(addressBar.windowToken, 0)
        addressBar.clearFocus()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
