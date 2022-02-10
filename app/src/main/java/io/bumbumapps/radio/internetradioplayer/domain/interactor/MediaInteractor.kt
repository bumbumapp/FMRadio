package io.bumbumapps.radio.internetradioplayer.domain.interactor

import io.bumbumapps.radio.internetradioplayer.data.db.entity.Station
import io.bumbumapps.radio.internetradioplayer.data.repository.FavoritesRepository
import io.bumbumapps.radio.internetradioplayer.data.repository.MediaRepository
import io.bumbumapps.radio.internetradioplayer.data.repository.PlayerRepository
import io.bumbumapps.radio.internetradioplayer.data.repository.RecordsRepository
import io.bumbumapps.radio.internetradioplayer.data.service.COMMAND_DISABLE_SEEK
import io.bumbumapps.radio.internetradioplayer.data.service.COMMAND_DISABLE_SKIP
import io.bumbumapps.radio.internetradioplayer.data.service.COMMAND_ENABLE_SEEK
import io.bumbumapps.radio.internetradioplayer.data.service.COMMAND_ENABLE_SKIP
import io.bumbumapps.radio.internetradioplayer.domain.model.Media
import io.bumbumapps.radio.internetradioplayer.domain.model.Record
import io.bumbumapps.radio.internetradioplayer.domain.model.RecordsQueue
import io.bumbumapps.radio.internetradioplayer.domain.model.SingletonMediaQueue
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.toCompletable
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 16.02.2019.
 */

class MediaInteractor
@Inject constructor(private val mediaRepository: MediaRepository,
                    private val favoritesRepository: FavoritesRepository,
                    private val recordsRepository: RecordsRepository,
                    private val playerRepository: PlayerRepository) {

    val currentMediaObs: Observable<Media> get() = mediaRepository.currentMediaObs

    var currentMedia: Media
        get() = mediaRepository.currentMedia
        set(value) {
            when (value) {
                is Record -> setupRecordsQueue()
                is Station -> setupStationsQueue(value)
            }
            mediaRepository.currentMedia = value
            playerRepository.sendCommand(
                    if (mediaRepository.getNext(value.id).isNull()) COMMAND_DISABLE_SKIP
                    else COMMAND_ENABLE_SKIP
            )
        }

    fun setMedia(media: Media): Completable {
        return { currentMedia = media }.toCompletable()
    }

    fun nextMedia() {
        mediaRepository.currentMedia = mediaRepository.getNext(currentMedia.id)
    }

    fun previousMedia() {
        mediaRepository.currentMedia = mediaRepository.getPrevious(currentMedia.id)
    }

    fun getSavedMediaId(): String {
        return mediaRepository.getSavedMediaId()
    }

    fun resetCurrentMediaAndQueue() {
        currentMedia = currentMedia
    }

    private fun setupRecordsQueue() {
        mediaRepository.mediaQueue = RecordsQueue(recordsRepository.records)
        playerRepository.sendCommand(COMMAND_ENABLE_SEEK)
    }

    private fun setupStationsQueue(station: Station) {
        //todo refactor (favorite field)
        val queue = if (favoritesRepository.getStation { it.id == station.id } != null) {
            favoritesRepository.stations
        } else {
            SingletonMediaQueue()
        }
        mediaRepository.mediaQueue = queue
        playerRepository.sendCommand(COMMAND_DISABLE_SEEK)
    }
}