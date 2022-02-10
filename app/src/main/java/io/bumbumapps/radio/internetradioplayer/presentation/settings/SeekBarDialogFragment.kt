package io.bumbumapps.radio.internetradioplayer.presentation.settings

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.preference.PreferenceDialogFragmentCompat
import kotlinx.android.synthetic.main.pref_seekbar.view.*


/**
 * Created by Vladimir Mikhalev 01.10.2018.
 */

class SeekBarDialogFragment : PreferenceDialogFragmentCompat() {

    private lateinit var seekBar: SeekBar
    private lateinit var seekBarValue: TextView

    companion object {
        fun newInstance(key: String) = SeekBarDialogFragment().apply {
            arguments = Bundle(1).apply {
                putString(ARG_KEY, key)
            }
        }
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        seekBar = view.seekBar
        seekBarValue = view.valueTv

        seekBar.progress = (preference as SeekBarDialogPreference).progress
        seekBarValue.text = preference.summary

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBarValue.text = (preference as SeekBarDialogPreference).createSummary(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult && preference.callChangeListener(seekBar.progress)) {
            (preference as SeekBarDialogPreference).progress = seekBar.progress
        }
    }
}
