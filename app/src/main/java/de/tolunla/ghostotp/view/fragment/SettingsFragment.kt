package de.tolunla.ghostotp.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import de.tolunla.ghostotp.R

class SettingsFragment : PreferenceFragmentCompat() {

    private val nightModeListener = Preference.OnPreferenceChangeListener { _, newVal ->
        AppCompatDelegate.setDefaultNightMode(
                if (newVal as Boolean)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
        true
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val nightMode = preferenceManager.findPreference<SwitchPreference>("night_mode")

        nightMode?.onPreferenceChangeListener = nightModeListener

        return super.onCreateView(inflater, container, savedInstanceState)
    }
}