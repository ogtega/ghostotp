package de.tolunla.ghostotp.view.fragment.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.tolunla.ghostotp.databinding.SheetAccountActionBinding

class AccountActionSheet : BottomSheetDialogFragment() {

  lateinit var binding: SheetAccountActionBinding

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    setHasOptionsMenu(true)
    binding = SheetAccountActionBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.navView.setNavigationItemSelectedListener(this::onOptionsItemSelected)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return item.onNavDestinationSelected(findNavController()) ||
        super.onOptionsItemSelected(item)
  }
}