package de.tolunla.ghostotp.view.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import de.tolunla.ghostotp.BiometricActivity.Companion.BioCheckObserver
import de.tolunla.ghostotp.R
import de.tolunla.ghostotp.db.AppCipher
import javax.crypto.Cipher

/**
 * Fragment for application settings screen
 */
class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var prefs: SharedPreferences

    lateinit var bioCheckObserver: BioCheckObserver
    private var biometricsSwitchPreference: SwitchPreference? = null

    private val nightModeListener = Preference.OnPreferenceChangeListener { _, newVal ->
        AppCompatDelegate.setDefaultNightMode(
            if (newVal as Boolean)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )

        true
    }

    private val biometricsListener = Preference.OnPreferenceChangeListener { _, newVal ->
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if (newVal as Boolean) {
            bioCheckObserver.bioCheck(requireActivity(), Cipher.ENCRYPT_MODE, true) { success ->
                if (success)
                    prefs.edit().putString(
                        "CIPHER_IV",
                        String(AppCipher.getInstance().iv, Charsets.ISO_8859_1)
                    ).apply()
                else
                    biometricsSwitchPreference?.isChecked = !newVal
            }
        } else {
            bioCheckObserver.bioCheck(requireActivity(), require = true) { success ->
                if (success)
                    prefs.edit().remove("CIPHER_IV").apply()
                else
                    biometricsSwitchPreference?.isChecked = !newVal
            }
        }

        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        bioCheckObserver = BioCheckObserver(requireActivity().activityResultRegistry)
        lifecycle.addObserver(bioCheckObserver)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val nightMode =
            preferenceManager.findPreference<SwitchPreference>(getString(R.string.preference_night_mode_key))
        biometricsSwitchPreference =
            preferenceManager.findPreference(getString(R.string.preference_biometrics_key))

        nightMode?.onPreferenceChangeListener = nightModeListener
        biometricsSwitchPreference?.onPreferenceChangeListener = biometricsListener

        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
