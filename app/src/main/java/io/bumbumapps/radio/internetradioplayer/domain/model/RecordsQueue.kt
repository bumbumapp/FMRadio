package io.bumbumapps.radio.internetradioplayer.domain.model

/**
 * Created by Vladimir Mikhalev 07.03.2019.
 */

class RecordsQueue(private val records: List<Record>) : List<Record> by records, MediaQueue {

    override fun getNext(id: String): Media? {
        val currIndex = records.indexOfFirst { it.id == id }
        if (currIndex == -1 || size == 1) return null
        return records[(currIndex + 1) % records.size]
    }

    override fun getPrevious(id: String): Media? {
        val currIndex = records.indexOfFirst { it.id == id }
        if (currIndex == -1 || size == 1) return null
        return records[(records.size + currIndex - 1) % records.size]
    }

}