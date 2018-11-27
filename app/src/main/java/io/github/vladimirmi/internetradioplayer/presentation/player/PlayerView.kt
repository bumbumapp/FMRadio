package io.github.vladimirmi.internetradioplayer.presentation.player

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

interface PlayerView : BaseView {

    fun setStation(station: Station)

    fun openLinkDialog(url: String)

    fun openAddShortcutDialog()

    fun setMetadata(metadata: String)
    fun showStopped()
    fun showBuffering()
    fun showPlaying()
    fun showPrevious()
    fun showNext()
}
