package de.tolunla.ghostotp

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import de.tolunla.ghostotp.databinding.ActivityMainBinding

/**
 * The main activity that contains all fragments
 */
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfig: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // Called once the host fragment switches to a new destination
    private val onDestinationChanged = OnDestinationChangedListener { _, destination, _ ->
        // Check if the destination is on the "top level" (this includes dialogs)
        if (appBarConfig.topLevelDestinations.contains(destination.id)) {
            binding.fab.visibility = View.VISIBLE
        } else {
            binding.fab.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        if (prefs.getBoolean(getString(R.string.preference_biometrics_key), false)) {
            if (BiometricManager.from(this)
                    .canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
            ) {
                val biometricPrompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            super.onAuthenticationError(errorCode, errString)
                            finish()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            finish()
                        }

                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            binding.content.visibility = View.VISIBLE
                        }
                    })

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.biometric_prompt_title))
                    .setSubtitle(getString(R.string.biometric_prompt_subtitle))
                    .setNegativeButtonText(getString(R.string.action_cancel))
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        if (prefs.getBoolean(getString(R.string.preference_biometrics_key), false))
            binding.content.visibility = View.INVISIBLE

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController

        // Used to know when we are at a "top level" destination
        appBarConfig = AppBarConfiguration.Builder(
            R.id.account_list_dest,
            R.id.new_account_sheet_dest
        ).build()

        setupNavigation()
    }

    private fun setupNavigation() {
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfig)

        navController.addOnDestinationChangedListener(onDestinationChanged)

        binding.fab.setOnClickListener {
            navController.navigate(R.id.action_account_list_dest_to_new_account_sheet_dest)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) ||
            super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfig)
    }
}
