package io.bumbumapps.radio.internetradioplayer.data.db.entity

import android.content.Context
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import io.bumbumapps.radio.internetradioplayer.R
import java.util.*

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

@Entity(indices = [Index(value = ["name"], unique = true)])
data class Group(
        @PrimaryKey
        val id: String = UUID.randomUUID().toString(),
        val name: String,
        val expanded: Boolean = true,
        val order: Int = 0,
        val equalizerPreset: String? = null
) {

    @Ignore var stations: List<Station> = arrayListOf()

    companion object {
        const val DEFAULT_ID = "default_id"
        const val NULL_ID = "null_id"
        const val DEFAULT_NAME = "default_name"

        fun default() = Group(DEFAULT_ID, DEFAULT_NAME, true, 0)

        fun nullObj() = Group(NULL_ID, "", true, 0)

        fun getViewName(name: String, context: Context): String {
            return if (name == DEFAULT_NAME) context.getString(R.string.default_group)
            else name
        }

        fun getDbName(name: String, context: Context): String {
            return if (name == context.getString(R.string.default_group) || name.isBlank()) {
                DEFAULT_NAME
            } else name
        }
    }

    fun isDefault() = id == DEFAULT_ID

    fun isNull() = id == NULL_ID
}
