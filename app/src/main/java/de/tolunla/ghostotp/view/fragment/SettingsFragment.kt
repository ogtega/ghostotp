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
import de.tolunla.ghostotp.db.AppCipher

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
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val biometricPrompt =
                    BiometricPrompt(this, ContextCompat.getMainExecutor(requireContext()),
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationError(
                                errorCode: Int,
                                errString: CharSequence
                            ) {
                                super.onAuthenticationError(errorCode, errString)
                                prefs.edit().remove("CIPHER_IV").apply()
                                biometricsSwitchPreference?.isChecked = !(newVal as Boolean)
                            }

                            override fun onAuthenticationSucceeded(
                                result: BiometricPrompt.AuthenticationResult
                            ) {
                                super.onAuthenticationSucceeded(result)
                                if (newVal as Boolean) {
                                    prefs.edit().putString(
                                        "CIPHER_IV",
                                        String(AppCipher.getInstance().iv, Charsets.ISO_8859_1)
                                    ).apply()
                                } else {
                                    prefs.edit().remove("CIPHER_IV").apply()
                                }
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
            }
            else -> {
                prefs.edit().remove("CIPHER_IV").apply()
                biometricsSwitchPreference?.isChecked = !(newVal as Boolean)
            }
        }

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
