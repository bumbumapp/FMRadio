package io.bumbumapps.radio.internetradioplayer.data.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import io.bumbumapps.radio.internetradioplayer.R
import io.bumbumapps.radio.internetradioplayer.data.db.entity.Station
import io.bumbumapps.radio.internetradioplayer.domain.model.Icon
import io.bumbumapps.radio.internetradioplayer.extensions.toUri
import io.bumbumapps.radio.internetradioplayer.presentation.root.RootActivity
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.01.2018.
 */

class ShortcutHelper
@Inject constructor(private val context: Context) {

    companion object {
        const val EXTRA_PLAY = "play"
        const val EXTRA_STATION_NAME = "station_name"
        const val EXTRA_DUPLICATE = "duplicate"
    }

    fun pinShortcut(station: Station, startPlay: Boolean): Boolean {
        val label = if (station.name.isBlank()) "Default name" else station.name
        val icon = Icon.randomIcon(label.hashCode().toLong())

        val info = ShortcutInfoCompat.Builder(context, station.id)
                .setShortLabel(label)
                .setLongLabel(label)
                .setIcon(IconCompat.createWithBitmap(icon.getBitmap(context, withBackground = true)))
                .setIntent(createShortcutIntent(station, startPlay))
                .setDisabledMessage(context.getString(R.string.msg_shortcut_remove))
                .build()

        return ShortcutManagerCompat.requestPinShortcut(context, info, null)
    }

    @Suppress("DEPRECATION")
    @SuppressLint("NewApi")
    fun removeShortcut(station: Station) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            shortcutManager.disableShortcuts(listOf(station.id),
                    context.getString(R.string.msg_shortcut_remove))
        } else {
            val removeIntent = Intent().apply {
                putExtra(Intent.EXTRA_SHORTCUT_INTENT, createShortcutIntent(station, true))
                putExtra(Intent.EXTRA_SHORTCUT_NAME, station.name)
                action = "com.android.launcher.action.UNINSTALL_SHORTCUT"
            }
            context.sendBroadcast(removeIntent)
        }
    }

    private fun createShortcutIntent(station: Station, startPlay: Boolean): Intent {
        return Intent(context, RootActivity::class.java).apply {
            putExtra(EXTRA_STATION_NAME, station.name)
            putExtra(EXTRA_PLAY, startPlay)
            putExtra(EXTRA_DUPLICATE, false)
            data = station.uri.toUri()
            action = Intent.ACTION_VIEW
        }
    }
}