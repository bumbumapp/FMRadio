package io.bumbumapps.radio.internetradioplayer.presentation.main

import io.bumbumapps.radio.internetradioplayer.R
import io.bumbumapps.radio.internetradioplayer.domain.interactor.MainInteractor
import io.bumbumapps.radio.internetradioplayer.presentation.base.BasePresenter
import io.bumbumapps.radio.internetradioplayer.presentation.navigation.Router
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

class MainPresenter
@Inject constructor(private val router: Router,
                    private val mainInteractor: MainInteractor)
    : BasePresenter<MainView>() {

    fun selectPage(position: Int) {
        val pageId = when (position) {
            PAGE_SEARCH -> R.id.nav_search
            PAGE_FAVORITES -> R.id.nav_favorites
            else -> R.id.nav_history
        }
        mainInteractor.saveMainPageId(pageId)
        router.replaceScreen(pageId)
    }
}
