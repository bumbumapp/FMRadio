package io.bumbumapps.radio.internetradioplayer.presentation.navigation

import android.view.MenuItem
import io.bumbumapps.radio.internetradioplayer.R

/**
 * Created by Vladimir Mikhalev 04.12.2017.
 */

class Router : ru.terrakok.cicerone.Router() {

    companion object {
        const val MAIN_SCREEN = "MAIN_SCREEN"
        const val SETTINGS_SCREEN = "SETTINGS_SCREEN"
        const val EQUALIZER_SCREEN = "EQUALIZER_SCREEN"
        const val ROOT_SCREEN = MAIN_SCREEN
        const val DELIMITER = "$"
    }

    fun navigateTo(item: MenuItem) {
        when {
            item.itemId == R.id.nav_settings -> addScreen(item.itemId)
            else -> replaceScreen(item.itemId)
        }
    }

    fun addScreen(navId: Int) {
        navigateTo(navId.toScreenKey())
    }

    fun newRootScreen(navId: Int) {
        newRootScreen(navId.toScreenKey())
    }

    fun replaceScreen(navId: Int) {
        replaceScreen(navId.toScreenKey())
    }

    private fun Int.toScreenKey(): String {
        val screen = when (this) {
            R.id.nav_settings -> SETTINGS_SCREEN
            R.id.nav_equalizer -> EQUALIZER_SCREEN
            else -> MAIN_SCREEN
        }
        return "$screen$DELIMITER$this"
    }
}
