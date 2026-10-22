package io.bumbumapps.radio.internetradioplayer.data.service.recorder

import io.bumbumapps.radio.internetradioplayer.data.service.player.BufferLoadControl

/**
 * Created by Vladimir Mikhalev 11.03.2019.
 * Loads continuously and never starts playback: the stream is only teed into the record file.
 */
class RecorderLoadControl : BufferLoadControl() {

    override fun shouldContinueLoading(playbackPositionUs: Long, bufferedDurationUs: Long,
                                       playbackSpeed: Float): Boolean = true

    override fun shouldStartPlayback(bufferedDurationUs: Long, playbackSpeed: Float,
                                     rebuffering: Boolean, targetLiveOffsetUs: Long): Boolean = false
}
