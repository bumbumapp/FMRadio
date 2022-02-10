package io.bumbumapps.radio.internetradioplayer.data.utils

import android.content.Context
import android.net.Uri
import android.util.Xml
import androidx.core.content.FileProvider
import io.bumbumapps.radio.internetradioplayer.BuildConfig
import io.bumbumapps.radio.internetradioplayer.data.db.entity.Group
import io.bumbumapps.radio.internetradioplayer.data.db.entity.Station
import io.bumbumapps.radio.internetradioplayer.data.repository.FavoritesRepository
import io.bumbumapps.radio.internetradioplayer.extensions.clear
import io.reactivex.Completable
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.StringWriter
import java.util.*
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 02.10.2018.
 */

const val BACKUP_TYPE = "text/xml"
private const val BACKUP_NAME = "stations_backup.xml"
private const val BACKUP_ENCODING = "UTF-8"
private const val BACKUP_VERSION = 5

private const val DATA_TAG = "data"
private const val STATIONS_TAG = "stations"
private const val STATION_TAG = "station"
private const val GROUPS_TAG = "groups"
private const val GROUP_TAG = "group"

private const val VERSION_ATTR = "version"
private const val NAME_ATTR = "name"
private const val GROUP_NAME_ATTR = "group"
private const val URI_ATTR = "streamUri"
private const val URL_ATTR = "url"
private const val ENCODING_ATTR = "encoding"
private const val BITRATE_ATTR = "bitrate"
private const val SAMPLE_ATTR = "sample"
private const val ORDER_ATTR = "order"
private const val DESCRIPTION_ATTR = "description"
private const val GENRE_ATTR = "genre"
private const val LANGUAGE_ATTR = "language"
private const val LOCATION_ATTR = "location"
private const val EXPANDED_ATTR = "expanded"

class BackupRestoreHelper
@Inject constructor(private val repository: FavoritesRepository,
                    private val context: Context) {

    private val ns: String? = null

    fun createBackup(): Uri {
        val file = File(context.cacheDir, BACKUP_NAME)
        if (file.exists()) file.clear()
        file.writeText(createXml(repository.groups))

        return FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
    }

    private fun createXml(groups: List<Group>): String {
        val serializer = Xml.newSerializer()
        val writer = StringWriter()
        serializer.setOutput(writer)

        serializer.startDocument(BACKUP_ENCODING, null)
        serializer.startTag(ns, DATA_TAG)
        serializer.attribute(ns, VERSION_ATTR, BACKUP_VERSION.toString())

        writeStations(serializer, groups)
        writeGroups(serializer, groups)

        serializer.endTag(ns, DATA_TAG)
        serializer.endDocument()

        return writer.toString()
    }

    private fun writeStations(serializer: XmlSerializer, groups: List<Group>) {
        fun writeStation(station: Station, groupName: String) {
            serializer.startTag(ns, STATION_TAG)
            with(station) {
                serializer.attribute(ns, NAME_ATTR, name)
                serializer.attribute(ns, GROUP_NAME_ATTR, groupName)
                serializer.attribute(ns, URI_ATTR, uri)
                url?.let { serializer.attribute(ns, URL_ATTR, it) }
                encoding?.let { serializer.attribute(ns, ENCODING_ATTR, it) }
                bitrate?.let { serializer.attribute(ns, BITRATE_ATTR, it) }
                sample?.let { serializer.attribute(ns, SAMPLE_ATTR, it) }
                serializer.attribute(ns, ORDER_ATTR, order.toString())
                description?.let { serializer.attribute(ns, DESCRIPTION_ATTR, it) }
                genre?.let { serializer.attribute(ns, GENRE_ATTR, it) }
                language?.let { serializer.attribute(ns, LANGUAGE_ATTR, it) }
                location?.let { serializer.attribute(ns, LOCATION_ATTR, it) }
            }
            serializer.endTag(ns, STATION_TAG)
        }

        serializer.startTag(ns, STATIONS_TAG)
        groups.forEach { group -> group.stations.forEach { writeStation(it, group.name) } }
        serializer.endTag(ns, STATIONS_TAG)
    }

    private fun writeGroups(serializer: XmlSerializer, groups: List<Group>) {
        serializer.startTag(ns, GROUPS_TAG)
        groups.forEach { group ->
            serializer.startTag(ns, GROUP_TAG)
            with(group) {
                serializer.attribute(ns, NAME_ATTR, name)
                serializer.attribute(ns, ORDER_ATTR, order.toString())
                serializer.attribute(ns, EXPANDED_ATTR, expanded.toString())
            }
            serializer.endTag(ns, GROUP_TAG)
        }
        serializer.endTag(ns, GROUPS_TAG)
    }

    fun restoreBackup(uri: Uri): Completable {
        val parser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        }
        val stations = arrayListOf<Station>()
        val groups = arrayListOf<Group>()
        val parse = Completable.fromCallable {
            context.contentResolver.openInputStream(uri).use {
                parser.setInput(it, null)
                while (parser.next() != XmlPullParser.END_DOCUMENT) {

                    if (parser.eventType == XmlPullParser.START_TAG) {
                        val version = parser.getAttributeValue(ns, VERSION_ATTR)?.toInt()
                        val stationsTag = if (version == 3) "data" else STATIONS_TAG

                        if (parser.name == stationsTag) stations.addAll(parseStations(parser, version
                                ?: BACKUP_VERSION))
                        if (parser.name == GROUPS_TAG) groups.addAll(parseGroups(parser))
                    }
                }
            }
        }
        return parse
                .andThen(Completable.defer {
                    Completable.merge(groups.map { repository.addGroup(it) })
                })
                .andThen(Completable.defer {
                    repository.getAllGroups().flatMapCompletable { groups ->
                        Completable.merge(stations.map { station ->

                            groups.find { it.name == station.group }?.let {
                                repository.addStation(station.copy(groupId = it.id))

                            } ?: repository.addGroup(Group.default())
                                    .andThen(repository.addStation(station))
                        })
                    }
                })
    }

    private fun parseStations(parser: XmlPullParser, version: Int): List<Station> {
        val stationsTag = if (version == 3) "data" else STATIONS_TAG
        val list = arrayListOf<Station>()
        while (!(parser.next() == XmlPullParser.END_TAG && parser.name == stationsTag)) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == STATION_TAG) {
                val station = Station(
                        id = UUID.randomUUID().toString(),
                        name = parser.getAttributeValue(ns, NAME_ATTR),
                        uri = parser.getAttributeValue(ns, URI_ATTR),
                        url = parser.getAttributeValue(ns, URL_ATTR),
                        encoding = parser.getAttributeValue(ns, ENCODING_ATTR),
                        bitrate = parser.getAttributeValue(ns, BITRATE_ATTR),
                        sample = parser.getAttributeValue(ns, SAMPLE_ATTR),
                        order = parser.getAttributeValue(ns, ORDER_ATTR).toInt(),
                        groupId = Group.DEFAULT_ID,
                        description = parser.getAttributeValue(ns, DESCRIPTION_ATTR),
                        genre = parser.getAttributeValue(ns, GENRE_ATTR),
                        language = parser.getAttributeValue(ns, LANGUAGE_ATTR),
                        location = parser.getAttributeValue(ns, LOCATION_ATTR)
                )
                val groupName = parser.getAttributeValue(ns, GROUP_NAME_ATTR)
                station.group = groupName
                list.add(station)
            }
        }
        return list
    }

    private fun parseGroups(parser: XmlPullParser): List<Group> {
        val list = arrayListOf<Group>()
        while (!(parser.next() == XmlPullParser.END_TAG && parser.name == GROUPS_TAG)) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == GROUP_TAG) {
                val name = parser.getAttributeValue(ns, NAME_ATTR)
                if (name == Group.DEFAULT_NAME) {
                    list.add(Group.default())
                    continue
                }
                val id = UUID.randomUUID().toString()
                val expanded = parser.getAttributeValue(ns, EXPANDED_ATTR)!!.toBoolean()
                val order = parser.getAttributeValue(ns, ORDER_ATTR).toInt()
                val group = Group(id, name, expanded, order)
                list.add(group)
            }
        }
        return list
    }
}

