package io.bumbumapps.radio.internetradioplayer.domain.interactor

import io.bumbumapps.radio.internetradioplayer.data.net.CoverArtService
import io.bumbumapps.radio.internetradioplayer.data.repository.CoverArtRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 25.03.2019.
 */

class CoverArtInteractor
@Inject constructor(private val repository: CoverArtRepository) {

    fun getCoverArtUri(artist: String, title: String): Single<String> {
        val query = "${CoverArtService.FIELD_ARTIST}:$artist AND ${CoverArtService.FIELD_TITLE}:$title"
        return repository.getCoverArtUri(query)
    }

}