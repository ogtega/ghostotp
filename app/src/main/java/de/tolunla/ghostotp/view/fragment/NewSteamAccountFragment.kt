package de.tolunla.ghostotp.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import de.tolunla.ghostotp.R
import de.tolunla.ghostotp.databinding.FragmentNewSteamAccountBinding
import de.tolunla.ghostotp.model.SteamAccount
import de.tolunla.ghostotp.viewmodel.AccountViewModel
import de.tolunla.steamguard.view.SteamGuardFragment
import de.tolunla.steamguard.view.SteamGuardFragment.Companion.SteamGuardListener
import de.tolunla.steamguard.view.SteamLoginFragment
import de.tolunla.steamguard.view.SteamLoginFragment.Companion.SteamLoginListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class NewSteamAccountFragment : Fragment(), SteamLoginListener, SteamGuardListener {

    private var accountId: Long = -1
    private lateinit var token: String
    private lateinit var steamID: String
    private lateinit var username: String
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var binding: FragmentNewSteamAccountBinding

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let {
            accountViewModel = ViewModelProvider(it).get(AccountViewModel::class.java)
        }
    }

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

    override fun onSteamGuardReceived(result: JSONObject) {
        accountId = accountViewModel.insert(
            SteamAccount(
                name = result.getString("account_name"),
                sharedSecret = result.getString("shared_secret"),
                revocationCode = result.getString("revocation_code"),
                identitySecret = result.getString("identity_secret")
            )
        )
    }

    override suspend fun onSteamGuardComplete(success: Boolean) {
        if (!success) {
            accountViewModel.delete(accountId)
        }

        GlobalScope.launch(Dispatchers.Main) {
            findNavController().navigate(R.id.account_list_dest)
        }
    }
}