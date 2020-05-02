package de.tolunla.steamguard.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import de.tolunla.steamguard.R
import de.tolunla.steamguard.SteamLogin
import de.tolunla.steamguard.databinding.FragmentSteamLoginBinding
import de.tolunla.steamguard.util.SteamLoginResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Class responsible for making all login related api requests.
 *
 * @property listener listening interface for when login is complete.
 * @constructor Creates an steamguard object with a user's credentials.
 */
class SteamLoginFragment : Fragment() {
    private lateinit var code: String
    private lateinit var captcha: String
    private lateinit var username: String
    private lateinit var password: String

    private lateinit var loginResult: SteamLoginResult

    private lateinit var binding: FragmentSteamLoginBinding
    private lateinit var listener: SteamLoginListener

    override fun onAttach(context: Context) {
        super.onAttach(context)

        parentFragment?.let {
            if (it is SteamLoginListener) listener = it
            else activity?.onBackPressed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSteamLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonContinue.setOnClickListener {
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
        if (loginResult.captcha) Picasso.get().load(loginResult.captchaURL)
            .into(binding.imageCaptcha)

        binding.layoutCodeInput.visibility = if (loginResult.emailCode) View.VISIBLE else View.GONE

        if (!loginResult.captcha && !loginResult.mobileCode && !loginResult.emailCode) {
            binding.layoutUsernameInput.error = getString(R.string.invalid_username_password)
            binding.layoutPasswordInput.error = getString(R.string.invalid_username_password)
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
                        R.string.remove_authenticator,
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                launch(Dispatchers.Main) { postLogin() }
            } else {
                listener.onLoginSuccess(loginResult.oathToken, loginResult.steamID, username)
            }
        }
    }

    companion object {
        interface SteamLoginListener {
            fun onLoginSuccess(token: String, steamID: String, username: String)
        }
    }
}