package io.bumbumapps.radio.internetradioplayer.data.service.recorder

import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.TeeDataSource
import com.google.android.exoplayer2.util.Util
import io.bumbumapps.radio.internetradioplayer.BuildConfig
import io.bumbumapps.radio.internetradioplayer.R
import io.bumbumapps.radio.internetradioplayer.data.service.PlayerCallback
import io.bumbumapps.radio.internetradioplayer.data.service.player.AudioRenderersFactory
import io.bumbumapps.radio.internetradioplayer.data.service.player.ErrorHandlingPolicy
import io.bumbumapps.radio.internetradioplayer.data.service.player.IcyHttpDataSource
import io.bumbumapps.radio.internetradioplayer.extensions.runOnUiThread
import io.bumbumapps.radio.internetradioplayer.extensions.wifiManager
import okhttp3.OkHttpClient
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 09.02.2019.
 */

class Recorder
@Inject constructor(private val context: Context,
                    private val httpClient: OkHttpClient,
                    private val recorderDataSink: RecorderDataSink) {

    var playerCallback: PlayerCallback? = null
    private var player: SimpleExoPlayer? = null

    private val wifiLock = context.wifiManager
            .createWifiLock(WifiManager.WIFI_MODE_FULL, BuildConfig.APPLICATION_ID)

    fun startRecord(name: String, uri: Uri) {
        runOnUiThread {
            if (player == null) createPlayer()
            if (!wifiLock.isHeld) wifiLock.acquire()
            preparePlayer(name, uri)
            player?.playWhenReady = false
        }
    }

    fun stopRecord() {
        runOnUiThread {
            player?.stop()
            releasePlayer()
            if (wifiLock.isHeld) wifiLock.release()
        }
    }

    private fun createPlayer() {
        player = ExoPlayerFactory.newSimpleInstance(context, AudioRenderersFactory(context),
                DefaultTrackSelector(), RecorderLoadControl())
        player?.addListener(playerCallback)
    }

    private fun releasePlayer() {
        player?.removeListener(playerCallback)
        player?.release()
        player = null
    }

    private fun preparePlayer(name: String, uri: Uri) {
        val userAgent = Util.getUserAgent(context, context.getString(R.string.app_name))
        val mediaSource = ExtractorMediaSource.Factory {
            val icyHttpDataSource = IcyHttpDataSource(httpClient, userAgent, null)
            TeeDataSource(icyHttpDataSource, recorderDataSink)
        }
                .setLoadErrorHandlingPolicy(ErrorHandlingPolicy())
                .setCustomCacheKey(name)
                .createMediaSource(uri)

        player?.prepare(mediaSource)
    }
}