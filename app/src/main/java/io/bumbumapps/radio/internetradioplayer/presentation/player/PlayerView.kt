package io.bumbumapps.radio.internetradioplayer.presentation.player

import io.bumbumapps.radio.internetradioplayer.data.db.entity.Station
import io.bumbumapps.radio.internetradioplayer.domain.model.Record
import io.bumbumapps.radio.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

interface PlayerView : BaseView {

    fun setStation(station: Station)

    fun setRecord(record: Record)

    fun setMetadata(metadata: String)

    fun setStatus(resId: Int)

    fun showPlaying(isPlaying: Boolean)

    fun showPrevious()

    fun showNext()

    fun setDuration(duration: Long)

    fun setPosition(position: Long)

    fun incrementPositionBy(duration: Long)

    fun enableSeek(isEnabled: Boolean)

    fun enableSkip(isEnabled: Boolean)

    fun enableCoverArt(isEnabled: Boolean)
}
