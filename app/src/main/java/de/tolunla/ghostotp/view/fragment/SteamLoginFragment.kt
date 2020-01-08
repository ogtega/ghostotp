package de.tolunla.ghostotp.view.fragment

import android.os.Bundle
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

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.buttonAdd.setOnClickListener {
      login()
    }
  }

  private fun login() = GlobalScope.launch(Dispatchers.Main) {
    val res = SteamAuthLogin(
      binding.inputUsername.text.toString(),
      binding.inputPassword.text.toString()
    ).doLogin(
      emailAuth = binding.inputCode.text.toString(),
      twoFactorCode = binding.inputCode.text.toString()
    )



    Log.d("DoLogin", res)
  }
}
