package io.github.vladimirmi.radius.model.repository

import android.graphics.Bitmap
import android.net.Uri
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.radius.extensions.toSingle
import io.github.vladimirmi.radius.model.entity.GroupedList
import io.github.vladimirmi.radius.model.entity.GroupingList
import io.github.vladimirmi.radius.model.entity.PlayerMode
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.manager.Preferences
import io.github.vladimirmi.radius.model.source.StationIconSource
import io.github.vladimirmi.radius.model.source.StationSource
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.toSingle
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class StationRepository
@Inject constructor(private val stationSource: StationSource,
                    private val stationIconSource: StationIconSource,
                    private val preferences: Preferences) {

    private lateinit var stationList: GroupingList
    val groupedStationList: GroupedList<Station> get() = stationList
    val currentStation: BehaviorRelay<Station> = BehaviorRelay.create()
    val playerMode: BehaviorRelay<PlayerMode> = BehaviorRelay.create()
    var newStation: Station? = null

    fun initStations() {
        stationList = GroupingList(stationSource.getStationList())
        if (stationList.size > preferences.currentPos) {
            currentStation.accept(stationList[preferences.currentPos])
        }
        preferences.hidedGroups.forEach { stationList.hideGroup(it) }
        updatePlayerMode()
    }

    fun setCurrentStation(station: Station) {
        val pos = stationList.indexOf(station)
        currentStation.accept(stationList[pos])
        preferences.currentPos = pos
    }

    fun parseStation(uri: Uri): Completable {
        return { stationSource.parseStation(uri) }
                .toSingle()
                .subscribeOn(Schedulers.io())
                .doOnSuccess {
                    newStation = it
                    playerMode.accept(PlayerMode.NEXT_PREVIOUS_DISABLED)
                }
                .flatMapCompletable {
                    Completable.fromCallable { stationIconSource.getIcon(it.uri, it.title) }
                }
    }

    fun showOrHideGroup(group: String) {
        if (stationList.isGroupVisible(group)) {
            stationList.hideGroup(group)
            preferences.hidedGroups = preferences.hidedGroups.toMutableSet().apply { add(group) }
        } else {
            stationList.showGroup(group)
            preferences.hidedGroups = preferences.hidedGroups.toMutableSet().apply { remove(group) }
        }
        updatePlayerMode()
    }

    fun updateStation(newStation: Station) {
        val oldStation = currentStation.value
        if (oldStation != newStation) {
            stationList[stationList.indexOfFirst { it.id == newStation.id }] = newStation
            stationSource.saveStation(newStation)
//            stationIconSource.saveIcon(newStation.title, getStationIcon(oldStation.title).blockingGet())
            if (oldStation.title != newStation.title) {
                stationSource.removeStation(oldStation)
//                stationIconSource.removeIcon(oldStation)
            }
            currentStation.accept(newStation)
        }
    }

    fun cacheStationIcon(bitmap: Bitmap) {
        stationIconSource.cacheIcon(currentStation.value.title, bitmap)
    }

    fun addStation(station: Station): Boolean {
        if (stationList.find { it.title == station.title } != null) return false
        stationList.add(station)
        stationSource.saveStation(station)
        stationIconSource.saveIcon(station.title, stationIconSource.getIcon(station.title))
        setCurrentStation(station)
        updatePlayerMode()
        return true
    }

    fun removeStation(station: Station) {
        if (stationList.remove(station)) {
            stationSource.removeStation(station)
            updatePlayerMode()
        }
    }

    fun nextStation(): Boolean {
        val next = stationList.getNext(currentStation.value)
        return if (next != null) {
            setCurrentStation(next)
            true
        } else false
    }

    fun previousStation(): Boolean {
        val previous = stationList.getPrevious(currentStation.value)
        return if (previous != null) {
            setCurrentStation(previous)
            true
        } else false
    }

    fun getStationIcon(path: String = currentStation.value.title): Single<Bitmap> {
        return { stationIconSource.getIcon(path) }
                .toSingle()
                .subscribeOn(Schedulers.io())
    }

    private fun updatePlayerMode() {
        if (stationList.itemSize() > 1) {
            playerMode.accept(PlayerMode.NEXT_PREVIOUS_ENABLED)
        } else {
            playerMode.accept(PlayerMode.NEXT_PREVIOUS_DISABLED)
        }
    }
}
