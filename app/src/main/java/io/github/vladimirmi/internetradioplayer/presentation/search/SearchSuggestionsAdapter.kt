package io.github.vladimirmi.internetradioplayer.presentation.search

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.github.vladimirmi.internetradioplayer.domain.model.SuggestionList
import kotlinx.android.synthetic.main.item_suggestion.view.*

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

class SearchSuggestionsAdapter(private val callback: SearchSuggestionsAdapter.Callback)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var longClickedItem: Suggestion? = null
    private var suggestions = SuggestionList()

    fun addRecentSuggestions(list: List<Suggestion>) {
        val newList = suggestions.copy()
        newList.recent = list
        getDiffResult(newList).dispatchUpdatesTo(this)
        suggestions = newList
    }

    fun addRegularSuggestions(list: List<Suggestion>) {
        val newList = suggestions.copy()
        newList.regular = list
        getDiffResult(newList).dispatchUpdatesTo(this)
        suggestions = newList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return SuggestionVH(inflater.inflate(R.layout.item_suggestion, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as SuggestionVH
        val suggestion = suggestions[position]
        holder.bind(suggestion)
        holder.itemView.setOnClickListener { callback.onSuggestionSelected(suggestion) }
        holder.itemView.setOnLongClickListener { longClickedItem = suggestion; false }
    }

    override fun getItemCount(): Int {
        return suggestions.size
    }

    private fun getDiffResult(new: List<Suggestion>): DiffUtil.DiffResult {
        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return suggestions.size
            }

            override fun getNewListSize(): Int {
                return new.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return suggestions[oldItemPosition] == new[newItemPosition]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return true
            }
        })
    }

    interface Callback {
        fun onSuggestionSelected(suggestion: Suggestion)
    }
}

class SuggestionVH(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {


    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        menu?.add(R.id.context_menu_suggestion, R.id.context_menu_action_delete, 0, R.string.menu_delete)
    }

    fun bind(suggestion: Suggestion) {
        itemView.suggestionTv.text = suggestion.value
        val icon = if (suggestion is Suggestion.Recent) {
            itemView.setOnCreateContextMenuListener(this)
            R.drawable.ic_history
        } else {
            itemView.setOnCreateContextMenuListener(null)
            R.drawable.ic_search
        }
        itemView.iconIv.setImageResource(icon)
    }

}
