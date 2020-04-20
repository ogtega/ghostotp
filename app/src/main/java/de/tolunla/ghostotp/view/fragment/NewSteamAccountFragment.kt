package de.tolunla.ghostotp.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.tolunla.ghostotp.databinding.FragmentNewSteamAccountBinding
import de.tolunla.steamguard.util.SteamGuardResult
import de.tolunla.steamguard.view.SteamGuardFragment.Companion.SteamGuardListener
import de.tolunla.steamguard.view.SteamLoginFragment
import de.tolunla.steamguard.view.SteamLoginFragment.Companion.SteamLoginListener

class NewSteamAccountFragment : Fragment(), SteamLoginListener, SteamGuardListener {

    private lateinit var binding: FragmentNewSteamAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewSteamAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ft = childFragmentManager.beginTransaction()
        ft.replace(binding.fragment.id, SteamLoginFragment())
        ft.commit()
    }

    override fun onLoginSuccess(token: String, steamID: String, username: String) {
        TODO("Not yet implemented")
    }

    override fun onSteamGuardSuccess(result: SteamGuardResult) {
        TODO("Not yet implemented")
    }
}