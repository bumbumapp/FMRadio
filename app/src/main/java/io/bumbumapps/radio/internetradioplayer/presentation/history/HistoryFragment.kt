package io.bumbumapps.radio.internetradioplayer.presentation.history

import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import io.bumbumapps.radio.internetradioplayer.R
import io.bumbumapps.radio.internetradioplayer.data.db.entity.Station
import io.bumbumapps.radio.internetradioplayer.di.Scopes
import io.bumbumapps.radio.internetradioplayer.extensions.visible
import io.bumbumapps.radio.internetradioplayer.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_history.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 02.12.2018.
 */

class HistoryFragment : BaseFragment<HistoryPresenter, HistoryView>(), HistoryView {

    private val historyAdapter = HistoryAdapter()

    override val layout = R.layout.fragment_history

    override fun providePresenter(): HistoryPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(HistoryPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        historyRv.layoutManager = LinearLayoutManager(context)
        historyRv.adapter = historyAdapter
        historyAdapter.onItemClickListener = { presenter.selectStation(it) }
    }

    override fun setHistory(list: List<Station>) {
        historyAdapter.data = list
    }

    override fun selectStation(uri: String) {
        val position = historyAdapter.selectMedia(uri)
        historyRv.scrollToPosition(position)
    }

    override fun showPlaceholder(show: Boolean) {
        historyRv.visible(!show)
        placeholderView.visible(show)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.groupId != R.id.context_menu_history) return false
        val selectedItem = historyAdapter.longClickedItem
        if (item.itemId == R.id.context_menu_action_delete && selectedItem != null) {
            presenter.deleteHistory(selectedItem)
            return true
        }
        return false
    }
}