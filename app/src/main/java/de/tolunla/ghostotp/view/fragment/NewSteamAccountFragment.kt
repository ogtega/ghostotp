package de.tolunla.ghostotp.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import de.tolunla.ghostotp.R
import de.tolunla.ghostotp.databinding.DialogSteamRecoveryCodeBinding
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

/**
 * Fragment where the user inputs their steam account credentials
 */
class NewSteamAccountFragment : Fragment(), SteamLoginListener, SteamGuardListener {
    private lateinit var mSteamID: String
    private lateinit var mUsername: String
    private lateinit var mCookies: String
    private lateinit var oathToken: String
    private lateinit var mViewModel: AccountViewModel
    private lateinit var binding: FragmentNewSteamAccountBinding
    private lateinit var account: SteamAccount

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(AccountViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentNewSteamAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ft = childFragmentManager.beginTransaction()
        ft.replace(binding.fragment.id, SteamLoginFragment())
        ft.commit()
    }

    override fun onLoginSuccess(
        token: String,
        steamID: String,
        username: String,
        oathToken: String,
        cookies: String
    ) {
        this.mSteamID = steamID
        this.mUsername = username
        this.oathToken = oathToken
        this.mCookies = cookies

        val ft = childFragmentManager.beginTransaction()
        ft.replace(binding.fragment.id, SteamGuardFragment(token, steamID))
        ft.commit()
    }

    override fun onSteamGuardReceived(result: JSONObject) {
        account = SteamAccount(
            id = this.mSteamID.toLong(),
            name = result.getString("account_name"),
            sharedSecret = result.getString("shared_secret"),
            revocationCode = result.getString("revocation_code"),
            identitySecret = result.getString("identity_secret"),
            oathToken = this.oathToken,
            cookies = mCookies
        )

        mViewModel.insert(account)
    }

    override suspend fun onSteamGuardComplete(success: Boolean) {
        if (!success) {
            mViewModel.delete(account)
        }

        val dialogBinding = DialogSteamRecoveryCodeBinding.inflate(layoutInflater)
        dialogBinding.code = account.getRevocationCode()

        GlobalScope.launch(Dispatchers.Main) {
            findNavController().navigate(R.id.account_list_dest)
            val builder = AlertDialog.Builder(requireContext())

            with(builder) {
                setTitle(R.string.label_steam_recovery_code)

                setView(dialogBinding.root)

                setPositiveButton(R.string.action_done) { _, _ -> }

                show()
            }
        }
    }
}
