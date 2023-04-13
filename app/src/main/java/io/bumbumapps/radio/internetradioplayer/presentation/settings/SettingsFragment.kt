package io.bumbumapps.radio.internetradioplayer.presentation.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ShareCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.bumbumapps.radio.internetradioplayer.R
import io.bumbumapps.radio.internetradioplayer.data.preference.Preferences
import io.bumbumapps.radio.internetradioplayer.data.utils.BACKUP_TYPE
import io.bumbumapps.radio.internetradioplayer.data.utils.BackupRestoreHelper
import io.bumbumapps.radio.internetradioplayer.di.Scopes
import io.bumbumapps.radio.internetradioplayer.domain.interactor.FavoriteListInteractor
import io.bumbumapps.radio.internetradioplayer.extensions.startActivitySafe
import io.bumbumapps.radio.internetradioplayer.extensions.subscribeX
import io.bumbumapps.radio.internetradioplayer.presentation.base.BackPressListener
import io.bumbumapps.radio.internetradioplayer.presentation.navigation.Router
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by Vladimir Mikhalev 30.09.2018.
 */

private const val PICK_BACKUP_REQUEST_CODE = 999

class SettingsFragment : PreferenceFragmentCompat(), BackPressListener {

    private val backupRestoreHelper: BackupRestoreHelper by lazy {
        Scopes.app.getInstance(BackupRestoreHelper::class.java)
    }
    private val preferences: Preferences by lazy {
        Scopes.app.getInstance(Preferences::class.java)
    }
    private val router: Router by lazy {
        Scopes.rootActivity.getInstance(Router::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = Preferences.PREFERENCES_NAME
        addPreferencesFromResource(R.xml.settings_screen)

        setupBackupPref()
        setupAudioFocusPref()
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is SeekBarDialogPreference && !requireFragmentManager().isStateSaved) {
            val fragment = SeekBarDialogFragment.newInstance(preference.key)
            fragment.setTargetFragment(this, 0)
            fragment.showNow(requireFragmentManager(), "SeekBarDialogFragment")
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_BACKUP_REQUEST_CODE && resultCode == Activity.RESULT_OK && data?.data != null) {
            //todo to interactor
            backupRestoreHelper.restoreBackup(data.data!!)
                    .andThen(Scopes.app.getInstance(FavoriteListInteractor::class.java).initFavoriteList())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeX(onComplete = { router.exit() })
        }
    }

    override fun handleBackPressed(): Boolean {
        router.exit()
        return true
    }

    private fun setupBackupPref() {
        findPreference<Preference>("BACKUP_STATIONS")?.setOnPreferenceClickListener {
            createBackup()
            true
        }
        findPreference<Preference>("RESTORE_STATIONS")?.setOnPreferenceClickListener {
            restoreBackup()
            true
        }
    }

    private fun setupAudioFocusPref() {
        val audioFocusPreference: Preference? = findPreference(Preferences.KEY_AUDIO_FOCUS)
        audioFocusPreference?.setSummary(preferences.audioFocus.summary)
        audioFocusPreference?.setOnPreferenceClickListener {
            val nextState = preferences.audioFocus.nextState
            it.setSummary(nextState.summary)
            preferences.audioFocus = nextState
            true
       }
    }

    private fun createBackup() {
        val uri = backupRestoreHelper.createBackup()
        val intent = ShareCompat.IntentBuilder.from(activity!!)
                .setType(BACKUP_TYPE)
                .setSubject(getString(R.string.full_app_name))
                .setStream(uri)
                .setChooserTitle(getString(R.string.chooser_save))
                .createChooserIntent()
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        requireContext().startActivitySafe(intent)
    }

    private fun restoreBackup() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = BACKUP_TYPE
        if (requireContext().packageManager.resolveActivity(intent, 0) != null) {
            startActivityForResult(intent, PICK_BACKUP_REQUEST_CODE)
        }
    }
}
