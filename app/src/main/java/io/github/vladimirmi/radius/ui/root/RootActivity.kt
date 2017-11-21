package io.github.vladimirmi.radius.ui.root

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.Screens
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.di.module.RootActivityModule
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.presentation.root.RootPresenter
import io.github.vladimirmi.radius.presentation.root.RootView
import io.github.vladimirmi.radius.ui.mediaList.MediaListFragment
import io.github.vladimirmi.radius.ui.station.StationFragment
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.android.SupportAppNavigator
import ru.terrakok.cicerone.commands.Command
import toothpick.Toothpick
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

class RootActivity : MvpAppCompatActivity(), RootView {

    @Inject lateinit var navigatorHolder: NavigatorHolder
    @InjectPresenter lateinit var presenter: RootPresenter

    @ProvidePresenter
    fun providePresenter(): RootPresenter =
            Scopes.rootActivity.getInstance(RootPresenter::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        Scopes.rootActivity.apply {
            installModules(RootActivityModule())
            Toothpick.inject(this@RootActivity, this)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun onDestroy() {
        if (isFinishing) Toothpick.closeScope(Scopes.ROOT_ACTIVITY)
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private val navigator = object : SupportAppNavigator(this, R.id.fragment_container) {

        private var currentScreen: String? = null

        override fun createActivityIntent(screenKey: String?, data: Any?) = null

        override fun createFragment(screenKey: String?, data: Any?): Fragment? {
            if (screenKey == currentScreen) return null
            currentScreen = screenKey
            return when (screenKey) {
                Screens.MEDIA_LIST_SCREEN -> MediaListFragment()
                Screens.STATION_SCREEN -> StationFragment.newInstance(data as Station)
                else -> null
            }
        }

        override fun unknownScreen(command: Command?) {
            //do nothing
        }
    }

    override fun showToast(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }


}