package io.bumbumapps.radio.internetradioplayer.presentation.player.coverart

import android.support.v4.media.MediaMetadataCompat
import io.bumbumapps.radio.internetradioplayer.data.service.extensions.*
import io.bumbumapps.radio.internetradioplayer.domain.interactor.CoverArtInteractor
import io.bumbumapps.radio.internetradioplayer.domain.interactor.PlayerInteractor
import io.bumbumapps.radio.internetradioplayer.extensions.subscribeX
import io.bumbumapps.radio.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Vladimir Mikhalev 28.03.2019.
 */

@Singleton
class CoverArtPresenter
@Inject constructor(private val coverArtInteractor: CoverArtInteractor,
                    private val playerInteractor: PlayerInteractor)
    : BasePresenter<CoverArtView>() {

    private var coverArtLoad: Disposable? = null

    override fun onAttach(view: CoverArtView) {
        playerInteractor.metadataObs
                .distinctUntilChanged { meta1, meta2 -> meta1.eq(meta2) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { loadCoverArt(it) })
                .addTo(viewSubs)
    }

    private fun loadCoverArt(metadata: MediaMetadataCompat) {
        coverArtLoad?.dispose()
        if (metadata.isEmpty() || metadata.isNotSupported()) return

        coverArtLoad = coverArtInteractor.getCoverArtUri(metadata.artist, metadata.title)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onSuccess = { view?.setCoverArt(it) })
    }

    override fun onDestroy() {
        coverArtLoad?.dispose()
    }
}