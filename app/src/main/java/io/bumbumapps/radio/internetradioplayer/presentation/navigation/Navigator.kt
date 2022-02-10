package io.bumbumapps.radio.internetradioplayer.presentation.navigation

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import io.bumbumapps.radio.internetradioplayer.R
import io.bumbumapps.radio.internetradioplayer.presentation.equalizer.EqualizerFragment
import io.bumbumapps.radio.internetradioplayer.presentation.main.MainFragment
import io.bumbumapps.radio.internetradioplayer.presentation.main.MainView
import io.bumbumapps.radio.internetradioplayer.presentation.settings.SettingsFragment
import ru.terrakok.cicerone.android.SupportAppNavigator
import ru.terrakok.cicerone.commands.*
import java.util.*

/**
 * Created by Vladimir Mikhalev 03.12.2017.
 */

class Navigator(private val activity: FragmentActivity, private val containerId: Int)
    : SupportAppNavigator(activity, containerId), Parcelable {

    private var currentNavId = 0
    var navigationIdListener: ((Int) -> Unit)? = null

    private val screenStack = LinkedList<String>()

    private val currScreenKeyFromBackStack: String
        get() {
            return with(activity.supportFragmentManager) {
                if (backStackEntryCount > 0) {
                    getBackStackEntryAt(backStackEntryCount - 1).name ?: Router.ROOT_SCREEN
                } else Router.ROOT_SCREEN
            }
        }

    init {
        activity.supportFragmentManager.addOnBackStackChangedListener {
            applyToStack(BackStackScreenNameChange(currScreenKeyFromBackStack.screenName))
            notifyNavigationListener()
        }
    }

    override fun createActivityIntent(context: Context, screenKey: String, data: Any?) = null

    override fun createFragment(screenKey: String, data: Any?): Fragment? {
        if (screenKey.navId == currentNavId) return null

        return when (screenKey.screenName) {
            Router.MAIN_SCREEN -> {
                val navId = screenKey.navId
                val fragment = getCurrentFragment()
                if (fragment is MainView) {
                    fragment.setPageId(navId)
                    return null
                }
                return MainFragment.newInstance(navId)
            }
            Router.SETTINGS_SCREEN -> SettingsFragment()
            Router.EQUALIZER_SCREEN -> EqualizerFragment()
            else -> null
        }
    }

    override fun setupFragmentTransactionAnimation(
            command: Command,
            currentFragment: Fragment?,
            nextFragment: Fragment?,
            fragmentTransaction: FragmentTransaction) {

        when (command) {
            is Forward -> forwardTransition(fragmentTransaction)
            is Back, is BackTo -> backTransition(fragmentTransaction)
            is Replace -> replaceTransition(fragmentTransaction)
        }
    }

    override fun applyCommands(commands: Array<out Command>?) {
        super.applyCommands(commands)
        notifyNavigationListener()
    }

    override fun applyCommand(command: Command?) {
        super.applyCommand(command)
        applyToStack(command)
    }

    override fun unknownScreen(command: Command?) {
        //no-op
    }

    private fun notifyNavigationListener() {
        val navId = screenStack.peek()?.navId
        if (navId != null && navId != currentNavId) {
            navigationIdListener?.invoke(navId)
            currentNavId = navId
        }
    }

    private fun applyToStack(command: Command?) {
        when (command) {
            is Forward -> {
                screenStack.push(command.screenKey)
            }
            is Back -> screenStack.poll()
            is Replace -> {
                screenStack.poll()
                screenStack.push(command.screenKey)
            }
            is BackTo -> {
                while (screenStack.peek() != command.screenKey) {
                    screenStack.poll()
                }
            }
            is BackStackScreenNameChange -> {
                while (screenStack.peek()?.screenName != command.screenName) {
                    screenStack.poll()
                }
            }
        }
    }

    private fun getCurrentFragment(): Fragment? {
        return activity.supportFragmentManager.findFragmentById(containerId)
    }

    private fun backTransition(fragmentTransaction: FragmentTransaction) {
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right,
                R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun forwardTransition(fragmentTransaction: FragmentTransaction?) {
        fragmentTransaction?.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun replaceTransition(fragmentTransaction: FragmentTransaction?) {
        fragmentTransaction?.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private val String.screenName
        get() = this.substringBefore(Router.DELIMITER)

    private val String.navId
        get() = this.substringAfter(Router.DELIMITER).toInt()

    constructor(parcel: Parcel) : this(
        TODO("activity"),
        parcel.readInt()
    ) {
        currentNavId = parcel.readInt()
    }

    private class BackStackScreenNameChange(val screenName: String) : Command

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(containerId)
        parcel.writeInt(currentNavId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Navigator> {
        override fun createFromParcel(parcel: Parcel): Navigator {
            return Navigator(parcel)
        }

        override fun newArray(size: Int): Array<Navigator?> {
            return arrayOfNulls(size)
        }
    }
}
