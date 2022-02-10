package io.bumbumapps.radio.internetradioplayer.data.service.extensions

import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import com.google.android.exoplayer2.C
import io.bumbumapps.radio.internetradioplayer.R
import io.bumbumapps.radio.internetradioplayer.di.Scopes
import io.bumbumapps.radio.internetradioplayer.domain.model.Media

/**
 * Created by Vladimir Mikhalev 01.02.2018.
 */

val MediaMetadataCompat.artist: String
    get() = getString(MediaMetadataCompat.METADATA_KEY_ARTIST) ?: ""
val MediaMetadataCompat.title: String
    get() = getString(MediaMetadataCompat.METADATA_KEY_TITLE) ?: ""
val MediaMetadataCompat.album: String
    get() = getString(MediaMetadataCompat.METADATA_KEY_ALBUM) ?: ""
val MediaMetadataCompat.art: Bitmap?
    get() = getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)
val MediaMetadataCompat.duration: Long
    get() = getLong(MediaMetadataCompat.METADATA_KEY_DURATION)

val EMPTY_METADATA: MediaMetadataCompat
    get() = MediaMetadataCompat.Builder().build()
            .setDuration(C.TIME_UNSET)

fun MediaMetadataCompat.setArtistTitle(metadata: String): MediaMetadataCompat {

    val artistTitle = metadata.substringAfter("StreamTitle=", "")
            .substringBefore(';')
            .trim(' ', '\'')

    var (artist, title) = if (artistTitle.contains(" - ")) {
        artistTitle.split(" - ", limit = 2)
    } else {
        listOf("", artistTitle)
    }

    if (title.endsWith(']')) title = title.substringBeforeLast('[')
    if (title.isBlank() || artist.isBlank()) {
        title = ""
        artist = Scopes.context.getString(R.string.metadata_not_available)
    }
    return MediaMetadataCompat.Builder(this)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .build()
}

fun MediaMetadataCompat.setMedia(media: Media): MediaMetadataCompat {
    return MediaMetadataCompat.Builder(this)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, media.name)
            .build()
}

fun MediaMetadataCompat.clearArtistTitle(): MediaMetadataCompat {
    return MediaMetadataCompat.Builder(this)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "")
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "")
            .build()
}

fun MediaMetadataCompat.isNotSupported(): Boolean {
    return artist == Scopes.context.getString(R.string.metadata_not_available)
}

fun MediaMetadataCompat.isEmpty(): Boolean {
    return title.isBlank() && artist.isBlank()
}

fun MediaMetadataCompat.setDuration(durationMs: Long): MediaMetadataCompat {
    return MediaMetadataCompat.Builder(this)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs)
            .build()
}

fun MediaMetadataCompat.eq(other: MediaMetadataCompat?): Boolean {
    return other != null && artist == other.artist && title == other.title
}
