package io.bumbumapps.radio.internetradioplayer.presentation.root

import android.net.Uri
import io.bumbumapps.radio.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

interface RootView : BaseView {

    fun checkIntent()

    fun showLoadingIndicator(visible: Boolean)

    fun hidePlayer()

    fun collapsePlayer()

    fun expandPlayer()

    fun createStation(uri: Uri, name: String?, addToFavorite: Boolean, startPlay: Boolean)

    fun setOffset(offset: Float)
}
