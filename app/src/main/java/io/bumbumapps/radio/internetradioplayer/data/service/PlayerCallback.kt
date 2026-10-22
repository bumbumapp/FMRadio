package io.bumbumapps.radio.internetradioplayer.data.service

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.metadata.icy.IcyInfo
import io.bumbumapps.radio.internetradioplayer.data.service.extensions.*
import io.bumbumapps.radio.internetradioplayer.domain.model.Media
import io.bumbumapps.radio.internetradioplayer.extensions.runOnUiThread
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.max

const val EVENT_SESSION_START = "EVENT_SESSION_START"
const val EVENT_SESSION_END = "EVENT_SESSION_END"

abstract class PlayerCallback : Player.Listener {

    private var sessionId = 0
    private var playbackStateCompat = DEFAULT_PLAYBACK_STATE
    private var mediaMetadata = EMPTY_METADATA
    private var metadataPostTask: TimerTask? = null
    var player: ExoPlayer? = null

    fun initDefault() {
        onPlaybackStateChanged(playbackStateCompat)
        onMediaMetadataChanged(mediaMetadata)
    }

    fun setStartAudioSessionId(audioSessionId: Int) {
        if (sessionId != audioSessionId) {
            onAudioSessionId(EVENT_SESSION_END, sessionId)
            sessionId = audioSessionId
            onAudioSessionId(EVENT_SESSION_START, audioSessionId)
        }
    }

    fun setMedia(media: Media) {
        mediaMetadata = EMPTY_METADATA.setMedia(media)
        onMediaMetadataChanged(mediaMetadata)
    }

    fun setMetadata(metadata: String) {
        fun postMetadata() {
            mediaMetadata = mediaMetadata.setArtistTitle(metadata)
            onMediaMetadataChanged(mediaMetadata)
        }
        runOnUiThread {
            metadataPostTask?.cancel()
            if (player == null) {
                postMetadata()
            } else {
                // take in to account buffered duration
                val bufferedMs = max(player!!.bufferedPosition - player!!.currentPosition, 0)
                metadataPostTask = Timer().schedule(bufferedMs) { postMetadata() }
            }
        }
    }

    fun changeActions(changer: (Long) -> Long) {
        playbackStateCompat = playbackStateCompat.changeActions(changer)
        onPlaybackStateChanged(playbackStateCompat)
    }

    //region =============== Player.Listener ==============

    override fun onPlayerError(error: PlaybackException) {
        onPlayerError(error as Exception)
    }

    /** ICY stream titles ("Artist - Title") arrive as IcyInfo metadata entries. */
    override fun onMetadata(metadata: Metadata) {
        for (i in 0 until metadata.length()) {
            val entry = metadata.get(i)
            if (entry is IcyInfo) {
                setMetadata(entry.title ?: "")
            }
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        val state = mapPlaybackState(playbackState, playWhenReady)
        playbackStateCompat = playbackStateCompat.setState(state)

        if (playbackState == Player.STATE_ENDED) {
            //todo implement "play next"
            player?.stop()
        }

        if (playbackState == Player.STATE_READY) {
            mediaMetadata = mediaMetadata.setDuration(player?.duration ?: 0)
            playbackStateCompat = playbackStateCompat.setPosition(player?.currentPosition ?: 0)
            if (playWhenReady && player?.volume == 0f) fadeInVolume(player)
          val actionsChanger = if (player?.isCurrentMediaItemSeekable == true) PlayerActions::enableSeek
          else PlayerActions::disableSeek
          playbackStateCompat = playbackStateCompat.changeActions(actionsChanger)
        }

        if (state == PlaybackStateCompat.STATE_STOPPED) {
            playbackStateCompat = playbackStateCompat.setPosition(0)
            onAudioSessionId(EVENT_SESSION_END, sessionId)
            sessionId = 0
            mediaMetadata = mediaMetadata.clearArtistTitle()
        }

        onPlaybackStateChanged(playbackStateCompat)
        onMediaMetadataChanged(mediaMetadata)
    }

    override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo,
                                         reason: Int) {
        if (reason == Player.DISCONTINUITY_REASON_SEEK) {
            player?.let {
                playbackStateCompat = playbackStateCompat.setPosition(it.currentPosition)
                onPlaybackStateChanged(playbackStateCompat)
            }
        }
    }

    //endregion

    protected abstract fun onPlayerError(error: Exception)

    protected abstract fun onAudioSessionId(event: String, audioSessionId: Int)

    protected abstract fun onPlaybackStateChanged(state: PlaybackStateCompat)

    protected abstract fun onMediaMetadataChanged(mediaMetadata: MediaMetadataCompat)

    private fun mapPlaybackState(playbackState: Int, playWhenReady: Boolean): Int {
        return when {
            playbackState == Player.STATE_IDLE -> PlaybackStateCompat.STATE_STOPPED
            playbackState == Player.STATE_BUFFERING && playWhenReady -> PlaybackStateCompat.STATE_BUFFERING
            playbackState == Player.STATE_BUFFERING && !playWhenReady -> PlaybackStateCompat.STATE_PAUSED
            playbackState == Player.STATE_READY && playWhenReady -> PlaybackStateCompat.STATE_PLAYING
            playbackState == Player.STATE_READY && !playWhenReady -> PlaybackStateCompat.STATE_PAUSED
            playbackState == Player.STATE_ENDED -> PlaybackStateCompat.STATE_STOPPED
            else -> PlaybackStateCompat.STATE_NONE
        }
    }

    private val volumeAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(3000)

    private fun fadeInVolume(player: ExoPlayer?) {
        if (player == null) return
        volumeAnimator.cancel()
        volumeAnimator.removeAllUpdateListeners()
        volumeAnimator.addUpdateListener { player.volume = it.animatedValue as Float }
        volumeAnimator.start()
    }
}
