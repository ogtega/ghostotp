package de.tolunla.ghostotp.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.tolunla.ghostotp.databinding.FragmentNewSteamAccountBinding
import de.tolunla.steamguard.view.SteamGuardFragment
import de.tolunla.steamguard.view.SteamGuardFragment.Companion.SteamGuardListener
import de.tolunla.steamguard.view.SteamLoginFragment
import de.tolunla.steamguard.view.SteamLoginFragment.Companion.SteamLoginListener
import org.json.JSONObject

class NewSteamAccountFragment : Fragment(), SteamLoginListener, SteamGuardListener {

    private lateinit var token: String
    private lateinit var steamID: String
    private lateinit var username: String
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
        this.token = token
        this.steamID = steamID
        this.username = username

        val ft = childFragmentManager.beginTransaction()
        ft.replace(binding.fragment.id, SteamGuardFragment(token, steamID))
        ft.commit()
    }

    override fun onSteamGuardSuccess(result: JSONObject) {
        context?.let {
            // AppDatabase.getInstance(it).accountDao().insert()
        }
        Log.i("Steam", "$username: ${result.toString()}")
    }
}