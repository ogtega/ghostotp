package de.tolunla.steamguard.view

import android.content.Context
import android.os.Bundle
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
 */
class SteamLoginFragment : Fragment() {
    private var loginResult = SteamLoginResult()
    private lateinit var binding: FragmentSteamLoginBinding
    private lateinit var listener: SteamLoginListener

    private val captchaBaseURL: String = "https://steamcommunity.com/login/rendercaptcha/?gid="

    override fun onAttach(context: Context) {
        super.onAttach(context)

        parentFragment?.let {
            if (it is SteamLoginListener) listener = it
            else activity?.onBackPressed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSteamLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonContinue.setOnClickListener {
            binding.layoutUsernameInput.error = null
            binding.layoutPasswordInput.error = null

            if (validateUsername() && validatePassword()) {
                binding.buttonContinue.isEnabled = false
                doLogin()
            } else {
                invalidateCredentials()
            }
        }
    }

    private fun postLogin() {
        binding.layoutCaptcha.visibility = if (loginResult.captcha) View.VISIBLE else View.GONE
        if (loginResult.captcha) Picasso.get().load(captchaBaseURL + loginResult.captchaGid)
            .into(binding.imageCaptcha)

        binding.layoutCodeInput.visibility = if (loginResult.emailCode) View.VISIBLE else View.GONE
        binding.layoutCodeInput.helperText =
            getString(R.string.hint_steam_email, loginResult.emailDomain)

        if (!loginResult.captcha && !loginResult.require2fa && !loginResult.emailCode) {
            invalidateCredentials()
        }

        binding.buttonContinue.isEnabled = true
    }

    private fun validateUsername(): Boolean = getUsername().length >= 5
    private fun validatePassword(): Boolean = getPassword().length >= 8

    private fun invalidateCredentials() {
        binding.layoutUsernameInput.error = getString(R.string.invalid_username_password)
        binding.layoutPasswordInput.error = getString(R.string.invalid_username_password)
    }

    private fun getUsername(): String = binding.inputUsername.text.toString()
    private fun getPassword(): String = binding.inputPassword.text.toString()
    private fun getCaptcha(): String = binding.inputCaptcha.text.toString()
    private fun getCode(): String = binding.inputCode.text.toString()

    private fun doLogin() {
        val steamLogin = SteamLogin(getUsername(), getPassword())

        GlobalScope.launch(Dispatchers.IO) {
            loginResult = steamLogin.doLogin(getCaptcha(), getCode(), loginResult)

            if (!loginResult.success) {
                if (loginResult.require2fa) {
                    Snackbar.make(
                        binding.root,
                        R.string.remove_authenticator,
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                launch(Dispatchers.Main) { postLogin() }
            } else {
                listener.onLoginSuccess(
                    loginResult.oathToken,
                    loginResult.steamID,
                    getUsername(),
                    loginResult.oathToken,
                    loginResult.cookies
                )
            }
        }
    }

    companion object {
        interface SteamLoginListener {
            /**
             * Called once we successfully complete the login api call
             */
            fun onLoginSuccess(
                token: String,
                steamID: String,
                username: String,
                oathToken: String,
                cookies: String
            )
        }
    }
}
