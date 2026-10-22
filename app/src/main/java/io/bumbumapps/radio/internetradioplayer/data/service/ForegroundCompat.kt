package io.bumbumapps.radio.internetradioplayer.data.service

import android.app.Notification
import android.app.Service
import androidx.core.app.ServiceCompat
import timber.log.Timber

/**
 * startForeground with an explicit service type (required when targeting Android 14+).
 * Returns false instead of crashing when the system refuses (e.g. background start on Android 12+).
 */
fun startForegroundCompat(service: Service, id: Int, notification: Notification, type: Int): Boolean {
    return try {
        ServiceCompat.startForeground(service, id, notification, type)
        true
    } catch (e: IllegalStateException) {
        // ForegroundServiceStartNotAllowedException / missing type permission
        Timber.w(e, "startForeground refused")
        false
    } catch (e: SecurityException) {
        Timber.w(e, "startForeground refused")
        false
    }
}
