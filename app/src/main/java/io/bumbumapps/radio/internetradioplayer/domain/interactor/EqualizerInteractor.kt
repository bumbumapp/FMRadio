package io.bumbumapps.radio.internetradioplayer.domain.interactor

import io.bumbumapps.radio.internetradioplayer.data.repository.EqualizerRepository
import io.bumbumapps.radio.internetradioplayer.data.repository.MediaRepository
import io.bumbumapps.radio.internetradioplayer.domain.model.EqualizerPreset
import io.bumbumapps.radio.internetradioplayer.domain.model.PresetBinderView
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.01.2019.
 */

class EqualizerInteractor
@Inject constructor(private val equalizerRepository: EqualizerRepository,
                    private val mediaRepository: MediaRepository) {

    val equalizerEnabledObs: Observable<Boolean> get() = equalizerRepository.equalizerEnabledObs
    val currentPresetObs: Observable<EqualizerPreset> get() = equalizerRepository.currentPresetObs
    val presetBinder: PresetBinderView get() = equalizerRepository.binder
    val equalizerConfig get() = equalizerRepository.equalizerConfig

    fun initPresets(): Completable {
        return equalizerRepository.getSavedPresets().map { entities ->
            val savedPresets = entities.map { EqualizerPreset.create(it) }
            val presets = equalizerRepository.equalizerConfig.defaultPresets.toMutableList()
            presets.forEachIndexed { index, preset ->
                savedPresets.find { it.name == preset.name }?.let { presets[index] = it }
            }
            equalizerRepository.presets = presets
        }
                .ignoreElement()
                .andThen(initCurrentPreset())
    }

    private fun initCurrentPreset(): Completable {
        return mediaRepository.currentMediaObs
                .flatMapSingle { equalizerRepository.createBinder(it.id) }
                .map { equalizerRepository.presets.indexOfFirst { preset -> preset.name == it.presetName } }
                .doOnNext { equalizerRepository.selectPreset(it) }
                .ignoreElements()
    }

    fun setBandLevel(band: Int, level: Int) {
        equalizerRepository.setBandLevel(band, level)
    }

    fun setBassBoostStrength(strength: Int) {
        equalizerRepository.setBassBoostStrength(strength)
    }

    fun setVirtualizerStrength(strength: Int) {
        equalizerRepository.setVirtualizerStrength(strength)
    }

    fun getPresetNames(): List<String> {
        return equalizerRepository.presets.map { it.name }
    }

    fun selectPreset(index: Int) {
        equalizerRepository.selectPreset(index)
    }

    fun saveCurrentPreset(): Completable {
        return equalizerRepository.saveCurrentPreset()
    }

    fun switchBind() {
        equalizerRepository.binder = equalizerRepository.binder.nextBinder()
    }

    fun bindPreset(): Completable {
        return equalizerRepository.bindPreset()
    }

    fun resetCurrentPreset(): Completable {
        return equalizerRepository.resetCurrentPreset()
    }

    fun isCurrentPresetCanReset(): Boolean {
        val preset = equalizerRepository.currentPreset
        val defaultPreset = equalizerConfig.defaultPresets.find { it.name == preset.name }
        return preset != defaultPreset
    }

    fun enableEqualizer(enabled: Boolean) {
        equalizerRepository.enableEqualizer(enabled)
    }
}