package io.bumbumapps.radio.internetradioplayer.domain.interactor

import android.net.Uri
import io.bumbumapps.radio.internetradioplayer.data.db.entity.Station
import io.bumbumapps.radio.internetradioplayer.data.repository.RecordsRepository
import io.bumbumapps.radio.internetradioplayer.domain.model.Media
import io.bumbumapps.radio.internetradioplayer.domain.model.Record
import io.bumbumapps.radio.internetradioplayer.extensions.toUri
import io.bumbumapps.radio.internetradioplayer.utils.MessageException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 15.02.2019.
 */

class RecordsInteractor
@Inject constructor(private val recordsRepository: RecordsRepository,
                    private val mediaInteractor: MediaInteractor) {

    val recordsObs: Observable<List<Record>> get() = recordsRepository.recordsObs

    fun initRecords(): Completable {
        return recordsRepository.initRecords()
                .doOnComplete {
                    recordsRepository.records
                            .find { mediaInteractor.getSavedMediaId() == it.id }
                            ?.let { mediaInteractor.currentMedia = it }
                }
    }

    fun commitRecord(stationUri: Uri, record: Record) {
        recordsRepository.records += record.commit()
        mediaInteractor.resetCurrentMediaAndQueue()
        recordsRepository.removeFromCurrentRecording(stationUri)
    }

    fun createNewRecord(stationUri: Uri, name: String): Record {
        recordsRepository.addToCurrentRecording(stationUri)
        return recordsRepository.createNewRecord(name)
    }

    fun deleteRecord(record: Record): Completable {
        return recordsRepository.deleteRecord(record)
                .flatMapCompletable { deleted ->
                    if (deleted) {
                        Completable.fromAction {
                            if (mediaInteractor.currentMedia == record) mediaInteractor.previousMedia()
                            recordsRepository.records -= record
                            mediaInteractor.resetCurrentMediaAndQueue()
                        }
                    } else Completable.error(MessageException("Cannot delete record"))
                }
    }

    fun startStopRecordingCurrentStation(): Completable {
        val station = mediaInteractor.currentMedia as? Station ?: return Completable.complete()
        return Completable.fromAction { recordsRepository.startStopRecording(station) }
    }

    fun stopRecording(uri: Uri) {
        recordsRepository.stopRecording(uri)
    }

    fun stopAllRecordings() {
        recordsRepository.currentRecordingUrisObs.value
                ?.forEach { stopRecording(it.toUri()) }
    }

    fun isCurrentRecordingObs(): Observable<Boolean> {
        return Observables.combineLatest(recordsRepository.currentRecordingUrisObs,
                mediaInteractor.currentMediaObs) { uris: Set<String>, media: Media ->
            uris.contains(media.uri)
        }.distinctUntilChanged()
    }
}