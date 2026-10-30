package io.bumbumapps.radio.internetradioplayer

import io.bumbumapps.radio.internetradioplayer.presentation.search.HomeGenres
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeGenresTest {
    @Test
    fun picksFeaturedInOrderIgnoringCaseAndMissing() {
        val available = listOf("70's", "80's", "rock", "Jazz", "Hit Music", "Metal")
        assertEquals(listOf("Hit Music", "rock", "Jazz", "80's"), HomeGenres.pick(available))
    }

    @Test
    fun respectsLimit() {
        assertEquals(listOf("Hit Music", "Rock"), HomeGenres.pick(HomeGenres.FEATURED, limit = 2))
    }
}
