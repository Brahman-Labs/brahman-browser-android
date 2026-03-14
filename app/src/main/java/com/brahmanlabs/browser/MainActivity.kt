package com.brahmanlabs.browser

import android.app.Activity
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
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
    private lateinit var addressBarRow: LinearLayout
    private lateinit var navigationBar: LinearLayout
    private lateinit var btnBookmark: ImageButton
    private lateinit var btnIncognito: ImageButton
    private lateinit var btnDesktop: ImageButton
    private lateinit var btnShield: ImageButton
    private lateinit var shieldBadge: TextView
    private lateinit var btnMore: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnForward: ImageButton
    private lateinit var btnRefresh: ImageButton
    private lateinit var btnHome: ImageButton
    private lateinit var btnNewTab: ImageButton
    private lateinit var btnTabGrid: ImageButton
    private lateinit var tabRecycler: RecyclerView
    private lateinit var findInPageBar: LinearLayout
    private lateinit var findInput: EditText
    private lateinit var findMatchCount: TextView
    private lateinit var btnFindPrev: ImageButton
    private lateinit var btnFindNext: ImageButton
    private lateinit var btnFindClose: ImageButton
    private lateinit var tabGridOverlay: RelativeLayout
    private lateinit var tabGridRecycler: RecyclerView
    private lateinit var tabCountLabel: TextView
    private lateinit var btnGridClose: ImageButton
    private lateinit var btnGridNewTab: ImageButton
    private lateinit var btnCloseAllTabs: Button
    private lateinit var btnGridAddNewTab: Button

    private val tabManager = TabManager()
    private lateinit var browserAdapter: BrowserAdapter
    private lateinit var db: BrahmanDatabase
    private lateinit var prefs: BrahmanPreferences
    private lateinit var adBlocker: AdBlocker
    private lateinit var gestureDetector: GestureDetector

    private val NEW_TAB_URL = "file:///android_asset/newtab.html"
    private var lastSavedHistoryUrl = ""
    private var isReaderModeOn = false
    private val historyDebounceHandler = Handler(Looper.getMainLooper())
    private var historyDebounceRunnable: Runnable? = null

    companion object {
        private const val FP_JS = """
(function(){
  try{
    var orig=HTMLCanvasElement.prototype.getContext;
    HTMLCanvasElement.prototype.getContext=function(t,a){
      var ctx=orig.call(this,t,a);
      if(ctx&&t==='2d'){
        var oi=ctx.getImageData.bind(ctx);
        ctx.getImageData=function(x,y,w,h){
          var d=oi(x,y,w,h);
          for(var i=0;i<d.data.length;i+=4){d.data[i]^=1;}
          return d;
        };
      }
      return ctx;
    };
    try{Object.defineProperty(navigator,'hardwareConcurrency',{get:function(){return 4;},configurable:true});}catch(e){}
    try{Object.defineProperty(screen,'colorDepth',{get:function(){return 24;},configurable:true});}catch(e){}
  }catch(e){}
})();
"""
        private const val NIGHT_MODE_JS = """
(function(){
  var s=document.getElementById('__brahman_night__');
  if(!s){s=document.createElement('style');s.id='__brahman_night__';document.head.appendChild(s);}
  s.textContent='html{filter:invert(1) hue-rotate(180deg)!important;}img,video,canvas,iframe{filter:invert(1) hue-rotate(180deg)!important;}';
})();
"""
        private const val READER_MODE_JS = """
(function(){
  try{
    var c=document.querySelector('article')||
      document.querySelector('[role="main"]')||
      document.querySelector('main')||
      document.querySelector('.post-content')||
      document.querySelector('.entry-content')||
      document.querySelector('.article-body')||
      document.querySelector('.content')||
      document.body;
    var title=document.title||'';
    var clone=c.cloneNode(true);
    clone.querySelectorAll('script,style,nav,header,footer,aside,iframe,.ad,.ads,'+
      '[class*="ad-"],[id*="ad-"],[class*="banner"],[class*="sidebar"],'+
      '[class*="popup"],[class*="cookie"],[class*="newsletter"],[class*="social"]')
      .forEach(function(el){el.remove();});
    document.body.innerHTML='<div id="br-reader"><h1 id="br-t"></h1><div id="br-c"></div></div>';
    document.getElementById('br-t').textContent=title;
    document.getElementById('br-c').innerHTML=clone.innerHTML;
    var s=document.createElement('style');
    s.textContent='*{max-width:100%!important;}body{background:#050E1F!important;color:#CAF0F8!important;'+
      'font-family:Georgia,serif!important;padding:20px 16px!important;max-width:680px!important;'+
      'margin:0 auto!important;font-size:17px!important;line-height:1.85!important;}'+
      '#br-reader h1{color:#00B4D8!important;font-size:21px!important;'+
      'margin-bottom:20px!important;line-height:1.4!important;border-bottom:1px solid #0A2A4A;padding-bottom:12px;}'+
      'img{max-width:100%!important;height:auto!important;border-radius:8px!important;margin:8px 0!important;}'+
      'a{color:#48CAE4!important;}p{margin-bottom:14px!important;}'+
      'h2,h3,h4{color:#90E0EF!important;margin:16px 0 8px!important;}';
    document.head.appendChild(s);
  }catch(e){}
})();
"""
    }

    private val historyLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val url = result.data?.getStringExtra("url")
            if (!url.isNullOrEmpty()) loadUrl(url)
        }
    }

    private val bookmarkLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val url = result.data?.getStringExtra("url")
            if (!url.isNullOrEmpty()) loadUrl(url)
        }
    }

    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            progressBar.visibility = View.GONE
            swipeRefresh.isRefreshing = false
            applySettings()
        }
    }

    private val downloadLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = BrahmanDatabase.getInstance(this)
        prefs = BrahmanPreferences.getInstance(this)
        adBlocker = AdBlocker.getInstance(this)
        bindViews()
        setupWindowInsets()
        setupGestureDetector()
        setupWebView()
        applySettings()
        setupRecyclerView()
        setupListeners()
        setupTabGrid()
        val urlToLoad = resolveIncomingIntent(intent) ?: NEW_TAB_URL
        tabManager.addTab(BrowserTab(url = urlToLoad, webView = webView))
        loadUrl(urlToLoad)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val url = resolveIncomingIntent(intent)
        if (!url.isNullOrEmpty()) { addNewTab(); loadUrl(url) }
    }

    override fun onStop() {
        super.onStop()
        if (adBlocker.hasAnyForgetOnClose()) {
            tabManager.tabs.forEach { tab ->
                if (adBlocker.isForgetOnClose(tab.url)) {
                    CookieManager.getInstance().removeAllCookies(null)
                    CookieManager.getInstance().flush()
                    WebStorage.getInstance().deleteAllData()
                }
            }
        }
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(this,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?, e2: MotionEvent,
                    velocityX: Float, velocityY: Float
                ): Boolean {
                    val diffX = e2.x - (e1?.x ?: 0f)
                    val diffY = e2.y - (e1?.y ?: 0f)
                    if (Math.abs(diffX) > Math.abs(diffY) &&
                        Math.abs(diffX) > 120 &&
                        Math.abs(velocityX) > 900) {
                        val idx = tabManager.getCurrentTabIndex()
                        if (diffX > 0 && idx > 0) {
                            switchToTabAt(idx - 1)
                        } else if (diffX < 0 && idx < tabManager.tabs.size - 1) {
                            switchToTabAt(idx + 1)
                        }
                        return true
                    }
                    return false
                }
            })
    }

    private fun switchToTabAt(index: Int) {
        tabManager.switchToTab(index)
        val url = tabManager.getCurrentTab()?.url ?: NEW_TAB_URL
        loadUrl(url)
        updateAddressBarForUrl(url)
        updateShieldIcon(url)
        browserAdapter.notifyDataSetChanged()
        tabRecycler.scrollToPosition(index)
        Toast.makeText(this, "Tab ${index + 1} of ${tabManager.tabs.size}",
            Toast.LENGTH_SHORT).show()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(navigationBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom + 4
            }
            insets
        }
    }

    private fun resolveIncomingIntent(intent: Intent?): String? {
        if (intent == null) return null
        return when (intent.action) {
            Intent.ACTION_VIEW -> intent.dataString
            Intent.ACTION_SEND -> if (intent.type == "text/plain")
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let { resolveUrl(it) } else null
            else -> null
        }
    }

    private fun applySettings() {
        webView.settings.apply {
            javaScriptEnabled = prefs.javascriptEnabled
            textZoom = prefs.textSize
            mediaPlaybackRequiresUserGesture = prefs.blockAutoplay
        }
    }

    private fun applyShieldSettings(url: String) {
        if (url.startsWith("file://")) return
        webView.settings.javaScriptEnabled =
            if (adBlocker.isScriptsBlocked(url)) false else prefs.javascriptEnabled
        val cookiesBlocked = adBlocker.isCookiesBlocked(url)
        CookieManager.getInstance().setAcceptCookie(!cookiesBlocked)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, !cookiesBlocked)
    }

    private fun injectNightMode(view: WebView) {
        if (prefs.nightModeEnabled) view.evaluateJavascript(NIGHT_MODE_JS, null)
    }

    private fun loadFrequentSites() {
        lifecycleScope.launch {
            val items = db.historyDao().getAll()
            val domainMap = mutableMapOf<String, Pair<String, Int>>()
            items.forEach { item ->
                try {
                    val domain = Uri.parse(item.url).host
                        ?.removePrefix("www.") ?: return@forEach
                    if (domain.isEmpty() || domain.startsWith("file")) return@forEach
                    val existing = domainMap[domain]
                    domainMap[domain] = if (existing == null)
                        Pair(item.url, 1)
                    else Pair(existing.first, existing.second + 1)
                } catch (e: Exception) { }
            }
            val top8 = domainMap.entries
                .sortedByDescending { it.value.second }
                .take(8)
                .map { entry ->
                    val domain = entry.key
                    val url = entry.value.first
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("'", "\\'")
                    val letter = domain.firstOrNull()
                        ?.uppercaseChar()?.toString() ?: "?"
                    """{"domain":"$domain","url":"$url","letter":"$letter"}"""
                }
            if (top8.isNotEmpty()) {
                val json = "[${top8.joinToString(",")}]"
                    .replace("'", "\\'")
                runOnUiThread {
                    webView.evaluateJavascript(
                        "if(typeof brahmanSetFrequent==='function')brahmanSetFrequent('$json');",
                        null
                    )
                }
            }
        }
    }

    private fun bindViews() {
        swipeRefresh = findViewById(R.id.swipeRefresh)
        lockIcon = findViewById(R.id.lockIcon)
        addressBar = findViewById(R.id.addressBar)
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        addressBarRow = findViewById(R.id.addressBarRow)
        navigationBar = findViewById(R.id.navigationBar)
        btnBookmark = findViewById(R.id.btnBookmark)
        btnIncognito = findViewById(R.id.btnIncognito)
        btnDesktop = findViewById(R.id.btnDesktop)
        btnShield = findViewById(R.id.btnShield)
        shieldBadge = findViewById(R.id.shieldBadge)
        btnMore = findViewById(R.id.btnMore)
        btnBack = findViewById(R.id.btnBack)
        btnForward = findViewById(R.id.btnForward)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnHome = findViewById(R.id.btnHome)
        btnNewTab = findViewById(R.id.btnNewTab)
        btnTabGrid = findViewById(R.id.btnTabGrid)
        tabRecycler = findViewById(R.id.tabRecycler)
        findInPageBar = findViewById(R.id.findInPageBar)
        findInput = findViewById(R.id.findInput)
        findMatchCount = findViewById(R.id.findMatchCount)
        btnFindPrev = findViewById(R.id.btnFindPrev)
        btnFindNext = findViewById(R.id.btnFindNext)
        btnFindClose = findViewById(R.id.btnFindClose)
        tabGridOverlay = findViewById(R.id.tabGridOverlay)
        tabGridRecycler = findViewById(R.id.tabGridRecycler)
        tabCountLabel = findViewById(R.id.tabCountLabel)
        btnGridClose = findViewById(R.id.btnGridClose)
        btnGridNewTab = findViewById(R.id.btnGridNewTab)
        btnCloseAllTabs = findViewById(R.id.btnCloseAllTabs)
        btnGridAddNewTab = findViewById(R.id.btnGridAddNewTab)
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = prefs.javascriptEnabled
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            allowFileAccess = true
            textZoom = prefs.textSize
            mediaPlaybackRequiresUserGesture = prefs.blockAutoplay
        }

        webView.addJavascriptInterface(object {
            @android.webkit.JavascriptInterface
            fun loadUrl(url: String) { runOnUiThread { this@MainActivity.loadUrl(url) } }
            @android.webkit.JavascriptInterface
            fun getSearchEngine(): String = prefs.searchEngine
        }, "BrahmanBridge")

        webView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }

        webView.setOnLongClickListener {
            val result = webView.hitTestResult
            val extra = result.extra ?: return@setOnLongClickListener false
            when (result.type) {
                WebView.HitTestResult.SRC_ANCHOR_TYPE,
                WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                    showLinkOptions(extra); true
                }
                WebView.HitTestResult.IMAGE_TYPE -> {
                    showImageOptions(extra); true
                }
                else -> false
            }
        }

        webView.webViewClient = object : WebViewClient() {

            override fun shouldInterceptRequest(
                view: WebView, request: WebResourceRequest
            ): WebResourceResponse? {
                val pageUrl = tabManager.getCurrentTab()?.url ?: ""
                val reqUrl = request.url.toString()
                if (adBlocker.isShieldsEnabled(pageUrl) && adBlocker.shouldBlock(reqUrl)) {
                    adBlocker.incrementBlockedCount()
                    runOnUiThread { updateShieldBadge() }
                    return WebResourceResponse("text/plain", "utf-8", null)
                }
                return null
            }

            override fun shouldOverrideUrlLoading(
                view: WebView, request: WebResourceRequest
            ): Boolean {
                val url = request.url.toString()
                if (url.startsWith("file://")) return false
                if (url.startsWith("javascript:") || url.startsWith("data:")) return false
                if (url.startsWith("http://")) {
                    val pageUrl = tabManager.getCurrentTab()?.url ?: ""
                    if (adBlocker.isShieldsEnabled(pageUrl) &&
                        adBlocker.isHttpsUpgradeEnabled(pageUrl)) {
                        loadUrl(url.replaceFirst("http://", "https://"))
                        return true
                    }
                }
                loadUrl(url)
                return true
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                adBlocker.resetBlockedCount()
                isReaderModeOn = false
                runOnUiThread { updateShieldBadge() }
                applyShieldSettings(url)
                val isNewTab = url.startsWith("file://")
                view.settings.setSupportZoom(!isNewTab)
                view.settings.builtInZoomControls = !isNewTab
                closeFindInPage()
            }

            override fun onPageFinished(view: WebView, url: String) {
                swipeRefresh.isRefreshing = false
                val isNewTab = url.startsWith("file://")
                val isHttps = url.startsWith("https://")
                addressBarRow.visibility = if (isNewTab) View.GONE else View.VISIBLE
                lockIcon.visibility = if (isHttps) View.VISIBLE else View.GONE
                val title = if (isNewTab) "New Tab" else (view.title ?: "New Tab")
                tabManager.getCurrentTab()?.title = title
                tabManager.getCurrentTab()?.url = url
                if (isNewTab) tabManager.getCurrentTab()?.favicon = null
                browserAdapter.notifyItemChanged(tabManager.getCurrentTabIndex())
                updateNavButtons()
                updateShieldIcon(url)
                if (isNewTab) {
                    addressBar.setText("")
                    addressBar.hint = "Search or type URL"
                    btnBookmark.setImageResource(R.drawable.ic_bookmark)
                    btnBookmark.alpha = 0.4f
                    loadFrequentSites()
                } else {
                    addressBar.setText(extractDomain(url))
                    updateBookmarkIcon(url)
                    if (adBlocker.isShieldsEnabled(url) &&
                        adBlocker.isFingerprintingBlocked(url)) {
                        view.evaluateJavascript(FP_JS, null)
                    }
                    injectNightMode(view)
                }
                if (!isNewTab && tabManager.getCurrentTab()?.isIncognito == false) {
                    historyDebounceRunnable?.let {
                        historyDebounceHandler.removeCallbacks(it)
                    }
                    historyDebounceRunnable = Runnable {
                        val currentUrl = webView.url ?: url
                        if (currentUrl != lastSavedHistoryUrl && currentUrl == url) {
                            lastSavedHistoryUrl = currentUrl
                            lifecycleScope.launch {
                                db.historyDao().insert(
                                    HistoryItem(
                                        title = view.title ?: "New Tab",
                                        url = currentUrl
                                    )
                                )
                            }
                        }
                    }
                    historyDebounceHandler.postDelayed(historyDebounceRunnable!!, 800)
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
                browserAdapter.notifyItemChanged(tabManager.getCurrentTabIndex())
            }
            override fun onPermissionRequest(request: PermissionRequest) {
                if (prefs.blockNotifications) request.deny()
                else request.grant(request.resources)
            }
        }

        webView.setFindListener { activeMatchOrdinal, numberOfMatches, _ ->
            findMatchCount.text = if (numberOfMatches == 0) "No matches"
            else "${activeMatchOrdinal + 1} / $numberOfMatches"
        }

        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimetype)
            request.addRequestHeader("User-Agent", userAgent)
            request.setDescription("Downloading file...")
            request.setTitle(fileName)
            request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, fileName
            )
            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            lifecycleScope.launch {
                db.downloadDao().insert(
                    DownloadItem(fileName = fileName, url = url, mimeType = mimetype)
                )
            }
            Toast.makeText(this, "Downloading: $fileName", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleReaderMode() {
        val currentUrl = tabManager.getCurrentTab()?.url ?: ""
        if (currentUrl.startsWith("file://")) {
            Toast.makeText(this, "Reader mode works on web pages",
                Toast.LENGTH_SHORT).show()
            return
        }
        if (!isReaderModeOn) {
            webView.evaluateJavascript(READER_MODE_JS, null)
            isReaderModeOn = true
            Toast.makeText(this, "Reader Mode ON", Toast.LENGTH_SHORT).show()
        } else {
            webView.reload()
            isReaderModeOn = false
            Toast.makeText(this, "Reader Mode OFF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fireButtonClearAll() {
        AlertDialog.Builder(this)
            .setTitle("🔥 Clear All Data")
            .setMessage("This will clear ALL history, bookmarks, downloads, cookies and cache. This cannot be undone.")
            .setPositiveButton("Clear Everything") { _, _ ->
                lifecycleScope.launch {
                    db.historyDao().clearAll()
                    db.bookmarkDao().deleteAll()
                    db.downloadDao().deleteAll()
                    runOnUiThread {
                        CookieManager.getInstance().removeAllCookies(null)
                        CookieManager.getInstance().flush()
                        WebStorage.getInstance().deleteAllData()
                        applicationContext.cacheDir.deleteRecursively()
                        Toast.makeText(this@MainActivity,
                            "🔥 All data cleared", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLinkOptions(url: String) {
        val options = arrayOf(
            "Open in New Tab", "Copy Link", "Share Link", "Download Link"
        )
        AlertDialog.Builder(this)
            .setTitle(extractDomain(url))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        tabManager.addTab(BrowserTab(url = url))
                        browserAdapter.notifyDataSetChanged()
                        tabRecycler.scrollToPosition(tabManager.tabs.lastIndex)
                        Toast.makeText(this, "Opened in new tab",
                            Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("link", url))
                        Toast.makeText(this, "Link copied", Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        startActivity(Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, url)
                            }, "Share link"
                        ))
                    }
                    3 -> {
                        val fileName = URLUtil.guessFileName(url, null, null)
                        val req = DownloadManager.Request(Uri.parse(url))
                        req.setTitle(fileName)
                        req.setDescription("Downloading...")
                        req.setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                        )
                        req.setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS, fileName
                        )
                        (getSystemService(DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
                        Toast.makeText(this, "Downloading: $fileName",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }.show()
    }

    private fun showImageOptions(url: String) {
        val options = arrayOf("Open Image", "Copy Image URL", "Download Image")
        AlertDialog.Builder(this)
            .setTitle("Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> loadUrl(url)
                    1 -> {
                        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("image url", url))
                        Toast.makeText(this, "Image URL copied",
                            Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        val fileName = URLUtil.guessFileName(url, null, "image/*")
                        val req = DownloadManager.Request(Uri.parse(url))
                        req.setTitle(fileName)
                        req.setDescription("Downloading image...")
                        req.setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                        )
                        req.setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS, fileName
                        )
                        (getSystemService(DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
                        Toast.makeText(this, "Downloading image",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }.show()
    }

    private fun setupTabGrid() {
        tabGridRecycler.layoutManager = GridLayoutManager(this, 2)
    }

    private fun openTabGrid() {
        refreshTabGrid()
        tabGridOverlay.visibility = View.VISIBLE
        hideKeyboard()
    }

    private fun closeTabGrid() {
        tabGridOverlay.visibility = View.GONE
    }

    private fun refreshTabGrid() {
        val count = tabManager.tabs.size
        tabCountLabel.text = "$count tab${if (count != 1) "s" else ""} open"
        tabGridRecycler.adapter = TabGridAdapter(
            tabs = tabManager.tabs,
            activeIndex = tabManager.getCurrentTabIndex(),
            onTabClick = { index ->
                tabManager.switchToTab(index)
                val url = tabManager.getCurrentTab()?.url ?: NEW_TAB_URL
                loadUrl(url)
                updateAddressBarForUrl(url)
                updateShieldIcon(url)
                browserAdapter.notifyDataSetChanged()
                closeTabGrid()
            },
            onTabClose = { index ->
                val tab = tabManager.tabs.getOrNull(index)
                if (tab != null && adBlocker.isForgetOnClose(tab.url)) {
                    CookieManager.getInstance().removeAllCookies(null)
                    CookieManager.getInstance().flush()
                }
                tabManager.removeTab(index)
                browserAdapter.notifyDataSetChanged()
                if (tabManager.tabs.isEmpty()) { addNewTab(); closeTabGrid() }
                else {
                    loadUrl(tabManager.getCurrentTab()?.url ?: NEW_TAB_URL)
                    refreshTabGrid()
                }
            }
        )
    }

    private fun updateAddressBarForUrl(url: String) {
        val isNewTab = url.startsWith("file://")
        addressBarRow.visibility = if (isNewTab) View.GONE else View.VISIBLE
        if (isNewTab) {
            addressBar.setText(""); addressBar.hint = "Search or type URL"
            lockIcon.visibility = View.GONE
            btnBookmark.setImageResource(R.drawable.ic_bookmark)
            btnBookmark.alpha = 0.4f
        } else {
            addressBar.setText(extractDomain(url))
            updateBookmarkIcon(url)
        }
    }

    private fun updateShieldBadge() {
        val count = adBlocker.getBlockedCount()
        if (count > 0) {
            shieldBadge.visibility = View.VISIBLE
            shieldBadge.text = if (count > 99) "99" else count.toString()
        } else shieldBadge.visibility = View.GONE
    }

    private fun updateShieldIcon(url: String) {
        btnShield.alpha = if (adBlocker.isShieldsEnabled(url)) 1.0f else 0.4f
    }

    private fun openFindInPage() {
        findInPageBar.visibility = View.VISIBLE
        findInput.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(findInput, InputMethodManager.SHOW_IMPLICIT)
        findInput.text.clear(); findMatchCount.text = ""
    }

    private fun closeFindInPage() {
        findInPageBar.visibility = View.GONE
        webView.clearMatches(); findInput.text.clear()
        findMatchCount.text = ""; hideKeyboard()
    }

    private fun updateBookmarkIcon(url: String) {
        lifecycleScope.launch {
            val isBookmarked = db.bookmarkDao().isBookmarked(url)
            btnBookmark.setImageResource(
                if (isBookmarked) R.drawable.ic_bookmark_filled
                else R.drawable.ic_bookmark
            )
            btnBookmark.alpha = 1.0f
        }
    }

    private fun setupRecyclerView() {
        browserAdapter = BrowserAdapter(
            tabs = tabManager.tabs,
            onTabClick = { index ->
                tabManager.switchToTab(index)
                val url = tabManager.getCurrentTab()?.url ?: NEW_TAB_URL
                updateAddressBarForUrl(url)
                updateShieldIcon(url)
                loadUrl(url)
            },
            onTabClose = { index ->
                val tab = tabManager.tabs.getOrNull(index)
                if (tab != null && adBlocker.isForgetOnClose(tab.url)) {
                    CookieManager.getInstance().removeAllCookies(null)
                    CookieManager.getInstance().flush()
                }
                tabManager.removeTab(index)
                browserAdapter.notifyDataSetChanged()
                if (tabManager.tabs.isEmpty()) addNewTab()
                else loadUrl(tabManager.getCurrentTab()?.url ?: NEW_TAB_URL)
            }
        )
        tabRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        tabRecycler.adapter = browserAdapter
    }

    private fun setupListeners() {
        addressBar.setOnFocusChangeListener { _, hasFocus ->
            val currentUrl = tabManager.getCurrentTab()?.url ?: ""
            if (hasFocus) {
                if (!currentUrl.startsWith("file://")) {
                    addressBar.setText(currentUrl); addressBar.selectAll()
                }
            } else {
                if (currentUrl.startsWith("file://")) addressBar.setText("")
                else addressBar.setText(extractDomain(currentUrl))
            }
        }

        addressBar.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val input = addressBar.text.toString().trim()
                if (input.isNotEmpty()) loadUrl(resolveUrl(input))
                hideKeyboard(); true
            } else false
        }

        btnBookmark.setOnClickListener {
            val currentUrl = tabManager.getCurrentTab()?.url ?: ""
            if (currentUrl.startsWith("file://")) return@setOnClickListener
            lifecycleScope.launch {
                val isBookmarked = db.bookmarkDao().isBookmarked(currentUrl)
                if (isBookmarked) {
                    db.bookmarkDao().deleteByUrl(currentUrl)
                    btnBookmark.setImageResource(R.drawable.ic_bookmark)
                    Toast.makeText(this@MainActivity,
                        "Bookmark removed", Toast.LENGTH_SHORT).show()
                } else {
                    val title = tabManager.getCurrentTab()?.title ?: currentUrl
                    db.bookmarkDao().insert(BookmarkItem(title = title, url = currentUrl))
                    btnBookmark.setImageResource(R.drawable.ic_bookmark_filled)
                    Toast.makeText(this@MainActivity,
                        "Bookmarked!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnShield.setOnClickListener {
            val currentUrl = tabManager.getCurrentTab()?.url ?: ""
            if (currentUrl.startsWith("file://")) {
                Toast.makeText(this, "Shields protect web pages",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            ShieldsPanel(this, currentUrl, adBlocker) {
                updateShieldIcon(currentUrl); webView.reload()
            }.show()
        }

        btnTabGrid.setOnClickListener { openTabGrid() }
        btnGridClose.setOnClickListener { closeTabGrid() }
        btnGridNewTab.setOnClickListener { addNewTab(); closeTabGrid() }
        btnGridAddNewTab.setOnClickListener { addNewTab(); closeTabGrid() }
        btnCloseAllTabs.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Close All Tabs")
                .setMessage("Close all ${tabManager.tabs.size} tabs?")
                .setPositiveButton("Close All") { _, _ ->
                    tabManager.tabs.clear()
                    browserAdapter.notifyDataSetChanged()
                    addNewTab(); closeTabGrid()
                }.setNegativeButton("Cancel", null).show()
        }

        btnBack.setOnClickListener { if (webView.canGoBack()) webView.goBack() }
        btnForward.setOnClickListener { if (webView.canGoForward()) webView.goForward() }
        btnRefresh.setOnClickListener { webView.reload() }
        btnHome.setOnClickListener { loadUrl(prefs.homepage) }
        btnNewTab.setOnClickListener { addNewTab() }

        btnIncognito.setOnClickListener {
            tabManager.toggleIncognito()
            val isIncognito = tabManager.getCurrentTab()?.isIncognito == true
            btnIncognito.alpha = if (isIncognito) 1.0f else 0.5f
            CookieManager.getInstance().setAcceptCookie(!isIncognito)
            Toast.makeText(this,
                if (isIncognito) "Incognito ON" else "Incognito OFF",
                Toast.LENGTH_SHORT).show()
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

        findInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s.toString().trim()
                if (q.isNotEmpty()) webView.findAllAsync(q)
                else { webView.clearMatches(); findMatchCount.text = "" }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        findInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                webView.findNext(true); true
            } else false
        }
        btnFindPrev.setOnClickListener { webView.findNext(false) }
        btnFindNext.setOnClickListener { webView.findNext(true) }
        btnFindClose.setOnClickListener { closeFindInPage() }

        btnMore.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add(0, 1, 0, "Bookmarks")
            popup.menu.add(0, 2, 0, "History")
            popup.menu.add(0, 3, 0, "Downloads")
            popup.menu.add(0, 4, 0, "Settings")
            popup.menu.add(0, 5, 0, "Share")
            popup.menu.add(0, 6, 0, "Find in page")
            popup.menu.add(0, 7, 0,
                if (isReaderModeOn) "Exit Reader Mode" else "Reader Mode")
            popup.menu.add(0, 8, 0, "🔥 Clear All Data")
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> { bookmarkLauncher.launch(
                        Intent(this, BookmarkActivity::class.java)); true }
                    2 -> { historyLauncher.launch(
                        Intent(this, HistoryActivity::class.java)); true }
                    3 -> { downloadLauncher.launch(
                        Intent(this, DownloadActivity::class.java)); true }
                    4 -> { settingsLauncher.launch(
                        Intent(this, SettingsActivity::class.java)); true }
                    5 -> {
                        val currentUrl = webView.url ?: ""
                        if (currentUrl.isEmpty() || currentUrl.startsWith("file://")) {
                            Toast.makeText(this, "Nothing to share",
                                Toast.LENGTH_SHORT).show()
                        } else {
                            startActivity(Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, currentUrl)
                                }, "Share URL"
                            ))
                        }
                        true
                    }
                    6 -> { openFindInPage(); true }
                    7 -> { toggleReaderMode(); true }
                    8 -> { fireButtonClearAll(); true }
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
        webView.settings.userAgentString = ""
        btnDesktop.alpha = 0.5f
        tabManager.addTab(BrowserTab(url = NEW_TAB_URL))
        browserAdapter.notifyDataSetChanged()
        loadUrl(NEW_TAB_URL)
        tabRecycler.scrollToPosition(tabManager.tabs.lastIndex)
    }

    private fun extractDomain(url: String): String = try {
        Uri.parse(url).host?.removePrefix("www.") ?: url
    } catch (e: Exception) { url }

    private fun resolveUrl(input: String): String = when {
        input.startsWith("http://") || input.startsWith("https://") -> input
        input.contains(".") && !input.contains(" ") -> "https://$input"
        else -> prefs.getSearchUrl(input)
    }

    private fun loadUrl(url: String) {
        tabManager.getCurrentTab()?.url = url
        webView.loadUrl(url)
        val isNewTab = url.startsWith("file://")
        addressBarRow.visibility = if (isNewTab) View.GONE else View.VISIBLE
        if (isNewTab) {
            addressBar.setText(""); addressBar.hint = "Search or type URL"
            lockIcon.visibility = View.GONE
            btnBookmark.setImageResource(R.drawable.ic_bookmark)
            btnBookmark.alpha = 0.4f
        } else addressBar.setText(extractDomain(url))
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (tabGridOverlay.visibility == View.VISIBLE) {
                closeTabGrid(); return true
            }
            if (findInPageBar.visibility == View.VISIBLE) {
                closeFindInPage(); return true
            }
            if (webView.canGoBack()) { webView.goBack(); return true }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        historyDebounceRunnable?.let { historyDebounceHandler.removeCallbacks(it) }
        super.onDestroy()
    }
}
