package io.github.vladimirmi.radius.model.source

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.util.LruCache
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.extensions.clear
import io.github.vladimirmi.radius.extensions.getBitmap
import io.github.vladimirmi.radius.extensions.toURL
import io.github.vladimirmi.radius.extensions.useConnection
import io.github.vladimirmi.radius.model.entity.Icon
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


/**
 * Created by Vladimir Mikhalev 08.12.2017.
 */

class StationIconSource
@Inject constructor(private val context: Context) {

    @Suppress("PrivatePropertyName")
    private val FAVICON_BASE_URI = Uri.Builder().scheme("http")
            .authority("www.google.com")
            .path("s2/favicons").build()

    private val appDir = context.getExternalFilesDir(null)

    private val defaultIconBitmap: Bitmap
        get() = ContextCompat.getDrawable(context, R.drawable.ic_radius).getBitmap()

    private val maxSize = (Runtime.getRuntime().maxMemory() / 1024 / 10).toInt()
    private val bitmapCache = object : LruCache<String, Icon>(maxSize) {
        override fun sizeOf(key: String, value: Icon): Int {
            return value.bitmap.byteCount / 1024
        }
    }

    fun getIcon(path: String): Icon {
        val cache = bitmapCache.get(path)
        if (cache != null) return cache
        return if (path.contains("http")) loadFromNet(path) else loadFromFile(path)
    }

    fun getSavedIcon(path: String): Icon {
        return loadFromFile(path)
    }

    fun saveIcon(icon: Icon) {
        try {
            val file = File(appDir, "${icon.name}.png")
            if (file.exists()) file.clear()
            FileOutputStream(file).use {
                icon.bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        } catch (e: IOException) {
            Timber.e(e)
        }
    }

    fun removeIcon(name: String) {
        bitmapCache.remove(name)
        File(appDir, "$name.png").delete()
    }

    fun cacheIcon(icon: Icon) {
        bitmapCache.put(icon.name, icon)
    }

    private fun loadFromNet(url: String): Icon {
        val host = Uri.parse(url).host
        val faviconUrl = FAVICON_BASE_URI.buildUpon()
                .appendQueryParameter("domain", host)
                .build().toURL() ?: return Icon("default", defaultIconBitmap)

        Timber.e("loadFromNet: $faviconUrl")
        val bitmap = faviconUrl.useConnection {
            BitmapFactory.decodeStream(inputStream)
        } ?: defaultIconBitmap
        return Icon(url, bitmap)
    }

    private fun loadFromFile(path: String): Icon {
        val file = File(appDir, "$path.png")
        if (!file.exists()) return Icon("default", defaultIconBitmap)

        Timber.e("loadFromFile: ${file.path}")
        val bitmap = FileInputStream(file).use {
            BitmapFactory.decodeStream(it)
        } ?: defaultIconBitmap
        //todo decode meta
        return Icon(path, bitmap)
    }
}