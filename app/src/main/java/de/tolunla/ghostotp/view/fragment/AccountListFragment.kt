package de.tolunla.ghostotp.view.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import kotlin.concurrent.fixedRateTimer


class AccountListFragment : Fragment() {

  private lateinit var accountAdapter: AccountListAdapter
  private lateinit var accountViewModel: AccountViewModel
  private lateinit var binding: FragmentAccountListBinding

  private val mHandler = Handler(Looper.getMainLooper())


  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    activity?.let {
      accountViewModel = ViewModelProviders.of(it).get(AccountViewModel::class.java)

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
      accountAdapter = AccountListAdapter(it)
      binding.accountList.adapter = accountAdapter
      binding.accountList.layoutManager = LinearLayoutManager(it)
    }

    fixedRateTimer("", false, System.currentTimeMillis().rem(250), 250) {
      // TODO: Update views without adapter.notifyDataSetChanged()
    }
  }
}