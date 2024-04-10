package io.bumbumapps.radio.internetradioplayer.presentation.root

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.bumbumapps.radio.internetradioplayer.R
import io.bumbumapps.radio.internetradioplayer.data.service.PlayerService
import io.bumbumapps.radio.internetradioplayer.data.utils.SCHEME_FILE
import io.bumbumapps.radio.internetradioplayer.data.utils.ShortcutHelper
import io.bumbumapps.radio.internetradioplayer.di.Scopes
import io.bumbumapps.radio.internetradioplayer.di.module.RootActivityModule
import io.bumbumapps.radio.internetradioplayer.extensions.visible
import io.bumbumapps.radio.internetradioplayer.presentation.base.BaseActivity
import io.bumbumapps.radio.internetradioplayer.presentation.navigation.drawer.DrawerFragment
import io.bumbumapps.radio.internetradioplayer.presentation.player.isExpanded
import io.bumbumapps.radio.internetradioplayer.presentation.player.isHidden
import io.bumbumapps.radio.internetradioplayer.utils.Globals.TIMER_FINISHED
import io.bumbumapps.radio.internetradioplayer.utils.Timers
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.view_toolbar.*
import toothpick.Toothpick


/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */
private const val WRITE_PERMISSION_REQUEST = 100

class RootActivity : BaseActivity<RootPresenter, RootView>(), RootView {

    override val layout = R.layout.activity_root
    private lateinit var playerBehavior: BottomSheetBehavior<View>

    override fun providePresenter(): RootPresenter = Scopes.rootActivity.getInstance(RootPresenter::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        Scopes.rootActivity.apply {
            installModules(RootActivityModule())
        }
        super.onCreate(savedInstanceState)
//        MobileAds.initialize(this)
//
//
//        val adRequest = AdRequest.Builder().build()
//        adView.loadAd(adRequest)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        val drawerFragment = supportFragmentManager.findFragmentById(R.id.drawerFr) as DrawerFragment
        drawerFragment.init(drawerLayout, toolbar)
        drawerFragment.setUpPopubmenu()

        drawerFragment.setUpPopubmenu()
        if (savedInstanceState != null || (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            intent = null // stop redeliver old intent
        }

    }

    override fun setupView() {
        setupToolbar()
        playerBehavior = BottomSheetBehavior.from(findViewById(R.id.playerFragment))
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        val drawerFragment = supportFragmentManager.findFragmentById(R.id.drawerFr) as DrawerFragment
        drawerFragment.init(drawerLayout, toolbar)

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        if (isPresenterInitialized) checkIntent()
    }

    override fun onDestroy() {
        if (isFinishing) Toothpick.closeScope(Scopes.ROOT_ACTIVITY)
        super.onDestroy()
    }

    //region =============== RootView ==============

    override fun checkIntent() {
        if (intent == null) return
        val startPlay = intent.getBooleanExtra(ShortcutHelper.EXTRA_PLAY, false)
        val stationName = intent.getStringExtra(ShortcutHelper.EXTRA_STATION_NAME)
        if (intent.hasExtra(PlayerService.EXTRA_STATION_ID)) {
            intent.getStringExtra(PlayerService.EXTRA_STATION_ID)
                ?.let { presenter.showStation(it, startPlay) }
            intent = null
        } else {
            intent.data?.let {
                createStation(it, stationName, addToFavorite = false, startPlay = startPlay)
            } ?: run { intent = null }
        }
    }

    override fun showLoadingIndicator(visible: Boolean) {
        loadingPb.visible(visible)
    }

    override fun hidePlayer() {
        playerBehavior.isHideable = true
        playerBehavior.isHidden = true
    }

    override fun collapsePlayer() {
        activityView.postDelayed({
            playerBehavior.isHideable = false
            playerBehavior.isHidden = false

        }, 300)
    }

    override fun expandPlayer() {
        playerBehavior.isHideable = false
        playerBehavior.isExpanded = true
    }

    override fun createStation(uri: Uri, name: String?, addToFavorite: Boolean, startPlay: Boolean) {
        if (uri.scheme != SCHEME_FILE || uri.scheme == SCHEME_FILE && checkWritePermission()) {
            intent = null
            presenter.createStation(uri, name, addToFavorite, startPlay)
        }
    }

    override fun setOffset(offset: Float) {
        val lp = rootContainer.layoutParams as ViewGroup.MarginLayoutParams
        lp.bottomMargin = (resources.getDimension(R.dimen.player_collapsed_height) * offset).toInt()
        rootContainer.layoutParams = lp
    }

    //endregion

    private fun checkWritePermission(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
              Manifest.permission.READ_MEDIA_IMAGES
        }else Manifest.permission.WRITE_EXTERNAL_STORAGE

        return if (ContextCompat.checkSelfPermission(this, permissions)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permissions),
                    WRITE_PERMISSION_REQUEST
            )
            false
        } else true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            WRITE_PERMISSION_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkIntent()
                }
            }
            else -> {
            }
        }
    }
}
