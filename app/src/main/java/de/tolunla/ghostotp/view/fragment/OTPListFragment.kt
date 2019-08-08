package de.tolunla.ghostotp.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.tolunla.ghostotp.databinding.OtpListFragmentBinding

class OTPListFragment : Fragment() {

    lateinit var binding: OtpListFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = OtpListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }
}