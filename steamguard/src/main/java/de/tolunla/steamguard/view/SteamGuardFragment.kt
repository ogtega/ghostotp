package de.tolunla.steamguard.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import de.tolunla.steamguard.R
import de.tolunla.steamguard.SteamGuard
import de.tolunla.steamguard.databinding.FragmentSteamGuardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Fragment where the user activates SteamGuard
 * @param token steam session token
 * @param steamId user steamID64
 */
class SteamGuardFragment(token: String, steamId: String) :
    DialogFragment() {

    private val steamGuard = SteamGuard(steamId, token)

    private lateinit var result: JSONObject
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
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSteamGuardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonAdd.isEnabled = false

        GlobalScope.launch(Dispatchers.IO) {
            result = steamGuard.enableTwoFactor()

            val status = result.optInt("status", -1)

            if (status != 1) {
                Snackbar.make(
                    binding.root,
                    when (status) {
                        2 -> R.string.phone_not_attached
                        84 -> R.string.rate_limit_exceeded
                        else -> R.string.unexpected_error
                    },
                    Snackbar.LENGTH_LONG
                ).show()

                launch(Dispatchers.Main) { activity?.onBackPressed() }
            }

            listener.onSteamGuardReceived(result)
            launch(Dispatchers.Main) { binding.buttonAdd.isEnabled = true }
        }

        binding.buttonAdd.setOnClickListener {

            GlobalScope.launch(Dispatchers.Main) {
                val success = async(Dispatchers.IO) {
                    steamGuard.finalizeTwoFactor(
                        result.getString("shared_secret"),
                        binding.inputSmsCode.text.toString()
                    )
                }

                listener.onSteamGuardComplete(success.await())
            }
        }
    }

    companion object {
        interface SteamGuardListener {

            /**
             * Called once we receive the SteamGuard json from steam
             */
            fun onSteamGuardReceived(result: JSONObject)

            /**
             * Called once the activation is completed
             */
            suspend fun onSteamGuardComplete(success: Boolean)
        }
    }
}
