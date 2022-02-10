package io.bumbumapps.radio.internetradioplayer.presentation.search

import io.bumbumapps.radio.internetradioplayer.domain.model.Suggestion
import io.bumbumapps.radio.internetradioplayer.presentation.base.BaseView
import io.bumbumapps.radio.internetradioplayer.presentation.data.DataView

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

interface ManualSearchView : BaseView, DataView {

    fun addRecentSuggestions(list: List<Suggestion>)

    fun addRegularSuggestions(list: List<Suggestion>)

    fun selectSuggestion(suggestion: Suggestion)

    fun showPlaceholder(show: Boolean)

    fun enableRefresh(enable: Boolean)
}
