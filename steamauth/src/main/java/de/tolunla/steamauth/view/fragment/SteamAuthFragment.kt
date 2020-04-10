package de.tolunla.steamauth.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import de.tolunla.steamauth.SteamLogin
import de.tolunla.steamauth.SteamTwoFactor
import de.tolunla.steamauth.databinding.FragmentSteamLoginBinding
import de.tolunla.steamauth.util.SteamAuthResult
import de.tolunla.steamauth.util.SteamLoginResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SteamAuthFragment() : Fragment() {
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
        var loginResult: SteamLoginResult? = null
        var twoFactorResult: SteamAuthResult? = null

        binding.buttonAdd.setOnClickListener {
            GlobalScope.launch {
                loginResult = if (loginResult != null) loginResult else login()

                loginResult?.let { login ->
                    postLogin(login)

                    if (login.success) {
                        twoFactorResult = if (twoFactorResult != null)
                            twoFactorResult else SteamTwoFactor(login).enableTwoFactor()

                        twoFactorResult?.let { tfa ->
                            postTFA(tfa)

                            if (tfa.success) {
                                // TODO: Send the result to the activity
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun login(): SteamLoginResult {
        val res = doLogin()

        withContext(Dispatchers.Main) {
            binding.layoutLogin.visibility =
                if (!res.success && !(res.captcha || res.emailCode)) View.VISIBLE else View.GONE

            if (res.captcha) Picasso.get().load(res.captchaURL).into(binding.imageCaptcha)

            binding.layoutCaptcha.visibility = if (res.captcha) View.VISIBLE else View.GONE

            binding.layoutCodeInput.visibility = if (res.emailCode) View.VISIBLE else View.GONE
        }

        return res
    }

    private suspend fun doLogin() = withContext(Dispatchers.IO) {
        return@withContext SteamLogin(
            binding.inputUsername.text.toString(),
            binding.inputPassword.text.toString()
        ).doLogin(
            emailAuth = binding.inputCode.text.toString(),
            mobileCode = binding.inputCode.text.toString(),
            captcha = binding.inputCaptcha.text.toString()
        )
    }

    private fun postLogin(login: SteamLoginResult) {
        if (login.mobileCode) {
            Snackbar.make(
                binding.root,
                "Please remove the authenticator associated to this account.",
                Snackbar.LENGTH_LONG
            )
                .show()
            return
        }

        if (login.emailCode) {
            Snackbar.make(
                binding.root,
                "Enter the code sent to your e-mail address.",
                Snackbar.LENGTH_LONG
            )
                .show()
            return
        }
    }

    private fun postTFA(auth: SteamAuthResult) {
        if (auth.status == 2) {
            Snackbar.make(
                binding.root,
                "A phone number needs to be attached to your account.",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        if (auth.status == 84) {
            Snackbar.make(
                binding.root,
                "Rate limit exceeded. Try again later",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        if (auth.status != 1) {
            Snackbar.make(binding.root, "Error: Status ${auth.status}", Snackbar.LENGTH_LONG)
        }
    }
}
