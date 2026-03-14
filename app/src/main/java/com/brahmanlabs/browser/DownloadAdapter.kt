package com.brahmanlabs.browser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DownloadAdapter(
    private var items: List<DownloadItem>,
    private val onItemClick: (DownloadItem) -> Unit,
    private val onDeleteClick: (DownloadItem) -> Unit
) : RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder>() {

    class DownloadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileName: TextView = view.findViewById(R.id.downloadFileName)
        val url: TextView = view.findViewById(R.id.downloadUrl)
        val time: TextView = view.findViewById(R.id.downloadTime)
        val deleteBtn: ImageButton = view.findViewById(R.id.btnDeleteDownload)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_download, parent, false)
        return DownloadViewHolder(view)
    }

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        val item = items[position]
        holder.fileName.text = item.fileName
        holder.url.text = item.url
        holder.time.text = formatTime(item.timestamp)
        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.deleteBtn.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<DownloadItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
