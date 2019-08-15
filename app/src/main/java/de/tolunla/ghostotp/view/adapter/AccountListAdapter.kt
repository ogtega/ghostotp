package de.tolunla.ghostotp.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.tolunla.ghostotp.databinding.ListItemAccountTotpBinding
import de.tolunla.ghostotp.db.entity.AccountEntity

class AccountListAdapter internal constructor(val context: Context) :
  RecyclerView.Adapter<AccountListAdapter.AccountViewHolder>() {

  private var accounts = emptyList<AccountEntity>()
  private val inflater = LayoutInflater.from(context)

  internal fun setAccounts(accounts: List<AccountEntity>) {
    this.accounts = accounts
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder =
    AccountViewHolder(ListItemAccountTotpBinding.inflate(inflater, parent, false))

  override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
    holder.update(accounts[position])
  }

  inner class AccountViewHolder internal constructor(val binding: ListItemAccountTotpBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun update(accountEntity: AccountEntity) {
      binding.accountName.text = accountEntity.name
    }
  }

  override fun getItemCount(): Int = accounts.size
}