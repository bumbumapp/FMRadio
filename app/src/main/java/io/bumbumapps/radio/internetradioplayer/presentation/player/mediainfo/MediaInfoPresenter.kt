package io.bumbumapps.radio.internetradioplayer.presentation.player.mediainfo

import io.bumbumapps.radio.internetradioplayer.R
import io.bumbumapps.radio.internetradioplayer.domain.interactor.MediaInteractor
import io.bumbumapps.radio.internetradioplayer.domain.interactor.RecordsInteractor
import io.bumbumapps.radio.internetradioplayer.extensions.subscribeX
import io.bumbumapps.radio.internetradioplayer.presentation.base.BasePresenter
import io.bumbumapps.radio.internetradioplayer.presentation.navigation.Router
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 27.03.2019.
 */

class MediaInfoPresenter
@Inject constructor(private val mediaInteractor: MediaInteractor,
                    private val recordsInteractor: RecordsInteractor,
                    private val router: Router)
    : BasePresenter<MediaInfoView>() {

    override fun onAttach(view: MediaInfoView) {
        recordsInteractor.isCurrentRecordingObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.setRecording(it) })
                .addTo(viewSubs)

        mediaInteractor.currentMediaObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.setMedia(it) })
                .addTo(viewSubs)
    }

    fun openEqualizer() {
        router.addScreen(R.id.nav_equalizer)
    }

    fun startStopRecording() {
        recordsInteractor.startStopRecordingCurrentStation()
                .subscribeX()
    }
}