package io.bumbumapps.radio.internetradioplayer.data.service.player

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.util.Util
import io.bumbumapps.radio.internetradioplayer.data.preference.Preferences
import io.bumbumapps.radio.internetradioplayer.extensions.subscribeX
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 22.09.2018.
 */

class LoadControl
@Inject constructor(private val prefs: Preferences) : BufferLoadControl() {

    private var initialBufferUs = C.msToUs(prefs.initialBufferLength * 1000L)
    private var bufferUs = C.msToUs(prefs.bufferLength * 1000L)
    private val minBufferUs = C.msToUs(DefaultLoadControl.DEFAULT_MIN_BUFFER_MS.toLong())
    private val maxBufferUs = C.msToUs(DefaultLoadControl.DEFAULT_MAX_BUFFER_MS.toLong())
    private var isBuffering: Boolean = false

    init {
        prefs.observe<Int>(Preferences.KEY_INITIAL_BUFFER_LENGTH)
                .subscribeX(onNext = {
                    initialBufferUs = C.msToUs(it * 1000L)
                    reset(true)
                })

        prefs.observe<Int>(Preferences.KEY_BUFFER_LENGTH)
                .subscribeX(onNext = {
                    bufferUs = C.msToUs(it * 1000L)
                    reset(true)
                })
    }

    override fun shouldContinueLoading(playbackPositionUs: Long, bufferedDurationUs: Long,
                                       playbackSpeed: Float): Boolean {
        val targetBufferSizeReached = allocator.totalBytesAllocated >= targetBufferSize

        isBuffering = bufferedDurationUs < minBufferUs // below low watermark
                || (bufferedDurationUs <= maxBufferUs // between watermarks
                && isBuffering
                && !targetBufferSizeReached)

        return isBuffering
    }

    override fun shouldStartPlayback(bufferedDurationUs: Long, playbackSpeed: Float,
                                     rebuffering: Boolean, targetLiveOffsetUs: Long): Boolean {
        val bufferDuration = Util.getPlayoutDurationForMediaDuration(bufferedDurationUs, playbackSpeed)
        val minBufferDuration = if (rebuffering) bufferUs else initialBufferUs
        return minBufferDuration <= 0 || bufferDuration >= minBufferDuration
    }

    override fun reset(resetAllocator: Boolean) {
        isBuffering = false
        super.reset(resetAllocator)
    }
}
