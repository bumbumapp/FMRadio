package io.bumbumapps.radio.internetradioplayer.presentation.equalizer

import io.bumbumapps.radio.internetradioplayer.domain.model.EqualizerConfig
import io.bumbumapps.radio.internetradioplayer.domain.model.EqualizerPreset
import io.bumbumapps.radio.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 09.01.2019.
 */

interface EqualizerView : BaseView {

    fun setupEqualizer(config: EqualizerConfig)

    fun setPresetNames(presets: List<String>)

    fun setPreset(preset: EqualizerPreset)

    fun setBindIcon(iconResId: Int)

    fun showReset(show: Boolean)

    fun enableEqualizer(enabled: Boolean)
}