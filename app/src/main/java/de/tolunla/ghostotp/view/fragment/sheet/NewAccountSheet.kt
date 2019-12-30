package de.tolunla.ghostotp.view.fragment.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.tolunla.ghostotp.R
import de.tolunla.ghostotp.databinding.SheetNewAccountBinding

class NewAccountSheet : BottomSheetDialogFragment() {

  private lateinit var binding: SheetNewAccountBinding

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    setHasOptionsMenu(true)
    binding = SheetNewAccountBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.navView.setNavigationItemSelectedListener(this::onOptionsItemSelected)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val platforms = setOf(R.id.new_steam_dest)

    if (platforms.contains(item.itemId)) {
      val bundle = bundleOf("dest" to item.itemId)
      activity?.findViewById<View>(R.id.nav_host_fragment)?.findNavController()
        ?.navigate(R.id.new_platform_dest, bundle)
      return super.onOptionsItemSelected(item)
    }

    return item.onNavDestinationSelected(findNavController()) ||
        super.onOptionsItemSelected(item)
  }
}