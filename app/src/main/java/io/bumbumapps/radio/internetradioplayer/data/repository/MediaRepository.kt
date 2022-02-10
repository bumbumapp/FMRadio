package io.bumbumapps.radio.internetradioplayer.data.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.bumbumapps.radio.internetradioplayer.data.preference.Preferences
import io.bumbumapps.radio.internetradioplayer.domain.model.Media
import io.bumbumapps.radio.internetradioplayer.domain.model.MediaQueue
import io.bumbumapps.radio.internetradioplayer.domain.model.SingletonMediaQueue
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 16.02.2019.
 */

class MediaRepository
@Inject constructor(private val prefs: Preferences) {

    var mediaQueue: MediaQueue = SingletonMediaQueue()
    val currentMediaObs = BehaviorRelay.createDefault(Media.nullObj())

    var currentMedia: Media
        get() = currentMediaObs.value ?: Media.nullObj()
        set(value) {
            prefs.mediaId = value.id
            currentMediaObs.accept(value)
        }

    fun getNext(id: String): Media {
        return mediaQueue.getNext(id) ?: Media.nullObj()
    }

    fun getPrevious(id: String): Media {
        return mediaQueue.getPrevious(id) ?: Media.nullObj()
    }

    fun getSavedMediaId(): String {
        return prefs.mediaId
    }
}