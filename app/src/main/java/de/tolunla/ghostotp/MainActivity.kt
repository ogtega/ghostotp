package de.tolunla.ghostotp

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.appbar.AppBarLayout
import de.tolunla.ghostotp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

  private lateinit var appBarConfig: AppBarConfiguration
  private lateinit var binding: ActivityMainBinding
  private lateinit var navController: NavController
  private lateinit var contentParameters: CoordinatorLayout.LayoutParams

  // Called once the host fragment switches to a new destination
  private val onDestinationChanged = OnDestinationChangedListener { _, destination, _ ->
    // Check if the destination is on the "top level" (this includes dialogs)
    if (appBarConfig.topLevelDestinations.contains(destination.id)) {
      showBottomBar()
    } else {
      showTopBar()
    }

    binding.content.requestLayout()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

    navController = findNavController(R.id.nav_host_fragment)

    contentParameters = binding.content.layoutParams as CoordinatorLayout.LayoutParams

    // Used to know when we are at a "top level" destination
    appBarConfig = AppBarConfiguration(
      setOf(
        R.id.account_list_dest,
        R.id.new_account_sheet_dest,
        R.id.option_menu_sheet_dest
      )
    )

    setupNavigation()
  }

  private fun setupNavigation() {
    setSupportActionBar(binding.toolbar)
    setupActionBarWithNavController(navController, appBarConfig)
    binding.bottomAppbar.setOnMenuItemClickListener(this::onOptionsItemSelected)

    navController.addOnDestinationChangedListener(onDestinationChanged)

    binding.fab.setOnClickListener {
      navController.navigate(R.id.action_account_list_dest_to_new_account_sheet_dest)
    }
  }

  private fun showBottomBar() {
    // Disable ScrollingViewBehavior
    contentParameters.behavior = null
    // Hide the toolbar and make the bottom appbar visible
    binding.topAppbar.visibility = View.GONE

    binding.bottomAppbar.visibility = View.VISIBLE
    binding.fab.visibility = View.VISIBLE
  }

  private fun showTopBar() {
    // Enable ScrollingViewBehavior
    contentParameters.behavior = AppBarLayout.ScrollingViewBehavior()
    // Show the toolbar and make the bottom appbar hidden
    binding.topAppbar.visibility = View.VISIBLE

    binding.bottomAppbar.visibility = View.GONE
    binding.fab.visibility = View.GONE
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return item.onNavDestinationSelected(navController) ||
        super.onOptionsItemSelected(item)
  }

  override fun onSupportNavigateUp(): Boolean {
    return navController.navigateUp(appBarConfig)
  }
}
