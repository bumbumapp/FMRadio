package io.bumbumapps.radio.internetradioplayer.presentation.search

import io.bumbumapps.radio.internetradioplayer.data.net.UberStationsService
import io.bumbumapps.radio.internetradioplayer.domain.interactor.MediaInteractor
import io.bumbumapps.radio.internetradioplayer.domain.interactor.SearchInteractor
import io.bumbumapps.radio.internetradioplayer.domain.interactor.SuggestionInteractor
import io.bumbumapps.radio.internetradioplayer.domain.model.Media
import io.bumbumapps.radio.internetradioplayer.domain.model.SearchState
import io.bumbumapps.radio.internetradioplayer.domain.model.Suggestion
import io.bumbumapps.radio.internetradioplayer.extensions.errorHandler
import io.bumbumapps.radio.internetradioplayer.extensions.subscribeX
import io.bumbumapps.radio.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

class ManualSearchPresenter
@Inject constructor(private val searchInteractor: SearchInteractor,
                    private val mediaInteractor: MediaInteractor,
                    private val suggestionInteractor: SuggestionInteractor)
    : BasePresenter<ManualSearchView>() {

    var intervalSearchEnabled: Boolean = false
    private var searchSub: Disposable? = null
    private var suggestionSub: Disposable? = null
    private var selectSub: Disposable? = null

    override fun onFirstAttach(view: ManualSearchView) {
        suggestionInteractor.queryRecentSuggestions("")
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { view.showPlaceholder(it.isEmpty()) }
                .filter { it.isNotEmpty() }
                .map { it.first() }
                .subscribeX(onNext = { view.selectSuggestion(it) })
                .addTo(viewSubs)
    }

    override fun onAttach(view: ManualSearchView) {
        mediaInteractor.currentMediaObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.selectMedia(it) })
                .addTo(viewSubs)
    }

    override fun onDetach() {
        searchSub?.dispose()
        suggestionSub?.dispose()
    }

    fun selectMedia(media: Media) {
        selectSub?.dispose()
        selectSub = searchInteractor.selectMedia(media)
                .subscribeX()
    }

    fun submitSearch(query: String) {
        searchSub?.dispose()

        searchSub = suggestionInteractor.saveSuggestion(query)
                .andThen(Observable.interval(0, 60, TimeUnit.SECONDS))
                .filter { intervalSearchEnabled }
                .flatMap { searchInteractor.search(UberStationsService.STATIONS_ENDPOINT, query) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { handleSearch(it) })
    }

    private fun handleSearch(state: SearchState) {
        when (state) {
            is SearchState.Loading -> {
                view?.showLoading(true)
            }
            is SearchState.Data -> {
                val data = state.data
                view?.setData(data)
                view?.showPlaceholder(data.isEmpty())
                view?.selectMedia(mediaInteractor.currentMedia)
                view?.showLoading(false)
            }
            is SearchState.Error -> {
                val error = state.error
                errorHandler.invoke(error)
                view?.showLoading(false)
                view?.showPlaceholder(true)
            }
        }
    }

    fun changeQuery(newText: String) {
        view?.enableRefresh(newText.isNotBlank())
        searchSub?.dispose()
        suggestionSub?.dispose()

        suggestionInteractor.queryRecentSuggestions(newText)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onSuccess = { view?.addRecentSuggestions(it) })
                .addTo(viewSubs)

        if (newText.isBlank()) return

        suggestionSub = suggestionInteractor.queryRegularSuggestions(newText)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view?.addRegularSuggestions(it) })
    }

    fun deleteRecentSuggestion(suggestion: Suggestion, curQuery: String) {
        suggestionInteractor.deleteRecentSuggestion(suggestion)
                .andThen(suggestionInteractor.queryRecentSuggestions(curQuery))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onSuccess = { view?.addRecentSuggestions(it) })
                .addTo(viewSubs)
    }
}
