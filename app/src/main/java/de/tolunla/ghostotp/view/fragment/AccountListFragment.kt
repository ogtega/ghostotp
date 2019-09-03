package de.tolunla.ghostotp.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.tolunla.ghostotp.databinding.FragmentAccountListBinding
import de.tolunla.ghostotp.view.adapter.AccountListAdapter
import de.tolunla.ghostotp.viewmodel.AccountViewModel

class AccountListFragment : Fragment() {

  private lateinit var accountAdapter: AccountListAdapter
  private lateinit var accountViewModel: AccountViewModel
  private lateinit var binding: FragmentAccountListBinding

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    activity?.let {
      accountViewModel = ViewModelProviders.of(it).get(AccountViewModel::class.java)

      accountAdapter.setViewModel(accountViewModel)

      accountViewModel.allAccounts.observe(this, Observer { accounts ->
        accounts?.let {
          accountAdapter.setAccounts(accounts)
        }
      })
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    setHasOptionsMenu(true)
    binding = FragmentAccountListBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    context?.let {
      accountAdapter = AccountListAdapter(it, childFragmentManager)
      binding.accountList.adapter = accountAdapter
      accountAdapter.onAttachedToRecyclerView(binding.accountList)
    }
  }

  override fun onDestroyView() {
    binding.accountList.adapter = null
    super.onDestroyView()
  }
}