package de.tolunla.steamguard.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog.Builder
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import de.tolunla.steamguard.R
import de.tolunla.steamguard.SteamGuard
import de.tolunla.steamguard.databinding.FragmentSteamGuardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

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
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSteamGuardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlobalScope.launch(Dispatchers.IO) {
            result = steamGuard.enableTwoFactor()

            val status = result.optInt("status", -1)

            if (status != 1) {
                Snackbar.make(
                    binding.root, when (status) {
                        2 -> R.string.phone_not_attached
                        84 -> R.string.rate_limit_exceeded
                        else -> R.string.unexpected_error
                    }
                    , Snackbar.LENGTH_LONG
                ).show()

                launch(Dispatchers.Main) { activity?.onBackPressed() }
            } else {
                listener.onSteamGuardSuccess(result)
            }
        }
    }

    companion object {
        interface SteamGuardListener {
            fun onSteamGuardSuccess(result: JSONObject)
        }
    }
}