package io.bumbumapps.radio.internetradioplayer.presentation.favorite.records

import io.bumbumapps.radio.internetradioplayer.domain.model.Record
import io.bumbumapps.radio.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 13.02.2019.
 */

interface RecordsView : BaseView {

    fun setRecords(records: List<Record>)

    fun selectRecord(id: String)
}