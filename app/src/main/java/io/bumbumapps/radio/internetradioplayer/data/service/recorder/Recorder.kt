package io.bumbumapps.radio.internetradioplayer.data.service.recorder

import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.TeeDataSource
import com.google.android.exoplayer2.util.Util
import io.bumbumapps.radio.internetradioplayer.BuildConfig
import io.bumbumapps.radio.internetradioplayer.R
import io.bumbumapps.radio.internetradioplayer.data.service.PlayerCallback
import io.bumbumapps.radio.internetradioplayer.data.service.player.AudioRenderersFactory
import io.bumbumapps.radio.internetradioplayer.data.service.player.ErrorHandlingPolicy
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
    private var player: ExoPlayer? = null

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
        player = ExoPlayer.Builder(context, AudioRenderersFactory(context))
                .setLoadControl(RecorderLoadControl())
                .build()
        playerCallback?.let { player?.addListener(it) }
    }

    private fun releasePlayer() {
        playerCallback?.let { player?.removeListener(it) }
        player?.release()
        player = null
    }

    private fun preparePlayer(name: String, uri: Uri) {
        val userAgent = Util.getUserAgent(context, context.getString(R.string.app_name))
        val httpFactory = OkHttpDataSource.Factory(httpClient).setUserAgent(userAgent)
        val teeFactory = DataSource.Factory { TeeDataSource(httpFactory.createDataSource(), recorderDataSink) }
        val mediaItem = MediaItem.Builder().setUri(uri).setCustomCacheKey(name).build()
        val mediaSource = ProgressiveMediaSource.Factory(teeFactory)
                .setLoadErrorHandlingPolicy(ErrorHandlingPolicy())
                .createMediaSource(mediaItem)

        player?.setMediaSource(mediaSource)
        player?.prepare()
    }
}