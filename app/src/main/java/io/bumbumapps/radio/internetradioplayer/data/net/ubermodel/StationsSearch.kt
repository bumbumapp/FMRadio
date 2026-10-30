package io.bumbumapps.radio.internetradioplayer.data.net.ubermodel

import io.bumbumapps.radio.internetradioplayer.utils.repairUtf8
import com.google.gson.annotations.SerializedName
import io.bumbumapps.radio.internetradioplayer.data.db.entity.Station
import java.util.*

/**
 * Created by Vladimir Mikhalev 15.11.2018.
 */

class StationsSearch(val result: List<StationResult>)

class StationResult(
        @SerializedName("station_id") val id: Int,
        val callsign: String,
        val genre: String,
        val artist: String?,
        val title: String?
) {

    val uri get() = "$URI_BASE$id"

    fun toStation(): Station {
        // Uberstations sends some titles double-encoded (HÃ¤gele); repair like TopSongsSearch.
        val a = artist?.repairUtf8() ?: ""
        val t = title?.repairUtf8() ?: ""
        val desc = if (a.isBlank() || t.isBlank()) genre else "$a - $t"
        return Station(
                id = UUID.randomUUID().toString(),
                name = callsign.repairUtf8(),
                uri = uri,
                description = desc
        )
    }
}
