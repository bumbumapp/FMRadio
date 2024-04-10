package io.bumbumapps.radio.internetradioplayer.presentation.data

import android.content.Context
import io.bumbumapps.radio.internetradioplayer.domain.interactor.MediaInteractor
import io.bumbumapps.radio.internetradioplayer.domain.interactor.SearchInteractor
import io.bumbumapps.radio.internetradioplayer.domain.model.Media
import io.bumbumapps.radio.internetradioplayer.domain.model.SearchState
import io.bumbumapps.radio.internetradioplayer.extensions.errorHandler
import io.bumbumapps.radio.internetradioplayer.extensions.subscribeX
import io.bumbumapps.radio.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 21.03.2019.
 */

class DataPresenter
@Inject constructor(private val searchInteractor: SearchInteractor,
                    private val mediaInteractor: MediaInteractor) : BasePresenter<DataView>() {

    private var selectSub: Disposable? = null

    override fun onAttach(view: DataView) {
        mediaInteractor.currentMediaObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.selectMedia(it) })
                .addTo(viewSubs)
    }

    fun fetchData(endpoint: String?, query: String?) {
        searchInteractor.search(endpoint, query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { handleSearch(it) })

    }

    fun selectMedia(media: Media,contxt:Context) {
        selectSub?.dispose()
        selectSub = searchInteractor
                .selectMedia(media,contxt)
                .subscribeX()
    }

    private fun handleSearch(state: SearchState) {
        when (state) {
            is SearchState.Loading -> {
                view?.showLoading(true)
            }
            is SearchState.Data -> {
                val data = state.data
                view?.setData(data)
                view?.selectMedia(mediaInteractor.currentMedia)
                view?.showLoading(false)
            }
            is SearchState.Error -> {
                val error = state.error
                errorHandler.invoke(error)
                view?.showLoading(false)
            }
        }
    }
}