package io.bumbumapps.radio.internetradioplayer.presentation.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import io.bumbumapps.radio.internetradioplayer.R

// Former kotlin-android-extensions synthetic view accessors.
private val android.app.Activity.back: android.widget.ImageView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.back)
private val androidx.fragment.app.Fragment.back: android.widget.ImageView get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.back)
private val android.app.Dialog.back: android.widget.ImageView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.back)
private val android.app.Activity.settings: android.widget.FrameLayout get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.settings)
private val androidx.fragment.app.Fragment.settings: android.widget.FrameLayout get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.settings)
private val android.app.Dialog.settings: android.widget.FrameLayout get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.settings)

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        back.setOnClickListener {
            onBackPressed()
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }
}