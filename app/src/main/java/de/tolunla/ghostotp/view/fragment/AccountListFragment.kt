package de.tolunla.ghostotp.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import de.tolunla.ghostotp.databinding.FragmentAccountListBinding
import de.tolunla.ghostotp.view.adapter.AccountListAdapter
import de.tolunla.ghostotp.viewmodel.AccountViewModel

class AccountListFragment : Fragment() {

  private lateinit var accountAdapter: AccountListAdapter
  private lateinit var binding: FragmentAccountListBinding
  private lateinit var accountViewModel: AccountViewModel

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    setHasOptionsMenu(true)


    accountViewModel = ViewModelProviders.of(this).get(AccountViewModel::class.java)
    binding = FragmentAccountListBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    context?.let {
      accountAdapter = AccountListAdapter(it)
      binding.accountList.adapter = accountAdapter
      binding.accountList.layoutManager = LinearLayoutManager(it)
    }

    accountViewModel.allAccounts.observe(this, Observer { accounts ->
      accounts?.let {
        accountAdapter.setAccounts(accounts)
      }
    })
  }
}