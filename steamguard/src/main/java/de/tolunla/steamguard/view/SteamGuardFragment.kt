package de.tolunla.steamguard.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import de.tolunla.steamguard.SteamGuard
import de.tolunla.steamguard.databinding.FragmentSteamGuardBinding
import de.tolunla.steamguard.util.SteamGuardResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SteamGuardFragment(private val token: String, private val steamId: String) :
    DialogFragment() {

    private val steamGuard = SteamGuard(steamId, token)

    private lateinit var binding: FragmentSteamGuardBinding
    private lateinit var listener: SteamGuardListener

    override fun onAttach(context: Context) {
        super.onAttach(context)

        parentFragment?.let {
            if (it is SteamGuardListener) listener = it
            else activity?.onBackPressed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSteamGuardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlobalScope.launch(Dispatchers.IO) {
            val steamGuardResult = steamGuard.enableTwoFactor()

            /**
             * response.status:
             * 02 - Phone number not attached to account
             * 84 - RateLimitExceeded
             */

            Log.i("Steam", "success: ${steamGuardResult.success}, ${steamGuardResult.status}")
            listener.onSteamGuardSuccess(steamGuardResult)
        }
    }

    companion object {
        interface SteamGuardListener {
            fun onSteamGuardSuccess(result: SteamGuardResult)
        }
    }
}