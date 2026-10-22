package io.bumbumapps.radio.internetradioplayer.presentation.player

import android.view.View
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.exoplayer2.util.Util

import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.bumbumapps.radio.internetradioplayer.R
import io.bumbumapps.radio.internetradioplayer.data.db.entity.Station
import io.bumbumapps.radio.internetradioplayer.di.Scopes
import io.bumbumapps.radio.internetradioplayer.domain.model.Record
import io.bumbumapps.radio.internetradioplayer.extensions.*
import io.bumbumapps.radio.internetradioplayer.presentation.base.BaseFragment
import io.bumbumapps.radio.internetradioplayer.presentation.root.RootView
import io.bumbumapps.radio.internetradioplayer.utils.AdsLoader
import io.bumbumapps.radio.internetradioplayer.utils.SimpleOnSeekBarChangeListener

import toothpick.Toothpick
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

// Former kotlin-android-extensions synthetic view accessors.
private val android.app.Activity.controlsView: androidx.constraintlayout.widget.ConstraintLayout get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.controlsView)
private val androidx.fragment.app.Fragment.controlsView: androidx.constraintlayout.widget.ConstraintLayout get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.controlsView)
private val android.app.Dialog.controlsView: androidx.constraintlayout.widget.ConstraintLayout get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.controlsView)
private val android.app.Activity.durationTv: android.widget.TextView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.durationTv)
private val androidx.fragment.app.Fragment.durationTv: android.widget.TextView get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.durationTv)
private val android.app.Dialog.durationTv: android.widget.TextView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.durationTv)
private val android.app.Activity.favoriteIv: android.widget.ImageButton get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.favoriteIv)
private val androidx.fragment.app.Fragment.favoriteIv: android.widget.ImageButton get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.favoriteIv)
private val android.app.Dialog.favoriteIv: android.widget.ImageButton get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.favoriteIv)
private val android.app.Activity.mediaInfoVp: androidx.viewpager.widget.ViewPager get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.mediaInfoVp)
private val androidx.fragment.app.Fragment.mediaInfoVp: androidx.viewpager.widget.ViewPager get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.mediaInfoVp)
private val android.app.Dialog.mediaInfoVp: androidx.viewpager.widget.ViewPager get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.mediaInfoVp)
private val android.app.Activity.metadataTv: android.widget.TextView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.metadataTv)
private val androidx.fragment.app.Fragment.metadataTv: android.widget.TextView get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.metadataTv)
private val android.app.Dialog.metadataTv: android.widget.TextView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.metadataTv)
private val android.app.Activity.nextBt: android.widget.ImageButton get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.nextBt)
private val androidx.fragment.app.Fragment.nextBt: android.widget.ImageButton get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.nextBt)
private val android.app.Dialog.nextBt: android.widget.ImageButton get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.nextBt)
private val android.app.Activity.pagerIndicator: com.google.android.material.tabs.TabLayout get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.pagerIndicator)
private val androidx.fragment.app.Fragment.pagerIndicator: com.google.android.material.tabs.TabLayout get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.pagerIndicator)
private val android.app.Dialog.pagerIndicator: com.google.android.material.tabs.TabLayout get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.pagerIndicator)
private val android.app.Activity.playPauseBt: android.widget.ImageButton get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.playPauseBt)
private val androidx.fragment.app.Fragment.playPauseBt: android.widget.ImageButton get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.playPauseBt)
private val android.app.Dialog.playPauseBt: android.widget.ImageButton get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.playPauseBt)
private val android.app.Activity.playPauseBtEnd: android.widget.Space get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.playPauseBtEnd)
private val androidx.fragment.app.Fragment.playPauseBtEnd: android.widget.Space get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.playPauseBtEnd)
private val android.app.Dialog.playPauseBtEnd: android.widget.Space get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.playPauseBtEnd)
private val android.app.Activity.playPauseBtStart: android.widget.Space get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.playPauseBtStart)
private val androidx.fragment.app.Fragment.playPauseBtStart: android.widget.Space get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.playPauseBtStart)
private val android.app.Dialog.playPauseBtStart: android.widget.Space get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.playPauseBtStart)
private val android.app.Activity.pointerIv: android.widget.ImageButton get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.pointerIv)
private val androidx.fragment.app.Fragment.pointerIv: android.widget.ImageButton get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.pointerIv)
private val android.app.Dialog.pointerIv: android.widget.ImageButton get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.pointerIv)
private val android.app.Activity.positionTv: android.widget.TextView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.positionTv)
private val androidx.fragment.app.Fragment.positionTv: android.widget.TextView get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.positionTv)
private val android.app.Dialog.positionTv: android.widget.TextView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.positionTv)
private val android.app.Activity.previousBt: android.widget.ImageButton get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.previousBt)
private val androidx.fragment.app.Fragment.previousBt: android.widget.ImageButton get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.previousBt)
private val android.app.Dialog.previousBt: android.widget.ImageButton get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.previousBt)
private val android.app.Activity.progressSb: android.widget.SeekBar get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.progressSb)
private val androidx.fragment.app.Fragment.progressSb: android.widget.SeekBar get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.progressSb)
private val android.app.Dialog.progressSb: android.widget.SeekBar get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.progressSb)
private val android.app.Activity.statusTv: android.widget.TextView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.statusTv)
private val androidx.fragment.app.Fragment.statusTv: android.widget.TextView get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.statusTv)
private val android.app.Dialog.statusTv: android.widget.TextView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.statusTv)
private val android.app.Activity.statusTvStart: android.widget.TextView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.statusTvStart)
private val androidx.fragment.app.Fragment.statusTvStart: android.widget.TextView get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.statusTvStart)
private val android.app.Dialog.statusTvStart: android.widget.TextView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.statusTvStart)
private val android.app.Activity.stopBt: android.widget.ImageButton get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.stopBt)
private val androidx.fragment.app.Fragment.stopBt: android.widget.ImageButton get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.stopBt)
private val android.app.Dialog.stopBt: android.widget.ImageButton get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.stopBt)
private val android.app.Activity.titleTv: android.widget.TextView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.titleTv)
private val androidx.fragment.app.Fragment.titleTv: android.widget.TextView get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.titleTv)
private val android.app.Dialog.titleTv: android.widget.TextView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.titleTv)

/**
 * Created by Vladimir Mikhalev 20.02.2019.
 */
class PlayerFragment : BaseFragment<PlayerPresenter, PlayerView>(), PlayerView {

    override val layout = R.layout.fragment_player
    private var playerBehavior: BottomSheetBehavior<View>? = null
    private var isSeekEnabled = false
    private var isCoverArtEnabled = false
    private val infoAdapter = InfoAdapter(lifecycle)

    override fun providePresenter(): PlayerPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(PlayerPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        titleTv.isSelected = true
        metadataTv.isSelected = true

        setupButtons()
        setupSeekBar()
        setupBehavior(view)
        setupInfo()
        AdsLoader.displayInterstitial(requireContext())
    }

    private fun setupButtons() {
        favoriteIv.setOnClickListener { presenter.switchFavorite() }
        previousBt.setOnClickListener { presenter.skipToPrevious() }
        nextBt.setOnClickListener { presenter.skipToNext() }
        stopBt.setOnClickListener { presenter.stop() }
        pointerIv.setOnClickListener { switchState() }
        playPauseBt.setOnClickListener {
                 presenter.playPause(requireContext())
                if (isSeekEnabled) presenter.seekTo(progressSb.progress)

        }

    }

    private fun setupSeekBar() {
        progressSb.setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) presenter.seekTo(progress)
                positionTv.text = Formats.duration(progress.toLong())
            }
        })
    }

    private fun setupBehavior(view: View) {
        requireView().post {
            playerBehavior = BottomSheetBehavior.from(view)
            playerBehavior?.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    setOffset(slideOffset)
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                }
            })
            setupOffset()
        }
    }

    private fun setupInfo() {
        mediaInfoVp.adapter = infoAdapter
        mediaInfoVp.currentItem = 0
        pagerIndicator.setupWithViewPager(mediaInfoVp)
    }

    override fun handleBackPressed(): Boolean {
        return if (playerBehavior.isExpanded) {
            switchState()
            true
        } else false
    }

    //region =============== PlayerView ==============

    override fun setStation(station: Station) {
        titleTv.text = station.name
        favoriteIv.visible(true)
        val tint = if (station.isFavorite) R.color.orange_500 else R.color.primary_variant
        favoriteIv.background.setTintExt(requireContext().color(tint))
        infoAdapter.coverArtEnabled = isCoverArtEnabled
    }

    override fun setRecord(record: Record) {
        titleTv.text = record.name
        setDuration(record.duration)
        favoriteIv.visible(false)
        infoAdapter.coverArtEnabled = false
        mediaInfoVp.currentItem = 0
    }

    override fun setStatus(resId: Int) {
        statusTv.setText(resId)
        if (statusTv.text==getString(R.string.status_playing) || statusTv.text==getString(R.string.metadata_buffering) ){
            playPauseBt.setImageDrawable(null)
           playPauseBt.setBackgroundResource(R.drawable.ic_pause)
        }
        else{
            playPauseBt.setImageDrawable(null)
            playPauseBt.setBackgroundResource(R.drawable.ic_play)
        }
    }

    override fun showPlaying(isPlaying: Boolean) {

    }

    override fun showNext() {
        nextBt.bounceXAnimation(200f).start()
    }

    override fun showPrevious() {
        previousBt.bounceXAnimation(-200f).start()
    }

    override fun setMetadata(metadata: String) {
        metadataTv.text = metadata
    }

    override fun setPosition(position: Long) {
        progressSb.progress = position.toInt()
    }

    override fun incrementPositionBy(duration: Long) {
        progressSb.incrementProgressBy(duration.toInt())
    }

    override fun setDuration(duration: Long) {
        durationTv.text = Formats.duration(duration)
        progressSb.max = duration.toInt()
    }

    override fun enableSeek(isEnabled: Boolean) {
        isSeekEnabled = isEnabled
        progressSb.visible(isEnabled)
        positionTv.visible(isEnabled)
        durationTv.visible(isEnabled)
    }

    override fun enableSkip(isEnabled: Boolean) {
        nextBt.isEnabled = isEnabled
        previousBt.isEnabled = isEnabled
    }

    override fun enableCoverArt(isEnabled: Boolean) {
        isCoverArtEnabled = isEnabled
        infoAdapter.coverArtEnabled = isEnabled
    }

    //endregion

    private fun setupOffset() {
        when {
            playerBehavior.isCollapsed -> setOffset(0f)
            playerBehavior.isExpanded -> setOffset(1f)
            playerBehavior.isHidden -> setOffset(-1f)
        }
    }

    private fun setOffset(offset: Float) {
        val parentState = Util.constrainValue(offset + 1, 0f, 1f)
        val state = Util.constrainValue(offset, 0f, 1f)
        (activity as? RootView)?.setOffset(parentState)

        val playerView = view as? ConstraintLayout ?: return
        val set = ConstraintSet()
        set.clone(playerView)
        set.setVerticalBias(R.id.controlsView, state)
        set.applyTo(playerView)

        val visible = state > 0.9
        nextBt.visible(visible, false)
        previousBt.visible(visible, false)
        stopBt.visible(visible, false)
        if (isSeekEnabled) {
            progressSb.visible(visible, false)
            positionTv.visible(visible, false)
            durationTv.visible(visible, false)
        }

        playPauseBt.x = playPauseBtStart.x + (playPauseBtEnd.x - playPauseBtStart.x) * state
        playPauseBt.y = playPauseBtStart.y + (playPauseBtEnd.y - playPauseBtStart.y) * state
        statusTv.y = statusTvStart.y + (playerView.height - statusTv.height - statusTvStart.y) * state

        pointerIv.rotationX = 180 * state
    }

    private fun switchState() {
        if (playerBehavior.isCollapsed) {
            playerBehavior.isExpanded = true
        } else if (playerBehavior.isExpanded) {
            playerBehavior.isCollapsed = true
        }
    }
}

var BottomSheetBehavior<View>?.isExpanded
    get() = this?.state == BottomSheetBehavior.STATE_EXPANDED
    set(value) {
        this ?: return
        this!!.state = if (value) BottomSheetBehavior.STATE_EXPANDED
        else BottomSheetBehavior.STATE_COLLAPSED
    }
var BottomSheetBehavior<View>?.isCollapsed
    get() = this?.state == BottomSheetBehavior.STATE_COLLAPSED
    set(value) {
        this ?: return
        if (value) this!!.state = BottomSheetBehavior.STATE_COLLAPSED
    }
var BottomSheetBehavior<View>?.isHidden
    get() = this?.state == BottomSheetBehavior.STATE_HIDDEN
    set(value) {
        this ?: return
        this!!.state = if (value) BottomSheetBehavior.STATE_HIDDEN
        else BottomSheetBehavior.STATE_COLLAPSED
    }