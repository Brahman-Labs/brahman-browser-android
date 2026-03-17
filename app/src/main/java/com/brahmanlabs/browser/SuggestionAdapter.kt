package com.brahmanlabs.browser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Suggestion(
    val text: String,
    val displayText: String = text,
    val subText: String = "",
    val isHistory: Boolean = false,
    val isBookmark: Boolean = false,
    val isSearch: Boolean = false,
    val isSearchSuggest: Boolean = false
)

class SuggestionAdapter(
    private var items: List<Suggestion>,
    private val onItemClick: (Suggestion) -> Unit,
    private val onFillClick: (Suggestion) -> Unit
) : RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder>() {

    class SuggestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.suggestionIcon)
        val text: TextView = view.findViewById(R.id.suggestionText)
        val subText: TextView = view.findViewById(R.id.suggestionSubText)
        val fillBtn: ImageButton = view.findViewById(R.id.btnSuggestionFill)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggestion, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val item = items[position]
        holder.text.text = item.displayText
        if (item.subText.isNotEmpty()) {
            holder.subText.visibility = View.VISIBLE
            holder.subText.text = item.subText
        } else {
            holder.subText.visibility = View.GONE
        }
        holder.icon.setImageResource(
            when {
                item.isBookmark -> R.drawable.ic_bookmark
                item.isHistory -> R.drawable.ic_history
                else -> R.drawable.ic_language
            }
        )
        holder.fillBtn.visibility =
            if (item.isSearch || item.isSearchSuggest) View.GONE else View.VISIBLE
        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.fillBtn.setOnClickListener { onFillClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<Suggestion>) {
        items = newItems
        notifyDataSetChanged()
    }
}
