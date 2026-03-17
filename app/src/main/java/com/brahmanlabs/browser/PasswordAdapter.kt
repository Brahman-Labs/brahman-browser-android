package com.brahmanlabs.browser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PasswordAdapter(
    private var items: List<PasswordItem>,
    private val onItemClick: (PasswordItem) -> Unit,
    private val onDeleteClick: (PasswordItem) -> Unit
) : RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder>() {

    class PasswordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val domain: TextView = view.findViewById(R.id.passwordDomain)
        val username: TextView = view.findViewById(R.id.passwordUsername)
        val deleteBtn: ImageButton = view.findViewById(R.id.btnDeletePassword)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_password, parent, false)
        return PasswordViewHolder(view)
    }

    override fun onBindViewHolder(holder: PasswordViewHolder, position: Int) {
        val item = items[position]
        holder.domain.text = item.domain
        holder.username.text = item.username
        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.deleteBtn.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<PasswordItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
