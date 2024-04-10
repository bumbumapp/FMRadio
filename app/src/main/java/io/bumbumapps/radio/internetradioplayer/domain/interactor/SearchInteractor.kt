package io.bumbumapps.radio.internetradioplayer.domain.interactor

import android.content.Context
import io.bumbumapps.radio.internetradioplayer.R
import io.bumbumapps.radio.internetradioplayer.data.db.entity.Station
import io.bumbumapps.radio.internetradioplayer.data.net.UberStationsService
import io.bumbumapps.radio.internetradioplayer.data.repository.FavoritesRepository
import io.bumbumapps.radio.internetradioplayer.data.repository.SearchRepository
import io.bumbumapps.radio.internetradioplayer.domain.model.Media
import io.bumbumapps.radio.internetradioplayer.domain.model.SearchState
import io.bumbumapps.radio.internetradioplayer.utils.AdsLoader
import io.bumbumapps.radio.internetradioplayer.utils.MessageResException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

class SearchInteractor
@Inject constructor(private val searchRepository: SearchRepository,
                    private val favoritesRepository: FavoritesRepository,
                    private val mediaInteractor: MediaInteractor) {

    fun search(endpoint: String?, query: String?): Observable<SearchState> {
        if (query == null) return Observable.just(SearchState.Data(emptyList()))
        val q = query.trim()
        return when (endpoint) {
            UberStationsService.STATIONS_ENDPOINT -> searchStations(q)
            UberStationsService.TOPSONGS_ENDPOINT -> searchTopSongs(q)
            else -> Observable.error(IllegalStateException("Can't find endpoint $endpoint"))
        }
    }

    private fun searchStations(query: String): Observable<SearchState> {
        if (query.length < 3) return Observable.just(SearchState.Error(MessageResException(R.string.msg_text_short)))
        return searchStationsWithFavorites(searchRepository.searchStations(query)) {
            it.toStation()
        }
    }

    private fun searchTopSongs(query: String): Observable<SearchState> {
        return searchStationsWithFavorites(searchRepository.searchTopSongs(query.trim())) {
            it.toStation()
        }
    }

    fun selectMedia(media: Media,context: Context): Completable {
        return when (media) {
            is Station -> selectStation(media,context)
            else -> Completable.complete()
        }
    }

    private fun selectStation(station: Station,context: Context): Completable {
        AdsLoader.showAds(context){}
        return searchRepository.searchStation(station)
                .flatMapObservable { response ->
                    val newStation = response.toStation()
                    searchRepository.parseFromNet(newStation)
                            .toObservable()
                            .startWith(newStation)
                }
                .doOnNext { newStation ->
                    mediaInteractor.currentMedia =
                            favoritesRepository.getStation { it.uri == newStation.uri }
                                    ?: newStation
                }.ignoreElements()
    }

    private fun <T> searchStationsWithFavorites(searchObs: Single<List<T>>, transform: (T) -> Station)
            : Observable<SearchState> {
        return Observables.combineLatest(favoritesRepository.stationsListObs, searchObs.toObservable())
        { _, result ->
            val data = result.map { element ->
                val station = transform(element)
                favoritesRepository.getStation { it.uri == station.uri }
                        ?: station.apply { isFavorite = false }
            }
            SearchState.Data(data) as SearchState
        }
                .startWith(SearchState.Loading)
                .onErrorReturn { SearchState.Error(it) }
    }
}
