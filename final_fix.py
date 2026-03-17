import re

BASE = 'app/src/main/java/com/brahmanlabs/browser/'
RES  = 'app/src/main/res/'

# ══════════════════════════════════════════════
# 1. BrowserAdapter.kt — full rewrite with
#    activeIndex + active tab indicator
# ══════════════════════════════════════════════
with open(BASE + 'BrowserAdapter.kt', 'w') as f:
    f.write('''package com.brahmanlabs.browser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BrowserAdapter(
    private val tabs: List<BrowserTab>,
    private val onTabClick: (Int) -> Unit,
    private val onTabClose: (Int) -> Unit,
    private var activeIndex: Int = 0
) : RecyclerView.Adapter<BrowserAdapter.TabViewHolder>() {

    class TabViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tabTitle)
        val closeBtn: ImageButton = view.findViewById(R.id.btnCloseTab)
        val favicon: ImageView = view.findViewById(R.id.tabFavicon)
        val activeBar: View = view.findViewById(R.id.tabActiveBar)
    }

    fun setActiveIndex(index: Int) {
        val old = activeIndex
        activeIndex = index
        if (old != index) {
            notifyItemChanged(old)
            notifyItemChanged(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.tab_item, parent, false)
        return TabViewHolder(view)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        val tab = tabs[position]
        val isActive = position == activeIndex
        holder.title.text = tab.title.ifEmpty { "New Tab" }
        holder.activeBar.visibility = if (isActive) View.VISIBLE else View.INVISIBLE
        holder.itemView.alpha = if (isActive) 1.0f else 0.6f
        holder.title.setTextColor(
            if (tab.isIncognito)
                holder.itemView.context.getColor(R.color.waterAqua)
            else
                holder.itemView.context.getColor(R.color.textSecondary)
        )
        holder.itemView.setOnClickListener { onTabClick(position) }
        holder.closeBtn.setOnClickListener { onTabClose(position) }
        if (tab.favicon != null) {
            holder.favicon.setImageBitmap(tab.favicon)
        } else {
            holder.favicon.setImageResource(
                if (tab.url.startsWith("file://")) R.drawable.ic_home
                else R.drawable.ic_language
            )
        }
    }

    override fun getItemCount(): Int = tabs.size
}
''')
print("✅ 1. BrowserAdapter.kt rewritten")

# ══════════════════════════════════════════════
# 2. tab_item.xml — add active indicator bar
# ══════════════════════════════════════════════
with open(RES + 'layout/tab_item.xml', 'w') as f:
    f.write('''<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="130dp"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@drawable/bg_tab_item"
    android:layout_marginEnd="5dp">

    <View android:id="@+id/tabActiveBar"
        android:layout_width="match_parent"
        android:layout_height="2dp"
        android:background="@color/waterCyan"
        android:visibility="invisible"/>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:paddingStart="8dp"
        android:paddingEnd="4dp">

        <ImageView android:id="@+id/tabFavicon"
            android:layout_width="14dp" android:layout_height="14dp"
            android:layout_marginEnd="5dp"
            android:src="@drawable/ic_home"
            android:scaleType="fitCenter"
            android:contentDescription="Favicon"/>

        <TextView android:id="@+id/tabTitle"
            android:layout_width="0dp" android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="New Tab"
            android:textColor="@color/textSecondary"
            android:textSize="11sp"
            android:maxLines="1"
            android:ellipsize="end"/>

        <ImageButton android:id="@+id/btnCloseTab"
            android:layout_width="22dp" android:layout_height="22dp"
            android:background="@android:color/transparent"
            android:src="@drawable/ic_close_tab"
            android:padding="4dp"
            android:contentDescription="Close Tab"/>

    </LinearLayout>
</LinearLayout>
''')
print("✅ 2. tab_item.xml rewritten with active indicator")

# ══════════════════════════════════════════════
# 3. activity_main.xml — add tabCountBadge,
#    remove topGlow, move progress bar to top,
#    toolbar overlays webview
# ══════════════════════════════════════════════
with open(RES + 'layout/activity_main.xml', 'w') as f:
    f.write('''<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:clipChildren="false"
    android:clipToPadding="false"
    android:background="@drawable/bg_water_gradient">

    <!-- WEBVIEW fills full screen; toolbars overlay on top like Brave -->
    <androidx.swiperefreshlayout.widget.SwipeRefreshLayout
        android:id="@+id/swipeRefresh"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_above="@id/navigationBar">
        <FrameLayout
            android:id="@+id/webViewContainer"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
    </androidx.swiperefreshlayout.widget.SwipeRefreshLayout>

    <!-- NAVIGATION BAR — fixed at bottom -->
    <LinearLayout android:id="@+id/navigationBar"
        android:layout_width="match_parent" android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:orientation="horizontal" android:gravity="center"
        android:paddingStart="16dp" android:paddingEnd="16dp"
        android:paddingTop="6dp" android:paddingBottom="6dp"
        android:background="@drawable/bg_top_bar"
        android:elevation="8dp">
        <LinearLayout android:layout_width="match_parent" android:layout_height="52dp"
            android:orientation="horizontal" android:gravity="center"
            android:background="@drawable/bg_nav_bar"
            android:paddingStart="4dp" android:paddingEnd="4dp">
            <ImageButton android:id="@+id/btnBack"
                android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="1"
                android:background="@drawable/bg_nav_button" android:src="@drawable/ic_back"
                android:padding="11dp" android:alpha="0.4" android:contentDescription="Back"/>
            <ImageButton android:id="@+id/btnForward"
                android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="1"
                android:background="@drawable/bg_nav_button" android:src="@drawable/ic_forward"
                android:padding="11dp" android:alpha="0.4" android:contentDescription="Forward"/>
            <ImageButton android:id="@+id/btnHome"
                android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="1"
                android:background="@drawable/bg_nav_button" android:src="@drawable/ic_home"
                android:padding="10dp" android:contentDescription="Home"/>
            <ImageButton android:id="@+id/btnRefresh"
                android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="1"
                android:background="@drawable/bg_nav_button" android:src="@drawable/ic_refresh"
                android:padding="11dp" android:contentDescription="Refresh"/>
            <ImageButton android:id="@+id/btnFullscreen"
                android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="1"
                android:background="@drawable/bg_nav_button" android:src="@drawable/ic_fullscreen"
                android:padding="11dp" android:contentDescription="Full Screen"/>
        </LinearLayout>
    </LinearLayout>

    <!-- PROGRESS BAR — thin line at very top like Brave -->
    <ProgressBar android:id="@+id/progressBar"
        style="?android:attr/progressBarStyleHorizontal"
        android:layout_width="match_parent" android:layout_height="3dp"
        android:layout_alignParentTop="true"
        android:progressDrawable="@drawable/progress_water"
        android:progress="0" android:visibility="gone"
        android:elevation="30dp"/>

    <!-- TOP BAR — overlays webview -->
    <LinearLayout android:id="@+id/topBar"
        android:layout_width="match_parent" android:layout_height="wrap_content"
        android:layout_alignParentTop="true" android:orientation="vertical"
        android:paddingTop="6dp" android:paddingBottom="2dp"
        android:paddingStart="10dp" android:paddingEnd="10dp"
        android:elevation="8dp">
        <RelativeLayout android:layout_width="match_parent" android:layout_height="42dp">
            <LinearLayout android:id="@+id/actionButtons"
                android:layout_width="wrap_content" android:layout_height="match_parent"
                android:layout_alignParentEnd="true"
                android:orientation="horizontal" android:gravity="center_vertical">
                <ImageButton android:id="@+id/btnBookmark"
                    android:layout_width="36dp" android:layout_height="36dp"
                    android:background="@drawable/bg_nav_button" android:src="@drawable/ic_bookmark"
                    android:padding="8dp" android:alpha="0.4" android:contentDescription="Bookmark"/>
                <ImageButton android:id="@+id/btnIncognito"
                    android:layout_width="36dp" android:layout_height="36dp"
                    android:background="@drawable/bg_nav_button" android:src="@drawable/ic_incognito"
                    android:padding="8dp" android:alpha="0.5" android:contentDescription="Incognito"/>
                <ImageButton android:id="@+id/btnDesktop"
                    android:layout_width="36dp" android:layout_height="36dp"
                    android:background="@drawable/bg_nav_button" android:src="@drawable/ic_desktop"
                    android:padding="8dp" android:alpha="0.5" android:contentDescription="Desktop Mode"/>
                <FrameLayout android:layout_width="36dp" android:layout_height="36dp">
                    <ImageButton android:id="@+id/btnShield"
                        android:layout_width="36dp" android:layout_height="36dp"
                        android:background="@drawable/bg_nav_button"
                        android:src="@drawable/ic_brahman_shield" android:padding="7dp"
                        android:contentDescription="Brahman Shields"/>
                    <TextView android:id="@+id/shieldBadge"
                        android:layout_width="14dp" android:layout_height="14dp"
                        android:layout_gravity="top|end"
                        android:layout_marginTop="1dp" android:layout_marginEnd="1dp"
                        android:background="@drawable/bg_badge"
                        android:textColor="#FFFFFF" android:textSize="7sp"
                        android:gravity="center" android:visibility="gone"/>
                </FrameLayout>
                <ImageButton android:id="@+id/btnMore"
                    android:layout_width="36dp" android:layout_height="36dp"
                    android:background="@drawable/bg_nav_button" android:src="@drawable/ic_more"
                    android:padding="8dp" android:contentDescription="More Options"/>
            </LinearLayout>
            <LinearLayout android:id="@+id/addressBarRow"
                android:layout_width="match_parent" android:layout_height="match_parent"
                android:layout_toStartOf="@id/actionButtons" android:layout_marginEnd="4dp"
                android:orientation="horizontal" android:gravity="center_vertical"
                android:background="@drawable/bg_address_bar"
                android:paddingStart="10dp" android:paddingEnd="4dp">
                <ImageView android:id="@+id/lockIcon"
                    android:layout_width="14dp" android:layout_height="14dp"
                    android:src="@drawable/ic_lock" android:contentDescription="Secure"
                    android:visibility="gone" android:layout_marginEnd="4dp"/>
                <EditText android:id="@+id/addressBar"
                    android:layout_width="0dp" android:layout_height="match_parent"
                    android:layout_weight="1" android:hint="Search or type URL"
                    android:textColorHint="#80CAF0F8" android:inputType="textUri"
                    android:singleLine="true" android:imeOptions="actionGo"
                    android:background="@android:color/transparent"
                    android:textColor="#FFFFFF" android:textSize="13sp"/>
                <ImageButton android:id="@+id/btnMic"
                    android:layout_width="32dp" android:layout_height="32dp"
                    android:background="@drawable/bg_nav_button" android:src="@drawable/ic_mic"
                    android:padding="8dp" android:alpha="0.7"
                    android:contentDescription="Voice Search"/>
            </LinearLayout>
        </RelativeLayout>
    </LinearLayout>

    <!-- TABS ROW — overlays webview, below topBar -->
    <LinearLayout android:id="@+id/tabsRow"
        android:layout_width="match_parent" android:layout_height="46dp"
        android:layout_below="@id/topBar"
        android:orientation="horizontal" android:gravity="center_vertical"
        android:paddingStart="8dp" android:paddingEnd="8dp"
        android:paddingTop="3dp" android:paddingBottom="3dp"
        android:elevation="8dp">
        <androidx.recyclerview.widget.RecyclerView android:id="@+id/tabRecycler"
            android:layout_width="0dp" android:layout_height="match_parent"
            android:layout_weight="1" android:clipToPadding="false"/>
        <FrameLayout android:layout_width="36dp" android:layout_height="36dp"
            android:layout_marginEnd="4dp">
            <ImageButton android:id="@+id/btnTabGrid"
                android:layout_width="36dp" android:layout_height="36dp"
                android:background="@drawable/bg_tab_item"
                android:src="@drawable/ic_tab_grid" android:padding="9dp"
                android:contentDescription="Tab Grid"/>
            <TextView android:id="@+id/tabCountBadge"
                android:layout_width="16dp" android:layout_height="16dp"
                android:layout_gravity="top|end"
                android:layout_marginTop="1dp" android:layout_marginEnd="1dp"
                android:background="@drawable/bg_badge"
                android:textColor="#FFFFFF" android:textSize="8sp"
                android:gravity="center" android:text="1"/>
        </FrameLayout>
        <ImageButton android:id="@+id/btnNewTab"
            android:layout_width="36dp" android:layout_height="36dp"
            android:background="@drawable/bg_tab_item"
            android:src="@drawable/ic_add_tab" android:padding="9dp"
            android:contentDescription="New Tab"/>
    </LinearLayout>

    <!-- SUGGESTIONS -->
    <LinearLayout android:id="@+id/suggestionsPanel"
        android:layout_width="match_parent" android:layout_height="wrap_content"
        android:layout_below="@id/topBar"
        android:orientation="vertical"
        android:background="@drawable/bg_shields_panel"
        android:elevation="20dp" android:visibility="gone"
        android:layout_marginStart="8dp" android:layout_marginEnd="8dp">
        <androidx.recyclerview.widget.RecyclerView android:id="@+id/suggestionsRecycler"
            android:layout_width="match_parent" android:layout_height="wrap_content"
            android:nestedScrollingEnabled="false" android:maxHeight="250dp"/>
    </LinearLayout>

    <!-- FIND IN PAGE BAR -->
    <LinearLayout android:id="@+id/findInPageBar"
        android:layout_width="match_parent" android:layout_height="48dp"
        android:layout_above="@id/navigationBar"
        android:orientation="horizontal" android:gravity="center_vertical"
        android:background="@drawable/bg_top_bar"
        android:paddingStart="8dp" android:paddingEnd="8dp"
        android:elevation="8dp" android:visibility="gone">
        <EditText android:id="@+id/findInput"
            android:layout_width="0dp" android:layout_height="38dp" android:layout_weight="1"
            android:hint="Find in page..." android:textColorHint="#80CAF0F8"
            android:textColor="#FFFFFF" android:textSize="14sp" android:inputType="text"
            android:singleLine="true" android:imeOptions="actionSearch"
            android:background="@drawable/bg_address_bar"
            android:paddingStart="10dp" android:paddingEnd="10dp"/>
        <TextView android:id="@+id/findMatchCount"
            android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:textColor="#8090E0EF" android:textSize="12sp"
            android:layout_marginStart="6dp" android:text=""/>
        <ImageButton android:id="@+id/btnFindPrev"
            android:layout_width="36dp" android:layout_height="36dp"
            android:background="@drawable/bg_nav_button" android:src="@drawable/ic_back"
            android:padding="10dp" android:layout_marginStart="4dp"
            android:contentDescription="Previous"/>
        <ImageButton android:id="@+id/btnFindNext"
            android:layout_width="36dp" android:layout_height="36dp"
            android:background="@drawable/bg_nav_button" android:src="@drawable/ic_forward"
            android:padding="10dp" android:layout_marginStart="4dp"
            android:contentDescription="Next"/>
        <ImageButton android:id="@+id/btnFindClose"
            android:layout_width="36dp" android:layout_height="36dp"
            android:background="@drawable/bg_nav_button" android:src="@drawable/ic_close"
            android:padding="10dp" android:layout_marginStart="4dp"
            android:contentDescription="Close"/>
    </LinearLayout>

    <!-- FULLSCREEN VIDEO -->
    <FrameLayout android:id="@+id/fullscreenVideoContainer"
        android:layout_width="match_parent" android:layout_height="match_parent"
        android:background="#000000" android:visibility="gone" android:elevation="100dp"/>

    <!-- TAB GRID OVERLAY -->
    <RelativeLayout android:id="@+id/tabGridOverlay"
        android:layout_width="match_parent" android:layout_height="match_parent"
        android:background="@drawable/bg_water_gradient"
        android:elevation="50dp" android:visibility="gone">
        <RelativeLayout android:id="@+id/gridTopBar"
            android:layout_width="match_parent" android:layout_height="56dp"
            android:layout_alignParentTop="true"
            android:background="@drawable/bg_top_bar"
            android:paddingStart="12dp" android:paddingEnd="12dp">
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:layout_centerInParent="true" android:text="All Tabs"
                android:textColor="@color/textPrimary" android:textSize="18sp"
                android:textStyle="bold"/>
            <ImageButton android:id="@+id/btnGridClose"
                android:layout_width="40dp" android:layout_height="40dp"
                android:layout_alignParentEnd="true" android:layout_centerVertical="true"
                android:background="@drawable/bg_nav_button" android:src="@drawable/ic_close"
                android:padding="10dp" android:contentDescription="Close grid"/>
            <ImageButton android:id="@+id/btnGridNewTab"
                android:layout_width="40dp" android:layout_height="40dp"
                android:layout_alignParentStart="true" android:layout_centerVertical="true"
                android:background="@drawable/bg_nav_button" android:src="@drawable/ic_add_tab"
                android:padding="10dp" android:contentDescription="New tab"/>
        </RelativeLayout>
        <TextView android:id="@+id/tabCountLabel"
            android:layout_width="match_parent" android:layout_height="wrap_content"
            android:layout_below="@id/gridTopBar"
            android:layout_marginTop="8dp" android:layout_marginBottom="4dp"
            android:gravity="center" android:textColor="@color/textHint" android:textSize="12sp"/>
        <androidx.recyclerview.widget.RecyclerView android:id="@+id/tabGridRecycler"
            android:layout_width="match_parent" android:layout_height="match_parent"
            android:layout_below="@id/tabCountLabel" android:layout_above="@id/gridBottomBar"
            android:paddingStart="6dp" android:paddingEnd="6dp"
            android:paddingTop="4dp" android:clipToPadding="false"/>
        <LinearLayout android:id="@+id/gridBottomBar"
            android:layout_width="match_parent" android:layout_height="wrap_content"
            android:layout_alignParentBottom="true"
            android:orientation="horizontal" android:gravity="center"
            android:background="@drawable/bg_top_bar"
            android:paddingTop="8dp" android:paddingBottom="8dp"
            android:paddingStart="16dp" android:paddingEnd="16dp">
            <Button android:id="@+id/btnCloseAllTabs"
                android:layout_width="0dp" android:layout_height="40dp"
                android:layout_weight="1" android:layout_marginEnd="8dp"
                android:text="Close All" android:textColor="@color/insecureRed"
                android:textSize="13sp" android:background="@drawable/bg_nav_button"/>
            <Button android:id="@+id/btnGridAddNewTab"
                android:layout_width="0dp" android:layout_height="40dp"
                android:layout_weight="1" android:layout_marginStart="8dp"
                android:text="+ New Tab" android:textColor="@color/waterCyan"
                android:textSize="13sp" android:background="@drawable/bg_nav_button"/>
        </LinearLayout>
    </RelativeLayout>

</RelativeLayout>
''')
print("✅ 3. activity_main.xml rewritten — overlay layout + tab badge + no topGlow")

# ══════════════════════════════════════════════
# 4. SessionManager.kt — never save incognito
# ══════════════════════════════════════════════
with open(BASE + 'SessionManager.kt', 'w') as f:
    f.write('''package com.brahmanlabs.browser
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object SessionManager {
    private const val PREF_NAME = "brahman_session"
    private const val KEY_TABS = "saved_tabs"
    private const val KEY_ACTIVE_INDEX = "active_index"

    fun saveSession(context: Context, tabs: List<BrowserTab>, activeIndex: Int) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val normalTabs = tabs.filter { !it.isIncognito }
            val jsonArray = JSONArray()
            normalTabs.forEach { tab ->
                if (!tab.url.startsWith("file://")) {
                    val obj = JSONObject()
                    obj.put("url", tab.url)
                    obj.put("title", tab.title)
                    jsonArray.put(obj)
                }
            }
            prefs.edit()
                .putString(KEY_TABS, jsonArray.toString())
                .putInt(KEY_ACTIVE_INDEX, activeIndex.coerceIn(0, (normalTabs.size - 1).coerceAtLeast(0)))
                .apply()
        } catch (e: Exception) { }
    }

    fun loadSession(context: Context): Pair<List<BrowserTab>, Int> {
        return try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY_TABS, null) ?: return Pair(emptyList(), 0)
            val activeIndex = prefs.getInt(KEY_ACTIVE_INDEX, 0)
            val jsonArray = JSONArray(json)
            val tabs = mutableListOf<BrowserTab>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                tabs.add(BrowserTab(
                    url = obj.getString("url"),
                    title = obj.optString("title", "")
                ))
            }
            Pair(tabs, activeIndex.coerceIn(0, (tabs.size - 1).coerceAtLeast(0)))
        } catch (e: Exception) { Pair(emptyList(), 0) }
    }

    fun clearSession(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
''')
print("✅ 4. SessionManager.kt — incognito tabs never saved")

# ══════════════════════════════════════════════
# 5. MainActivity.kt — precise patches only
# ══════════════════════════════════════════════
path = BASE + 'MainActivity.kt'
with open(path, 'r') as f: c = f.read()

def patch(old, new, label):
    global c
    if old in c:
        c = c.replace(old, new, 1)
        print(f"✅ 5.{label}")
    else:
        print(f"⏭  5.{label} already applied or not found")

# a. Add ValueAnimator import
patch(
    'import androidx.appcompat.app.AppCompatDelegate\n',
    'import android.animation.ValueAnimator\nimport androidx.appcompat.app.AppCompatDelegate\n',
    'a. ValueAnimator import'
)

# b. Add tabCountBadge field
patch(
    '    private lateinit var suggestionsRecycler: RecyclerView\n    private lateinit var suggestionAdapter: SuggestionAdapter\n\n',
    '    private lateinit var suggestionsRecycler: RecyclerView\n    private lateinit var suggestionAdapter: SuggestionAdapter\n    private lateinit var tabCountBadge: android.widget.TextView\n\n',
    'b. tabCountBadge field'
)

# c. Safe browsing
patch(
    '            blockNetworkImage = prefs.dataSaverEnabled\n            loadsImagesAutomatically = !prefs.dataSaverEnabled\n        }',
    '            blockNetworkImage = prefs.dataSaverEnabled\n            loadsImagesAutomatically = !prefs.dataSaverEnabled\n            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {\n                safeBrowsingEnabled = true\n            }\n        }',
    'c. Safe browsing'
)

# d. onCreateWindow — target=_blank opens new tab
patch(
    '            override fun onShowCustomView(view: View, callback: CustomViewCallback) {',
    '''            override fun onCreateWindow(
                view: WebView, isDialog: Boolean, isUserGesture: Boolean,
                resultMsg: android.os.Message?
            ): Boolean {
                if (!isUserGesture) return false
                val newWv = createWebView()
                val tab = BrowserTab(
                    url = "",
                    isIncognito = tabManager.getCurrentTab()?.isIncognito == true,
                    webView = newWv
                )
                tabManager.addTab(tab)
                browserAdapter.notifyItemInserted(tabManager.tabs.lastIndex)
                tabRecycler.scrollToPosition(tabManager.tabs.lastIndex)
                getCurrentWebView()?.visibility = View.GONE
                newWv.visibility = View.VISIBLE
                val transport = resultMsg?.obj as? WebView.WebViewTransport
                transport?.webView = newWv
                resultMsg?.sendToTarget()
                updateNavButtons()
                return true
            }

            override fun onShowCustomView(view: View, callback: CustomViewCallback) {''',
    'd. onCreateWindow target=_blank'
)

# e. Fix updateUIForUrl — address bar ALWAYS visible
patch(
    '        val isNewTab = url.startsWith("file://")\n        addressBarRow.visibility = if (isNewTab) View.GONE else View.VISIBLE\n        if (isNewTab) {\n            addressBar.clearFocus()\n            addressBar.setText("")\n            addressBar.hint = "Search or type URL"\n            lockIcon.visibility = View.GONE\n            btnBookmark.setImageResource(R.drawable.ic_bookmark)\n            btnBookmark.alpha = 0.4f\n        } else {\n            setAddressBarSilently(extractDomain(url))\n        }',
    '        val isNewTab = url.startsWith("file://")\n        addressBarRow.visibility = View.VISIBLE\n        if (isNewTab) {\n            addressBar.clearFocus()\n            setAddressBarSilently("")\n            addressBar.hint = "Search or type URL"\n            lockIcon.visibility = View.GONE\n            btnBookmark.setImageResource(R.drawable.ic_bookmark)\n            btnBookmark.alpha = 0.4f\n        } else {\n            setAddressBarSilently(extractDomain(url))\n        }',
    'e. Address bar always visible'
)

# f. openTab — notifyItemInserted
patch(
    '        tabManager.addTab(tab)\n        browserAdapter.notifyDataSetChanged()\n        tabRecycler.scrollToPosition(tabManager.tabs.lastIndex)',
    '        tabManager.addTab(tab)\n        browserAdapter.notifyItemInserted(tabManager.tabs.lastIndex)\n        tabRecycler.scrollToPosition(tabManager.tabs.lastIndex)\n        updateNavButtons()',
    'f. openTab notifyItemInserted'
)

# g. closeTab — notifyItemRemoved
patch(
    '        tabManager.removeTab(index)\n        browserAdapter.notifyDataSetChanged()',
    '        tabManager.removeTab(index)\n        browserAdapter.notifyItemRemoved(index)\n        browserAdapter.notifyItemRangeChanged(index, tabManager.tabs.size)',
    'g. closeTab notifyItemRemoved'
)

# h. onPause — exclude incognito from session save
patch(
    '        if (tabManager.tabs.isNotEmpty()) {\n            SessionManager.saveSession(\n                this, tabManager.tabs, tabManager.getCurrentTabIndex())\n        }',
    '        val normalTabs = tabManager.tabs.filter { !it.isIncognito }\n        if (normalTabs.isNotEmpty()) {\n            SessionManager.saveSession(this, normalTabs, tabManager.getCurrentTabIndex())\n        }',
    'h. Session save excludes incognito'
)

# i. updateNavButtons — update badge + setActiveIndex
patch(
    '    private fun updateNavButtons() {\n        val wv = getCurrentWebView()\n        btnBack.alpha = if (wv?.canGoBack() == true) 1.0f else 0.4f\n        btnForward.alpha = if (wv?.canGoForward() == true) 1.0f else 0.4f\n        // Update tab count on grid button like Brave\n        val count = tabManager.tab',
    '    private fun updateNavButtons() {\n        val wv = getCurrentWebView()\n        btnBack.alpha = if (wv?.canGoBack() == true) 1.0f else 0.4f\n        btnForward.alpha = if (wv?.canGoForward() == true) 1.0f else 0.4f\n        val count = tabManager.tab',
    'i. updateNavButtons cleanup'
)

# Full updateNavButtons replacement
old_nav = '''    private fun updateNavButtons() {
        val wv = getCurrentWebView()
        btnBack.alpha = if (wv?.canGoBack() == true) 1.0f else 0.4f
        btnForward.alpha = if (wv?.canGoForward() == true) 1.0f else 0.4f
        val count = tabManager.tabs.size
        runOnUiThread {
            try {
                val tag = btnTabGrid.tag
                if (tag == null || tag != count) {
                    btnTabGrid.tag = count
                    // Update tab count label if we add one later
                }
            } catch (e: Exception) {}
        }
    }'''
new_nav = '''    private fun updateNavButtons() {
        val wv = getCurrentWebView()
        btnBack.alpha = if (wv?.canGoBack() == true) 1.0f else 0.4f
        btnForward.alpha = if (wv?.canGoForward() == true) 1.0f else 0.4f
        val count = tabManager.tabs.size
        if (::tabCountBadge.isInitialized) {
            tabCountBadge.text = if (count > 99) "99" else count.toString()
        }
        browserAdapter.setActiveIndex(tabManager.getCurrentTabIndex())
    }'''
patch(old_nav, new_nav, 'i2. updateNavButtons full replacement')

# j. Bind tabCountBadge in bindViews
patch(
    '        suggestionsPanel = findViewById(R.id.suggestionsPanel)\n        suggestionsRecycler = findViewById(R.id.suggestionsRecycler)\n    }',
    '        suggestionsPanel = findViewById(R.id.suggestionsPanel)\n        suggestionsRecycler = findViewById(R.id.suggestionsRecycler)\n        tabCountBadge = findViewById(R.id.tabCountBadge)\n    }',
    'j. Bind tabCountBadge'
)

# k. setupRecyclerView — pass activeIndex
patch(
    '        browserAdapter = BrowserAdapter(\n            tabs = tabManager.tabs,\n            onTabClick = { index -> switchToExistingTab(index) },\n            onTabClose = { index -> closeTab(index) }\n        )',
    '        browserAdapter = BrowserAdapter(\n            tabs = tabManager.tabs,\n            onTabClick = { index -> switchToExistingTab(index) },\n            onTabClose = { index -> closeTab(index) },\n            activeIndex = tabManager.getCurrentTabIndex()\n        )',
    'k. BrowserAdapter with activeIndex'
)

# l. setupWindowInsets — set webview padding for toolbar height
patch(
    '    private fun setupWindowInsets() {\n        ViewCompat.setOnApplyWindowInsetsListener(navigationBar) { view, insets ->\n            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())\n            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {\n                bottomMargin = systemBars.bottom + 4\n            }\n            insets\n        }\n    }',
    '''    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(navigationBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom + 4
            }
            insets
        }
        topBar.post {
            toolbarTopOffset = topBar.height + tabsRow.height
            webViewContainer.setPadding(0, toolbarTopOffset, 0, 0)
        }
    }''',
    'l. WebView padding for toolbar height'
)

# m. Add ValueAnimator to hideToolbarsAnimated if missing
if 'ValueAnimator' not in c:
    patch(
        '    private fun hideToolbarsAnimated() {\n        if (toolbarHidden || isFullscreen) return\n        toolbarHidden = true\n        val h = -(topBar.height + tabsRow.height).toFloat()\n        topBar.animate().translationY(h).setDuration(220)\n            .withEndAction { if (toolbarHidden) topBar.visibility = View.INVISIBLE }\n            .start()\n        tabsRow.animate().translationY(h).setDuration(220).start()\n    }',
        '''    private fun hideToolbarsAnimated() {
        if (toolbarHidden || isFullscreen || toolbarTopOffset == 0) return
        toolbarHidden = true
        val h = toolbarTopOffset.toFloat()
        topBar.animate().translationY(-h).setDuration(200).start()
        tabsRow.animate().translationY(-h).setDuration(200)
            .withEndAction { if (toolbarHidden) topBar.visibility = View.INVISIBLE }.start()
        ValueAnimator.ofInt(toolbarTopOffset, 0).apply {
            duration = 200
            addUpdateListener { webViewContainer.setPadding(0, it.animatedValue as Int, 0, 0) }
            start()
        }
    }''',
        'm. hideToolbarsAnimated with ValueAnimator'
    )
    patch(
        '    private fun showToolbarsAnimated() {\n        if (!toolbarHidden) return\n        toolbarHidden = false\n        topBar.visibility = View.VISIBLE\n        topBar.animate().translationY(0f).setDuration(220).start()\n        tabsRow.animate().translationY(0f).setDuration(220).start()\n    }',
        '''    private fun showToolbarsAnimated() {
        if (!toolbarHidden) return
        toolbarHidden = false
        topBar.visibility = View.VISIBLE
        topBar.animate().translationY(0f).setDuration(200).start()
        tabsRow.animate().translationY(0f).setDuration(200).start()
        ValueAnimator.ofInt(0, toolbarTopOffset).apply {
            duration = 200
            addUpdateListener { webViewContainer.setPadding(0, it.animatedValue as Int, 0, 0) }
            start()
        }
    }''',
        'm2. showToolbarsAnimated with ValueAnimator'
    )

# n. forceShowToolbars — update padding too
patch(
    '    private fun forceShowToolbars() {\n        toolbarHidden = false\n        topBar.visibility = View.VISIBLE\n        topBar.translationY = 0f\n        tabsRow.translationY = 0f\n    }',
    '''    private fun forceShowToolbars() {
        toolbarHidden = false
        topBar.visibility = View.VISIBLE
        topBar.translationY = 0f
        tabsRow.translationY = 0f
        if (toolbarTopOffset > 0) webViewContainer.setPadding(0, toolbarTopOffset, 0, 0)
    }''',
    'n. forceShowToolbars updates padding'
)

with open(path, 'w') as f: f.write(c)
print("\n✅ All patches written to MainActivity.kt")
print("""
═══════════════════════════════════════════
🌊 ALL FIXES APPLIED. Build:
   ./gradlew assembleDebug --no-daemon
═══════════════════════════════════════════
""")
