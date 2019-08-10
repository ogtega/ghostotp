package de.tolunla.ghostotp.view.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import de.tolunla.ghostotp.R
import de.tolunla.ghostotp.setNightMode

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onStart() {
        super.onStart()

        val nightModeSwitch = preferenceScreen.findPreference<SwitchPreference>("night_mode")

        nightModeSwitch?.setOnPreferenceChangeListener { _, value ->
            setNightMode(value as Boolean)
            true
        }
    }
}