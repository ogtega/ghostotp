package de.tolunla.ghostotp

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import de.tolunla.ghostotp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var appBarConfig: AppBarConfiguration
    lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController

    val onDestinationChanged = NavController.OnDestinationChangedListener { _, destination, _ ->
        if (destination.id == R.id.otplist) {
            binding.toolbar.visibility = View.GONE
            binding.appbar.visibility = View.VISIBLE
        } else {
            binding.toolbar.visibility = View.VISIBLE
            binding.appbar.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        navController = findNavController(R.id.nav_host_fragment)
        appBarConfig = AppBarConfiguration(navController.graph)

        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfig)
        binding.appbar.setOnMenuItemClickListener(this::onOptionsItemSelected)

        navController.addOnDestinationChangedListener(onDestinationChanged)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) ||
                super.onOptionsItemSelected(item)
    }
}
