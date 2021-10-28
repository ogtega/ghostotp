package de.tolunla.ghostotp.view.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import de.tolunla.ghostotp.R

/**
 * Fragment for application settings screen
 */
class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var prefs: SharedPreferences
    private lateinit var biometricManager: BiometricManager

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

        val biometricPrompt =
            BiometricPrompt(this, ContextCompat.getMainExecutor(requireContext()),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        biometricsSwitchPreference?.isChecked = !(newVal as Boolean)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        biometricsSwitchPreference?.isChecked = !(newVal as Boolean)
                    }
                })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()

        biometricPrompt.authenticate(promptInfo)
        Log.d("MY_APP_TAG", "App can authenticate using biometrics.")

        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        biometricManager = BiometricManager.from(requireContext())
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

        biometricsSwitchPreference?.isEnabled =
            biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
