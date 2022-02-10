package io.bumbumapps.radio.internetradioplayer.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.jakewharton.rxrelay2.BehaviorRelay
import io.bumbumapps.radio.internetradioplayer.data.db.entity.Station
import io.bumbumapps.radio.internetradioplayer.data.service.recorder.RecorderService
import io.bumbumapps.radio.internetradioplayer.data.service.recorder.RecordersPool
import io.bumbumapps.radio.internetradioplayer.domain.model.Record
import io.bumbumapps.radio.internetradioplayer.extensions.toUri
import io.bumbumapps.radio.internetradioplayer.utils.MessageException
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 02.02.2019.
 */

private const val RECORDS_DIRECTORY = "Records"
private const val RECORD_EXT = "mp3"
private const val RECORD_SUFFIX = " Record"

class RecordsRepository
@Inject constructor(private val context: Context) {

    private val recordsDirectory: File by lazy {
        val dir = File(context.getExternalFilesDir(null), RECORDS_DIRECTORY)
        dir.mkdir()
        dir
    }

    val currentRecordingUrisObs: BehaviorRelay<Set<String>> = BehaviorRelay.createDefault(emptySet())
    val recordsObs: BehaviorRelay<List<Record>> = BehaviorRelay.create()

    var records: List<Record>
        get() = recordsObs.value ?: emptyList()
        set(value) {
            recordsObs.accept(value.sortedBy(Record::createdAt))
        }

    fun initRecords(): Completable {
        return Completable.fromAction { records = loadRecords() }
                .subscribeOn(Schedulers.io())
    }

    fun startStopRecording(station: Station) {
        when {
            currentRecordingUrisObs.value!!.contains(station.uri) -> stopRecording(station.uri.toUri())
            currentRecordingUrisObs.value!!.size < RecordersPool.MAX_RECORDERS -> startRecording(station)
            else -> throw MessageException("The maximum number of simultaneous recordings is ${RecordersPool.MAX_RECORDERS}")
        }
    }

    private fun startRecording(station: Station) {
        Toast.makeText(context,
                "Feature is in beta. The current record size limit is 50 Mb",
                Toast.LENGTH_LONG).show()
        val intent = Intent(context, RecorderService::class.java).apply {
            putExtra(RecorderService.EXTRA_START_RECORD, station.name)
            data = station.uri.toUri()
        }
        context.startService(intent)
    }

    fun stopRecording(uri: Uri) {
        val intent = Intent(context, RecorderService::class.java).apply {
            putExtra(RecorderService.EXTRA_STOP_RECORD, "")
            data = uri
        }
        context.startService(intent)
    }

    fun createNewRecord(name: String): Record {
        val newName = getNewRecordName(name)
        val file = File(recordsDirectory, "$newName.$RECORD_EXT")
        return Record.newRecord(file)
    }

    fun deleteRecord(record: Record): Single<Boolean> {
        return Single.fromCallable { record.file.delete() }
                .subscribeOn(Schedulers.io())
    }

    fun addToCurrentRecording(stationUri: Uri) {
        currentRecordingUrisObs.accept(currentRecordingUrisObs.value!! + stationUri.toString())
    }

    fun removeFromCurrentRecording(stationUri: Uri) {
        currentRecordingUrisObs.accept(currentRecordingUrisObs.value!! - stationUri.toString())
    }

    private fun loadRecords(): List<Record> {
        return recordsDirectory
                .listFiles { pathname -> pathname.extension == RECORD_EXT }
                ?.map { Record.fromFile(it) } ?: emptyList()
    }

    private fun getNewRecordName(stationName: String): String {
        val regex = "^$stationName$RECORD_SUFFIX( \\d)?".toRegex()
        val list = records.filter { it.name.matches(regex) }
        return if (list.isEmpty()) "$stationName$RECORD_SUFFIX"
        else "$stationName$RECORD_SUFFIX ${list.size}"
    }
}
