package io.bumbumapps.radio.internetradioplayer.domain.interactor

import io.bumbumapps.radio.internetradioplayer.R
import io.bumbumapps.radio.internetradioplayer.data.preference.Preferences
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 20.11.2018.
 */

class MainInteractor
@Inject constructor(private val prefs: Preferences) {

    fun saveMainPageId(pageId: Int) {
        prefs.mainPageId = pageId
    }

    fun getMainPageId(): Int {
        val id = prefs.mainPageId
        return if (id == R.id.nav_settings || id == R.id.nav_equalizer) R.id.nav_search
        else id
    }

    fun saveFavoritePageId(pageId: Int) {
        prefs.favoritePageId = pageId
    }

    fun getFavoritePageId(): Int {
        return prefs.favoritePageId
    }
}