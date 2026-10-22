package io.bumbumapps.radio.internetradioplayer.presentation.navigation.cicerone

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The in-tree Cicerone replacement must buffer commands and keep Cicerone 3 command semantics. */
class RouterTest {

    private class RecordingNavigator : Navigator {
        val applied = mutableListOf<Command>()
        override fun applyCommands(commands: Array<out Command>?) {
            commands?.let { applied.addAll(it) }
        }
    }

    @Test
    fun commandsAreBufferedUntilNavigatorIsSet() {
        val cicerone = Cicerone.create(Router())
        cicerone.router.navigateTo("A")
        val navigator = RecordingNavigator()
        cicerone.navigatorHolder.setNavigator(navigator)
        assertEquals(1, navigator.applied.size)
        assertEquals("A", (navigator.applied[0] as Forward).screenKey)
    }

    @Test
    fun newRootScreenClearsStackThenReplaces() {
        val cicerone = Cicerone.create(Router())
        val navigator = RecordingNavigator()
        cicerone.navigatorHolder.setNavigator(navigator)
        cicerone.router.newRootScreen("ROOT")
        assertNull((navigator.applied[0] as BackTo).screenKey)
        assertEquals("ROOT", (navigator.applied[1] as Replace).screenKey)
    }

    @Test
    fun finishChainGoesBackToRootThenExits() {
        val cicerone = Cicerone.create(Router())
        val navigator = RecordingNavigator()
        cicerone.navigatorHolder.setNavigator(navigator)
        cicerone.router.finishChain()
        assertTrue(navigator.applied[0] is BackTo)
        assertTrue(navigator.applied[1] is Back)
    }
}
