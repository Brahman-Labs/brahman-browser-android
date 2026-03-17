package com.brahmanlabs.browser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReadingAdapter(
    private var items: List<ReadingItem>,
    private val onItemClick: (ReadingItem) -> Unit,
    private val onDeleteClick: (ReadingItem) -> Unit
) : RecyclerView.Adapter<ReadingAdapter.ReadingViewHolder>() {

    class ReadingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.readingTitle)
        val url: TextView = view.findViewById(R.id.readingUrl)
        val deleteBtn: ImageButton = view.findViewById(R.id.btnDeleteReading)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reading, parent, false)
        return ReadingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReadingViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title.ifEmpty { item.url }
        holder.url.text = item.url
        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.deleteBtn.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<ReadingItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
