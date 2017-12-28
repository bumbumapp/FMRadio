package io.github.vladimirmi.radius.model.repository

import android.net.Uri
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.radius.extensions.toSingle
import io.github.vladimirmi.radius.model.entity.GroupingList
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.manager.Preferences
import io.github.vladimirmi.radius.model.source.StationSource
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class StationListRepository
@Inject constructor(private val stationSource: StationSource,
                    private val preferences: Preferences) {

    val stationList: GroupingList = GroupingList(ArrayList())
    val currentStation: BehaviorRelay<Station> = BehaviorRelay.createDefault(Station.nullObject())

    fun initStations() {
        stationList.addAll(stationSource.getStationList())
        if (stationList.size > preferences.currentPos) {
            currentStation.accept(stationList[preferences.currentPos])
        }
        preferences.hidedGroups.forEach { stationList.hideGroup(it) }
    }

    fun setCurrentStation(station: Station) {
        val pos = stationList.indexOf(station)
        currentStation.accept(stationList[pos])
        preferences.currentPos = pos
    }

    fun createStation(uri: Uri): Single<Station> {
        return { stationSource.parseStation(uri) }
                .toSingle()
    }

    fun updateStation(newStation: Station): Completable {
        return Completable.fromCallable {
            Timber.e("updateStation: $stationList")
            stationList[stationList.indexOfFirst { it.id == newStation.id }] = newStation
            saveStation(newStation)
        }
    }

    fun addStation(station: Station): Completable {
        return Completable.fromCallable {
            stationList.add(station)
            saveStation(station)
        }
    }

    fun removeStation(station: Station): Completable {
        return Completable.fromCallable {
            if (stationList.remove(station)) {
                stationSource.removeStation(station)
            }
        }
    }

    fun showGroup(group: String) {
        stationList.showGroup(group)
        preferences.hidedGroups = preferences.hidedGroups.toMutableSet().apply { remove(group) }
    }

    fun hideGroup(group: String) {
        stationList.hideGroup(group)
        preferences.hidedGroups = preferences.hidedGroups.toMutableSet().apply { add(group) }
    }

    private fun saveStation(station: Station) {
        stationSource.saveStation(station)
    }
}
