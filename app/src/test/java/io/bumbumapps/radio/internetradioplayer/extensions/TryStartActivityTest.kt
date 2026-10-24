package io.bumbumapps.radio.internetradioplayer.extensions

import android.content.ActivityNotFoundException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Android 11+: resolveActivity() hid browsers/pickers, so station links and backup restore did nothing. */
class TryStartActivityTest {
    @Test
    fun startsWhenAnAppHandlesTheIntent() {
        var started = false
        assertTrue(tryStartActivity { started = true })
        assertTrue(started)
    }

    @Test
    fun reportsMissingAppInsteadOfCrashing() {
        assertFalse(tryStartActivity { throw ActivityNotFoundException() })
    }
}
