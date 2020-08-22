package de.tolunla.ghostotp.view.fragment

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.tolunla.ghostotp.databinding.FragmentSteamConfirmationsBinding
import de.tolunla.steamguard.util.generateConfKey
import de.tolunla.steamguard.util.getDeviceId
import de.tolunla.steamguard.util.getWebViewClient

class SteamConfirmationFragment : Fragment() {
    private val confBaseURL = "https://steamcommunity.com/mobileconf"

    private lateinit var binding: FragmentSteamConfirmationsBinding

    val args: SteamConfirmationFragmentArgs by navArgs()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSteamConfirmationsBinding.inflate(inflater, container, false)
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = getWebViewClient()

        val time = System.currentTimeMillis() / 1000L

        Log.d(
            this.javaClass.canonicalName, "${args.secretKey} ${
                generateConfKey(
                    args.secretKey,
                    time,
                )
            }"
        )

        binding.webView.loadUrl(
            Uri.parse("${confBaseURL}/conf").buildUpon()
                .appendQueryParameter("p", getDeviceId(args.steamId))
                .appendQueryParameter("a", args.steamId).appendQueryParameter(
                    "k", generateConfKey(
                        args.secretKey,
                        time,
                    )
                ).appendQueryParameter("t", time.toString()).appendQueryParameter("m", "android")
                .appendQueryParameter("tag", "conf")
                .build().toString()
        )

        return binding.root
    }
}
