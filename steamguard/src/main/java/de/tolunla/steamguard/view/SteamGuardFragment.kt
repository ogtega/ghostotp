package de.tolunla.steamguard.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import de.tolunla.steamguard.databinding.FragmentSteamGuardBinding
import de.tolunla.steamguard.util.SteamGuardResult

class SteamGuardFragment(private val token: String, private val steamId: String) :
    DialogFragment() {

    private lateinit var binding: FragmentSteamGuardBinding
    private lateinit var listener: SteamGuardListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SteamGuardListener) listener = context
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
        TODO("Not yet implemented")
    }

    companion object {
        interface SteamGuardListener {
            fun onSteamGuardSuccess(result: SteamGuardResult)
        }
    }
}