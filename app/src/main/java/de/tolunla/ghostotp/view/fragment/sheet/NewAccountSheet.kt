package de.tolunla.ghostotp.view.fragment.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.tolunla.ghostotp.databinding.SheetNewAccountBinding

class NewAccountSheet : BottomSheetDialogFragment() {

    private lateinit var binding: SheetNewAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SheetNewAccountBinding.inflate(inflater, container, false)
        return binding.root
    }
}