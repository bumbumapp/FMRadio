package io.bumbumapps.radio.internetradioplayer.presentation.player.mediainfo

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import io.bumbumapps.radio.internetradioplayer.R
import io.bumbumapps.radio.internetradioplayer.data.db.entity.Group
import io.bumbumapps.radio.internetradioplayer.data.db.entity.Station
import io.bumbumapps.radio.internetradioplayer.di.Scopes
import io.bumbumapps.radio.internetradioplayer.domain.model.Media
import io.bumbumapps.radio.internetradioplayer.domain.model.Record
import io.bumbumapps.radio.internetradioplayer.extensions.color
import io.bumbumapps.radio.internetradioplayer.extensions.setTextOrHide
import io.bumbumapps.radio.internetradioplayer.extensions.visible
import io.bumbumapps.radio.internetradioplayer.presentation.base.BaseCustomView
import kotlinx.android.synthetic.main.view_media_info.view.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 27.03.2019.
 */

class MediaInfoViewImpl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseCustomView<MediaInfoPresenter, MediaInfoView>(context, attrs, defStyleAttr), MediaInfoView {


    override val layout = R.layout.view_media_info

    override fun providePresenter(): MediaInfoPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(MediaInfoPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView() {
        recordBt.setOnClickListener { presenter.startStopRecording() }
        addShortcutBt.setOnClickListener { openAddShortcutDialog() }
        equalizerBt.setOnClickListener { presenter.openEqualizer() }
        websiteTv.setOnClickListener { openLinkDialog(websiteTv.text.toString()) }
    }

    override fun setMedia(media: Media) {
        groupTv.setTextOrHide(if (media.group == Group.DEFAULT_NAME) null else media.group)
        genreTv.setTextOrHide(media.genre)
        specsTv.setTextOrHide(media.specs)
        locationTv.setTextOrHide(media.languageString)
        websiteTv.setTextOrHide(media.url)
        websiteTv.linkStyle(true)
        dateTv.setTextOrHide(if (media is Record) media.createdAtString else null)

        recordBt.visible(media is Station)
        addShortcutBt.visible(media is Station)
        equalizerBt.visible(media is Station)
    }

    override fun setRecording(isRecording: Boolean) {
        val tint = context.color(if (isRecording) R.color.secondary else R.color.white)
        recordBt.setColorFilter(tint)
    }

    private fun openAddShortcutDialog() {
        fragmentManager {
            AddShortcutDialog().show(it, "add_shortcut_dialog")
        }
    }

    private fun openLinkDialog(url: String) {
        fragmentManager {
            LinkDialog.newInstance(url).show(it, "link_dialog")
        }
    }

    private fun TextView.linkStyle(enable: Boolean) {
        val string = text.toString()
        val color = ContextCompat.getColor(context, R.color.green)
        text = if (enable) {
            val spannable = SpannableString(string)
            spannable.setSpan(URLSpan(string), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(ForegroundColorSpan(color), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable
        } else {
            string
        }
    }

    private fun fragmentManager(block: (FragmentManager) -> Unit) {
        (context as? FragmentActivity)?.let { block(it.supportFragmentManager) }
    }
}