package io.bumbumapps.radio.internetradioplayer.presentation.favorite.records

import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import io.bumbumapps.radio.internetradioplayer.R
import io.bumbumapps.radio.internetradioplayer.di.Scopes
import io.bumbumapps.radio.internetradioplayer.domain.model.Record
import io.bumbumapps.radio.internetradioplayer.presentation.base.BaseFragment

import toothpick.Toothpick

// Former kotlin-android-extensions synthetic view accessors.
private val android.app.Activity.recordsRv: androidx.recyclerview.widget.RecyclerView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.recordsRv)
private val androidx.fragment.app.Fragment.recordsRv: androidx.recyclerview.widget.RecyclerView get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.recordsRv)
private val android.app.Dialog.recordsRv: androidx.recyclerview.widget.RecyclerView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.recordsRv)

/**
 * Created by Vladimir Mikhalev 13.02.2019.
 */

class RecordsFragment : BaseFragment<RecordsPresenter, RecordsView>(), RecordsView {

    override val layout = R.layout.fragment_records

    private val recordsAdapter = RecordsAdapter()

    override fun providePresenter(): RecordsPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(RecordsPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        recordsRv.layoutManager = LinearLayoutManager(context)
        recordsRv.adapter = recordsAdapter
        recordsAdapter.onItemClickListener = { presenter.selectRecord(it) }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.groupId != R.id.context_menu_records) return false
        val selectedItem = recordsAdapter.longClickedItem
        if (item.itemId == R.id.context_menu_action_delete && selectedItem != null) {
            presenter.deleteRecord(selectedItem)
        } else {
            return false
        }
        return true
    }

    //region =============== RecordsView ==============

    override fun setRecords(records: List<Record>) {
        recordsAdapter.records = records
    }

    override fun selectRecord(id: String) {
        val position = recordsAdapter.selectRecord(id)
        recordsRv.scrollToPosition(position)
    }

    //endregion
}