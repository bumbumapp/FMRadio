package io.bumbumapps.radio.internetradioplayer.presentation.favorite

import io.bumbumapps.radio.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 25.02.2019.
 */

interface FavoriteView : BaseView {

    fun showTabs(visible: Boolean)

    fun showPage(position: Int)

    fun selectTab(position: Int)
}