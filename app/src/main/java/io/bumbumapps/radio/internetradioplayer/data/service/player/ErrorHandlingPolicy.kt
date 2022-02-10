package io.bumbumapps.radio.internetradioplayer.data.service.player

import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy

/**
 * Created by Vladimir Mikhalev 23.01.2019.
 */

class ErrorHandlingPolicy : DefaultLoadErrorHandlingPolicy() {

    override fun getMinimumLoadableRetryCount(dataType: Int): Int {
        return 10
    }
}