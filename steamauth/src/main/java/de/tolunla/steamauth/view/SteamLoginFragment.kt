package de.tolunla.steamauth.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import de.tolunla.steamauth.SteamLogin
import de.tolunla.steamauth.databinding.FragmentSteamLoginBinding
import de.tolunla.steamauth.util.SteamLoginResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SteamLoginFragment(private val listener: SteamLoginListener) : Fragment() {
    private lateinit var code: String
    private lateinit var captcha: String
    private lateinit var username: String
    private lateinit var password: String

    private lateinit var loginResult: SteamLoginResult

    private lateinit var binding: FragmentSteamLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSteamLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonAdd.setOnClickListener {
            code = binding.inputCode.text.toString()
            captcha = binding.inputCaptcha.text.toString()
            username = binding.inputUsername.text.toString()
            password = binding.inputPassword.text.toString()
            binding.layoutUsernameInput.error = null
            binding.layoutPasswordInput.error = null

            doLogin()
        }
    }

    private fun postLogin() {
        binding.layoutCaptcha.visibility = if (loginResult.captcha) View.VISIBLE else View.GONE
        if (loginResult.captcha) Picasso.get().load(loginResult.captchaURL).into(binding.imageCaptcha)

        binding.layoutCodeInput.visibility = if (loginResult.emailCode) View.VISIBLE else View.GONE

        if (!loginResult.captcha && !loginResult.mobileCode && !loginResult.emailCode) {
            binding.layoutUsernameInput.error = "Invalid username or password"
            binding.layoutPasswordInput.error = "Invalid username or password"
        }
    }

    private fun doLogin() {
        val steamLogin = SteamLogin(username, password)

        GlobalScope.launch(Dispatchers.IO) {
            loginResult = steamLogin.doLogin(captcha, code, code)

            if (!loginResult.success) {
                if (loginResult.mobileCode) {
                    Snackbar.make(
                        binding.root,
                        "Please remove the authenticator associated to this account.",
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                launch(Dispatchers.Main) { postLogin() }
            } else {
                listener.onSuccess(loginResult.oathToken, loginResult.steamID, username)
            }
        }
    }

    companion object {
        interface SteamLoginListener {
            fun onSuccess(token: String, steamID: String, username: String)
        }
    }
}