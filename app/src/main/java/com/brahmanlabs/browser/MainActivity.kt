package com.brahmanlabs.browser

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupWebView()
        setupRecyclerView()
        setupListeners()

        val firstTab = BrowserTab(url = "https://google.com", webView = webView)
        tabManager.addTab(firstTab)
        loadUrl("https://google.com")
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
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                loadUrl(request.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                addressBar.setText(url)
                swipeRefresh.isRefreshing = false
                lockIcon.visibility = if (url.startsWith("https://")) View.VISIBLE else View.GONE
                tabManager.getCurrentTab()?.title = view.title ?: "New Tab"
                tabManager.getCurrentTab()?.url = url
                browserAdapter.notifyDataSetChanged()
                updateNavButtons()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
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
        addressBar.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                event?.keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                loadUrl(resolveUrl(addressBar.text.toString().trim()))
                true
            } else false
        }

        btnBack.setOnClickListener { if (webView.canGoBack()) webView.goBack() }
        btnForward.setOnClickListener { if (webView.canGoForward()) webView.goForward() }
        btnRefresh.setOnClickListener { webView.reload() }
        btnHome.setOnClickListener { loadUrl("https://google.com") }
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
                    1 -> { /* TODO: Bookmarks */ true }
                    2 -> { /* TODO: History */ true }
                    3 -> { /* TODO: Downloads */ true }
                    4 -> { /* TODO: Settings */ true }
                    5 -> {
                        val share = Intent(Intent.ACTION_SEND)
                        share.type = "text/plain"
                        share.putExtra(Intent.EXTRA_TEXT, webView.url)
                        startActivity(Intent.createChooser(share, "Share URL"))
                        true
                    }
                    6 -> { /* TODO: Find in page */ true }
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
        val newTab = BrowserTab(url = "https://google.com")
        tabManager.addTab(newTab)
        browserAdapter.notifyDataSetChanged()
        loadUrl("https://google.com")
        tabRecycler.scrollToPosition(tabManager.tabs.lastIndex)
    }

    private fun resolveUrl(input: String): String = when {
        input.startsWith("http://") || input.startsWith("https://") -> input
        input.contains(".") && !input.contains(" ") -> "https://$input"
        else -> "https://www.google.com/search?q=${input.replace(" ", "+")}"
    }

    private fun loadUrl(url: String) {
        tabManager.getCurrentTab()?.url = url
        webView.loadUrl(url)
        addressBar.setText(url)
    }

    private fun updateNavButtons() {
        btnBack.alpha = if (webView.canGoBack()) 1.0f else 0.4f
        btnForward.alpha = if (webView.canGoForward()) 1.0f else 0.4f
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
