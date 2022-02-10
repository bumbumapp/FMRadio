package io.bumbumapps.radio.internetradioplayer.presentation.favorite.stations

import io.bumbumapps.radio.internetradioplayer.domain.model.FlatStationsList
import io.bumbumapps.radio.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 13.02.2019.
 */

interface FavoriteStationsView : BaseView {

    fun setStations(stationList: FlatStationsList)

    fun selectStation(uri: String)

    fun showPlaceholder(show: Boolean)
}