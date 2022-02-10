package io.bumbumapps.radio.internetradioplayer.presentation.search

import io.bumbumapps.radio.internetradioplayer.data.preference.Preferences
import io.bumbumapps.radio.internetradioplayer.di.Scopes
import io.bumbumapps.radio.internetradioplayer.presentation.navigation.NavigationHolderFragment
import io.bumbumapps.radio.internetradioplayer.presentation.navigation.ScreenContext
import io.bumbumapps.radio.internetradioplayer.presentation.navigation.SearchNavigationTree

/**
 * Created by Vladimir Mikhalev 20.03.2019.
 */

class SearchFragment : NavigationHolderFragment() {

    val preferences: Preferences by lazy {
        Scopes.app.getInstance(Preferences::class.java)
    }

    override fun getFirstScreen(): ScreenContext {
        return SearchNavigationTree.getScreen(preferences.searchScreenId)
    }

    override fun onScreenChange(screen: ScreenContext) {
        preferences.searchScreenId = screen.id
    }
}