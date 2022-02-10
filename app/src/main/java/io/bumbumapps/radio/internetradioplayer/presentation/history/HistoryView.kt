package io.bumbumapps.radio.internetradioplayer.presentation.history

import io.bumbumapps.radio.internetradioplayer.data.db.entity.Station
import io.bumbumapps.radio.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 02.12.2018.
 */

interface HistoryView : BaseView {

    fun setHistory(list: List<Station>)

    fun selectStation(uri: String)

    fun showPlaceholder(show: Boolean)
}