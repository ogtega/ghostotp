package de.tolunla.ghostotp.view.fragment

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.tolunla.ghostotp.databinding.FragmentSteamLoginBinding
import de.tolunla.steamauth.SteamAuthLogin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SteamLoginFragment : Fragment() {

  private lateinit var binding: FragmentSteamLoginBinding

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?): View? {
    binding = FragmentSteamLoginBinding.inflate(inflater, container, false)
    return binding.root
  }

  private fun login(username: String, password: String) = GlobalScope.launch(Dispatchers.Main) {
    val res = SteamAuthLogin(username, password).doLogin()
    Log.d("DoLogin", res)
  }
}
