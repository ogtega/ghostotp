package de.tolunla.steamauth.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import de.tolunla.steamauth.SteamAuthLogin
import de.tolunla.steamauth.SteamAuthTwoFactor
import de.tolunla.steamauth.databinding.FragmentSteamLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SteamLoginFragment : Fragment() {

    private var captchaGid = "-1"
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
            GlobalScope.launch {
                val result = login()
                if (result.success) {
                    SteamAuthTwoFactor(result).enableTwoFactor()
                    findNavController().navigateUp()
                }
            }
        }
    }

    private suspend fun login(): SteamAuthLogin.LoginResult {
        val res = withContext(Dispatchers.IO) {
            return@withContext SteamAuthLogin(
                binding.inputUsername.text.toString(),
                binding.inputPassword.text.toString()
            ).doLogin(
                emailAuth = binding.inputCode.text.toString(),
                twoFactorCode = binding.inputCode.text.toString(),
                captcha = binding.inputCaptcha.text.toString(),
                captchaGid = captchaGid
            )
        }

        captchaGid = res.captchaGid

        binding.layoutLogin.visibility =
            if (!res.success && !(res.captcha || res.mobileCode || res.emailCode))
                View.VISIBLE else View.GONE

        if (res.captcha) Picasso.get().load(res.captchaURL).into(binding.imageCaptcha)

        binding.layoutCaptcha.visibility = if (res.captcha) View.VISIBLE else View.GONE

        binding.layoutCodeInput.visibility =
            if (res.emailCode || res.mobileCode) View.VISIBLE else View.GONE

        return res
    }
}