package io.bumbumapps.radio.internetradioplayer.presentation.favorite

import io.bumbumapps.radio.internetradioplayer.domain.interactor.MainInteractor
import io.bumbumapps.radio.internetradioplayer.domain.interactor.RecordsInteractor
import io.bumbumapps.radio.internetradioplayer.extensions.subscribeX
import io.bumbumapps.radio.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class FavoritePresenter
@Inject constructor(private val recordsInteractor: RecordsInteractor,
                    private val mainInteractor: MainInteractor)
    : BasePresenter<FavoriteView>() {

    override fun onFirstAttach(view: FavoriteView) {
        view.showPage(mainInteractor.getFavoritePageId())
    }

    override fun onAttach(view: FavoriteView) {
        view.selectTab(mainInteractor.getFavoritePageId())

        recordsInteractor.recordsObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.showTabs(it.isNotEmpty()) })
                .addTo(viewSubs)
    }

    fun selectTab(position: Int) {
        mainInteractor.saveFavoritePageId(position)
        view?.showPage(position)
    }

}


