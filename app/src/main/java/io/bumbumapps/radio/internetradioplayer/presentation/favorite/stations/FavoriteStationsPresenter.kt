package io.bumbumapps.radio.internetradioplayer.presentation.favorite.stations

import io.bumbumapps.radio.internetradioplayer.data.db.entity.Group
import io.bumbumapps.radio.internetradioplayer.data.db.entity.Station
import io.bumbumapps.radio.internetradioplayer.domain.interactor.FavoriteListInteractor
import io.bumbumapps.radio.internetradioplayer.domain.interactor.MediaInteractor
import io.bumbumapps.radio.internetradioplayer.domain.interactor.StationInteractor
import io.bumbumapps.radio.internetradioplayer.domain.model.FlatStationsList
import io.bumbumapps.radio.internetradioplayer.extensions.subscribeX
import io.bumbumapps.radio.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 13.02.2019.
 */

class FavoriteStationsPresenter
@Inject constructor(private val favoriteListInteractor: FavoriteListInteractor,
                    private val mediaInteractor: MediaInteractor,
                    private val stationInteractor: StationInteractor)
    : BasePresenter<FavoriteStationsView>() {

    override fun onAttach(view: FavoriteStationsView) {
        favoriteListInteractor.stationsListObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = {
                    view.setStations(it)
                    view.showPlaceholder(it.size == 0)
                })
                .addTo(viewSubs)

        mediaInteractor.currentMediaObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.selectStation(it.uri) })
                .addTo(viewSubs)
    }

    fun selectStation(station: Station) {
        mediaInteractor.currentMedia = station
    }

    fun selectGroup(group: Group) {
        favoriteListInteractor.expandOrCollapseGroup(group)
                .subscribeX()
                .addTo(dataSubs)
    }

    fun createGroup(groupName: String) {
        favoriteListInteractor.createGroup(groupName)
                .subscribeX()
                .addTo(viewSubs)
    }

    fun deleteGroup(group: Group) {
        favoriteListInteractor.deleteGroup(group)
                .subscribeX()
                .addTo(dataSubs)
    }

    fun editGroup(group: Group, newName: String) {
        favoriteListInteractor.updateGroup(group.copy(name = newName))
                .subscribeX()
                .addTo(dataSubs)
    }

    fun moveGroupElements(stations: FlatStationsList) {
        favoriteListInteractor.moveGroupElements(stations)
                .subscribeX()
                .addTo(dataSubs)
    }

    fun deleteStation(station: Station) {
        stationInteractor.switchFavorite(station)
                .subscribeX()
                .addTo(dataSubs)
    }

    fun editStation(station: Station, newName: String) {
        stationInteractor.updateStation(station.copy(name = newName))
                .subscribeX()
                .addTo(dataSubs)
    }
}