package io.bumbumapps.radio.internetradioplayer.data.repository

import io.bumbumapps.radio.internetradioplayer.data.db.entity.Station
import io.bumbumapps.radio.internetradioplayer.data.net.UberStationsService
import io.bumbumapps.radio.internetradioplayer.data.net.ubermodel.StationIdResult
import io.bumbumapps.radio.internetradioplayer.data.net.ubermodel.StationIdSearch
import io.bumbumapps.radio.internetradioplayer.data.net.ubermodel.StationResult
import io.bumbumapps.radio.internetradioplayer.data.net.ubermodel.TopSongResult
import io.bumbumapps.radio.internetradioplayer.data.utils.StationParser
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

class SearchRepository
@Inject constructor(private val uberStationsService: UberStationsService,
                    private val parser: StationParser) {

    fun searchStations(query: String): Single<List<StationResult>> {
        return uberStationsService.searchStations(query)
                .map { it.result }
                .subscribeOn(Schedulers.io())
    }

    fun searchStation(station: Station): Single<StationIdResult> {
        return uberStationsService.getStation(station.getUberId())
                .map(StationIdSearch::getResult)
                .subscribeOn(Schedulers.io())
    }

    fun parseFromNet(station: Station): Single<Station> {
        return Single.fromCallable { parser.parseFromStation(station) }
                .subscribeOn(Schedulers.io())
    }

    fun searchTopSongs(query: String): Single<List<TopSongResult>> {
        return uberStationsService.searchTopSongs(query)
                .map { it.result }
                .subscribeOn(Schedulers.io())
    }
}
