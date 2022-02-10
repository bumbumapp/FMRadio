package io.bumbumapps.radio.internetradioplayer.presentation.player.mediainfo

import io.bumbumapps.radio.internetradioplayer.domain.model.Media
import io.bumbumapps.radio.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 27.03.2019.
 */
interface MediaInfoView : BaseView {

    fun setRecording(isRecording: Boolean)

    fun setMedia(media: Media)
}