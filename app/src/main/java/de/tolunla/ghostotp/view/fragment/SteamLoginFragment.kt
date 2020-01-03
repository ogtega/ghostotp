package de.tolunla.ghostotp.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.tolunla.ghostotp.databinding.FragmentSteamLoginBinding

class SteamLoginFragment : Fragment() {

  private lateinit var binding: FragmentSteamLoginBinding

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?): View? {
    binding = FragmentSteamLoginBinding.inflate(inflater, container, false)
    return binding.root
  }
}