package de.tolunla.ghostotp.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import de.tolunla.ghostotp.R
import de.tolunla.ghostotp.databinding.FragmentAccountListBinding
import de.tolunla.ghostotp.view.adapter.AccountListAdapter
import de.tolunla.ghostotp.viewmodel.AccountViewModel

/**
 * Fragment that acts as the application's home screen
 */
class AccountListFragment : Fragment() {

    private lateinit var accountAdapter: AccountListAdapter
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var binding: FragmentAccountListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = FragmentAccountListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountAdapter = AccountListAdapter(requireContext())
        binding.accountList.adapter = accountAdapter
        accountAdapter.onAttachedToRecyclerView(binding.accountList)

        accountViewModel =
            ViewModelProvider(requireActivity()).get(AccountViewModel::class.java)

        accountAdapter.setViewModel(accountViewModel)

        accountViewModel.allAccounts.observe(
            viewLifecycleOwner,
            { accounts -> accountAdapter.setAccounts(accounts ?: listOf()) }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.appbar_menu, menu)
    }

    override fun onResume() {
        accountAdapter.resumeUpdates()
        super.onResume()
    }

    override fun onPause() {
        accountAdapter.pauseUpdates()
        super.onPause()
    }

    override fun onDestroyView() {
        binding.accountList.adapter = null
        super.onDestroyView()
    }
}
