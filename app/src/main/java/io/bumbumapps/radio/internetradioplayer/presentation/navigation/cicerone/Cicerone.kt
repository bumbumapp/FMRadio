package io.bumbumapps.radio.internetradioplayer.presentation.navigation.cicerone

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import java.util.LinkedList

/*
 * Minimal in-tree replacement for ru.terrakok.cicerone 3.0.0 (only published on the retired jcenter).
 * Implements just the API surface this app uses, with the same semantics.
 */

interface Command
class Forward(val screenKey: String, val transitionData: Any?) : Command
class Replace(val screenKey: String, val transitionData: Any?) : Command
class BackTo(val screenKey: String?) : Command
class Back : Command

interface Navigator {
    fun applyCommands(commands: Array<out Command>?)
}

interface NavigatorHolder {
    fun setNavigator(navigator: Navigator)
    fun removeNavigator()
}

/** Buffers commands while no navigator is attached (e.g. activity paused). */
class CommandBuffer : NavigatorHolder {
    private var navigator: Navigator? = null
    private val pending = LinkedList<Array<out Command>>()

    override fun setNavigator(navigator: Navigator) {
        this.navigator = navigator
        while (pending.isNotEmpty()) {
            navigator.applyCommands(pending.poll())
        }
    }

    override fun removeNavigator() {
        navigator = null
    }

    fun executeCommands(commands: Array<out Command>) {
        val n = navigator
        if (n != null) n.applyCommands(commands) else pending.add(commands)
    }
}

open class Router {
    internal val commandBuffer = CommandBuffer()

    protected fun executeCommands(vararg commands: Command) {
        commandBuffer.executeCommands(commands)
    }

    fun navigateTo(screenKey: String, data: Any? = null) = executeCommands(Forward(screenKey, data))
    fun newRootScreen(screenKey: String, data: Any? = null) =
            executeCommands(BackTo(null), Replace(screenKey, data))
    fun replaceScreen(screenKey: String, data: Any? = null) = executeCommands(Replace(screenKey, data))
    fun backTo(screenKey: String?) = executeCommands(BackTo(screenKey))
    fun finishChain() = executeCommands(BackTo(null), Back())
    fun exit() = executeCommands(Back())
}

class Cicerone<T : Router> private constructor(val router: T) {
    val navigatorHolder: NavigatorHolder get() = router.commandBuffer

    companion object {
        fun <T : Router> create(router: T) = Cicerone(router)
    }
}

/** Fragment back-stack navigator, behaviour compatible with Cicerone 3 SupportAppNavigator. */
abstract class SupportAppNavigator(
        private val activity: FragmentActivity,
        private val containerId: Int
) : Navigator {

    private val fragmentManager: FragmentManager = activity.supportFragmentManager
    private val localStackCopy = LinkedList<String>()

    override fun applyCommands(commands: Array<out Command>?) {
        fragmentManager.executePendingTransactions()
        localStackCopy.clear()
        for (i in 0 until fragmentManager.backStackEntryCount) {
            localStackCopy.add(fragmentManager.getBackStackEntryAt(i).name ?: "")
        }
        commands?.forEach { applyCommand(it) }
    }

    open fun applyCommand(command: Command?) {
        when (command) {
            is Forward -> forward(command)
            is Replace -> replace(command)
            is BackTo -> backTo(command)
            is Back -> back()
        }
    }

    abstract fun createActivityIntent(context: Context, screenKey: String, data: Any?): Intent?
    abstract fun createFragment(screenKey: String, data: Any?): Fragment?

    open fun setupFragmentTransactionAnimation(command: Command, currentFragment: Fragment?,
                                               nextFragment: Fragment?,
                                               fragmentTransaction: FragmentTransaction) = Unit

    open fun unknownScreen(command: Command?) {
        throw RuntimeException("Can't create a screen for passed screenKey.")
    }

    private fun forward(command: Forward) {
        val intent = createActivityIntent(activity, command.screenKey, command.transitionData)
        if (intent != null) {
            activity.startActivity(intent)
            return
        }
        val fragment = createFragment(command.screenKey, command.transitionData)
        if (fragment == null) {
            unknownScreen(command)
            return
        }
        val tx = fragmentManager.beginTransaction()
        setupFragmentTransactionAnimation(command, fragmentManager.findFragmentById(containerId), fragment, tx)
        tx.replace(containerId, fragment).addToBackStack(command.screenKey).commit()
        localStackCopy.add(command.screenKey)
    }

    private fun replace(command: Replace) {
        val intent = createActivityIntent(activity, command.screenKey, command.transitionData)
        if (intent != null) {
            activity.startActivity(intent)
            activity.finish()
            return
        }
        val fragment = createFragment(command.screenKey, command.transitionData)
        if (fragment == null) {
            unknownScreen(command)
            return
        }
        if (localStackCopy.isNotEmpty()) {
            fragmentManager.popBackStack()
            localStackCopy.removeLast()
            val tx = fragmentManager.beginTransaction()
            setupFragmentTransactionAnimation(command, fragmentManager.findFragmentById(containerId), fragment, tx)
            tx.replace(containerId, fragment).addToBackStack(command.screenKey).commit()
            localStackCopy.add(command.screenKey)
        } else {
            val tx = fragmentManager.beginTransaction()
            setupFragmentTransactionAnimation(command, fragmentManager.findFragmentById(containerId), fragment, tx)
            tx.replace(containerId, fragment).commit()
        }
    }

    private fun backTo(command: BackTo) {
        val key = command.screenKey
        if (key == null || !localStackCopy.contains(key)) {
            backToRoot()
            return
        }
        while (localStackCopy.isNotEmpty() && localStackCopy.last != key) localStackCopy.removeLast()
        fragmentManager.popBackStack(key, 0)
    }

    private fun backToRoot() {
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        localStackCopy.clear()
    }

    private fun back() {
        if (localStackCopy.isNotEmpty()) {
            fragmentManager.popBackStack()
            localStackCopy.removeLast()
        } else {
            activity.finish()
        }
    }
}
