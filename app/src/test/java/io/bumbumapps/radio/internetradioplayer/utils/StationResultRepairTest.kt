package io.bumbumapps.radio.internetradioplayer.utils

import io.bumbumapps.radio.internetradioplayer.data.net.ubermodel.StationResult
import org.junit.Assert.assertEquals
import org.junit.Test

class StationResultRepairTest {
    @Test
    fun stationListDescriptionIsRepaired() {
        val s = StationResult(1, "3chords", "Rock", "Sondaschule", "SchÃ¶ne GrÃ¼ÃŸe Vom Meer").toStation()
        assertEquals("Sondaschule - Schöne Grüße Vom Meer", s.description)
    }

    @Test
    fun genreUsedWhenNoSong() {
        assertEquals("Rock", StationResult(1, "x", "Rock", null, null).toStation().description)
    }
}
