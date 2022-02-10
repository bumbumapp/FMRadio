package io.bumbumapps.radio.internetradioplayer.presentation.data

import io.bumbumapps.radio.internetradioplayer.domain.model.Media
import io.bumbumapps.radio.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 21.03.2019.
 */

interface DataView : BaseView {

    fun setData(data: List<Media>)

    fun selectMedia(media: Media)

    fun showLoading(loading: Boolean)
}