package io.bumbumapps.radio.internetradioplayer.domain.interactor

import android.net.Uri
import io.bumbumapps.radio.internetradioplayer.R
import io.bumbumapps.radio.internetradioplayer.data.db.entity.Group
import io.bumbumapps.radio.internetradioplayer.data.db.entity.Station
import io.bumbumapps.radio.internetradioplayer.data.repository.FavoritesRepository
import io.bumbumapps.radio.internetradioplayer.data.repository.StationRepository
import io.bumbumapps.radio.internetradioplayer.data.utils.ShortcutHelper
import io.bumbumapps.radio.internetradioplayer.utils.MessageResException
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject


/**
 * Created by Vladimir Mikhalev 23.12.2017.
 */

class StationInteractor
@Inject constructor(private val stationRepository: StationRepository,
                    private val favoritesRepository: FavoritesRepository,
                    private val favoriteListInteractor: FavoriteListInteractor,
                    private val mediaInteractor: MediaInteractor,
                    private val historyInteractor: HistoryInteractor,
                    private val shortcutHelper: ShortcutHelper) {

    fun addCurrentShortcut(startPlay: Boolean): Boolean {
        val station = mediaInteractor.currentMedia as? Station ?: return false
        return shortcutHelper.pinShortcut(station, startPlay)
    }

    fun createStation(uri: Uri, name: String?): Single<Station> {
        return stationRepository.createStation(uri, name)
                .map { newStation ->
                    favoritesRepository.getStation { it.uri == newStation.uri }
                            ?: historyInteractor.getStation { it.uri == newStation.uri }
                            ?: newStation
                }
    }

    fun switchFavorite(station: Station): Completable {
        return if (isFavorite(station.id)) removeFromFavorite(station)
        else addToFavorite(station)
    }

    fun updateStation(station: Station): Completable {
        if (station.name.isBlank()) return Completable.error(MessageResException(R.string.msg_name_empty_error))

        return favoritesRepository.updateStations(listOf(station))
                .andThen(favoriteListInteractor.initFavoriteList())
    }

    fun addToFavorite(station: Station): Completable {
        return favoriteListInteractor.getGroup(station.groupId)
                .flatMapCompletable {
                    val newStation = station.copy(order = it.stations.size, groupId = Group.DEFAULT_ID)
                    newStation.isFavorite = true
                    favoritesRepository.addStation(newStation)
                            .andThen(favoriteListInteractor.initFavoriteList())
                            .andThen(mediaInteractor.setMedia(newStation))
                }
    }

    private fun removeFromFavorite(station: Station): Completable {
        return favoritesRepository.removeStation(station)
                .andThen(favoriteListInteractor.initFavoriteList())
                .andThen(mediaInteractor.setMedia(station.apply { isFavorite = false }))
    }

    private fun isFavorite(id: String) = favoritesRepository.getStation { it.id == id } != null
}

