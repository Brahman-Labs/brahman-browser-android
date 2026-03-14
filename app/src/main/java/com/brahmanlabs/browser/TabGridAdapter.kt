package com.brahmanlabs.browser

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TabGridAdapter(
    private val tabs: List<BrowserTab>,
    private val activeIndex: Int,
    private val onTabClick: (Int) -> Unit,
    private val onTabClose: (Int) -> Unit
) : RecyclerView.Adapter<TabGridAdapter.GridViewHolder>() {

    class GridViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val favicon: ImageView = view.findViewById(R.id.tabGridFavicon)
        val title: TextView = view.findViewById(R.id.tabGridTitle)
        val url: TextView = view.findViewById(R.id.tabGridUrl)
        val closeBtn: ImageButton = view.findViewById(R.id.btnCloseTabGrid)
        val incognitoLabel: TextView = view.findViewById(R.id.tabGridIncognito)
        val activeIndicator: View = view.findViewById(R.id.tabActiveIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tab_grid, parent, false)
        return GridViewHolder(view)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val tab = tabs[position]

        holder.title.text = tab.title.ifEmpty { "New Tab" }

        val isNewTab = tab.url.startsWith("file://")
        holder.url.text = if (isNewTab) "New Tab" else try {
            Uri.parse(tab.url).host?.removePrefix("www.") ?: tab.url
        } catch (e: Exception) { tab.url }

        if (tab.favicon != null) {
            holder.favicon.setImageBitmap(tab.favicon)
        } else {
            holder.favicon.setImageResource(
                if (isNewTab) R.drawable.ic_add_tab else R.drawable.ic_home
            )
        }

        holder.incognitoLabel.visibility =
            if (tab.isIncognito) View.VISIBLE else View.GONE

        holder.activeIndicator.visibility =
            if (position == activeIndex) View.VISIBLE else View.GONE

        // Highlight active card
        holder.itemView.alpha = if (position == activeIndex) 1.0f else 0.75f

        holder.itemView.setOnClickListener { onTabClick(position) }
        holder.closeBtn.setOnClickListener { onTabClose(position) }
    }

    override fun getItemCount(): Int = tabs.size
}
