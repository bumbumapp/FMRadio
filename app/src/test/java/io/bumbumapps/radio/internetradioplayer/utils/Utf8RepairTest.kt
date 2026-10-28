package io.bumbumapps.radio.internetradioplayer.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class Utf8RepairTest {
    @Test
    fun repairsDoubleEncodedUmlaut() {
        assertEquals("Gregor Hägele", "Gregor HÃ¤gele".repairUtf8())
    }

    @Test
    fun repairsDoubleEncodedCyrillic() {
        val doubleEncoded = String("Кино".toByteArray(Charsets.UTF_8), Charsets.ISO_8859_1)
        assertEquals("Кино", doubleEncoded.repairUtf8())
    }

    @Test
    fun leavesCleanTextAlone() {
        assertEquals("Hägele", "Hägele".repairUtf8())
        assertEquals("Queen - Another One Bites The Dust", "Queen - Another One Bites The Dust".repairUtf8())
        assertEquals("Ãngel", "Ãngel".repairUtf8())
    }
}
