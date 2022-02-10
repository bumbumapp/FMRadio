package io.bumbumapps.radio.internetradioplayer.domain.model

import android.media.MediaMetadataRetriever
import io.bumbumapps.radio.internetradioplayer.data.db.entity.Group
import io.bumbumapps.radio.internetradioplayer.di.Scopes
import io.bumbumapps.radio.internetradioplayer.extensions.Formats
import timber.log.Timber
import java.io.File
import kotlin.math.roundToInt


/**
 * Created by Vladimir Mikhalev 02.02.2019.
 */

data class Record(override val id: String,
                  override val name: String,
                  override val uri: String,
                  val file: File,
                  val createdAt: Long,
                  val duration: Long) : Media {

    override val group: String = Group.DEFAULT_NAME
    override val specs: String
    override val description: String? = null
    override val genre: String? = null
    override val language: String? = null
    override val location: String? = null
    override val url: String? = null

    val createdAtString = Formats.dateTime(createdAt)
    val durationString = Formats.duration(duration)
    private val sizeMb: Double = run { (file.length() * 100 / 1024.0 / 1024.0).roundToInt() / 100.0 }

    init {
        specs = "$durationString, $sizeMb MB"
    }

    companion object {
        fun fromFile(file: File): Record {
            val uri = file.toURI().toString()
            return Record(
                    id = uri,
                    name = file.name.substringBeforeLast('.'),
                    uri = uri,
                    file = file,
                    createdAt = file.lastModified(),
                    duration = getDuration(file)
            )
        }

        fun newRecord(file: File): Record {
            val uri = file.toURI().toString()
            return Record(
                    id = uri,
                    name = file.name.substringBeforeLast('.'),
                    uri = uri,
                    file = file,
                    createdAt = 0,
                    duration = 0
            )
        }

        private fun getDuration(file: File): Long {
            return try {
                val mmr = Scopes.app.getInstance(MediaMetadataRetriever::class.java)
                mmr.setDataSource(file.absolutePath)
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong()
            } catch (e: Exception) {
                Timber.e(e, file.absolutePath)
                //todo handle unknown duration (-1)
                0
            }
        }
    }

    fun commit() = copy(createdAt = System.currentTimeMillis(), duration = getDuration(file))
}


