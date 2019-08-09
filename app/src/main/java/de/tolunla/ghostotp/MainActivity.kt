package de.tolunla.ghostotp

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import de.tolunla.ghostotp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfig: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // Called once the host fragment switches to a new destination
    private val onDestinationChanged = OnDestinationChangedListener { _, destination, _ ->
        if (appBarConfig.topLevelDestinations.contains(destination.id)) {
            binding.toolbar.visibility = View.GONE
            binding.appbar.visibility = View.VISIBLE
            binding.fab.visibility = View.VISIBLE
        } else {
            binding.toolbar.visibility = View.VISIBLE
            binding.appbar.visibility = View.GONE
            binding.fab.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        navController = findNavController(R.id.nav_host_fragment)

        // Used to know when we are at a top level destination
        appBarConfig = AppBarConfiguration(
            setOf(
                R.id.otp_list_dest,
                R.id.create_otp_sheet_dest,
                R.id.menu_sheet_dest
            )
        )

        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfig)
        binding.appbar.setOnMenuItemClickListener(this::onOptionsItemSelected)

        navController.addOnDestinationChangedListener(onDestinationChanged)

        binding.fab.setOnClickListener {
            navController.navigate(R.id.action_otp_list_dest_to_create_otp_sheet_dest)
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
