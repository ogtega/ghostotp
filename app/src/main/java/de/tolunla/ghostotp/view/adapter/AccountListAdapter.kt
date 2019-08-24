package de.tolunla.ghostotp.view.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.tolunla.ghostotp.databinding.ListItemAccountTotpBinding
import de.tolunla.ghostotp.db.entity.Account

class AccountListAdapter(context: Context) :
  RecyclerView.Adapter<AccountListAdapter.AccountViewHolder>() {

  private var accounts = emptyList<Account>()

  private val inflater = LayoutInflater.from(context)
  private val viewHolders = HashSet<AccountViewHolder>()
  private val mHandler = Handler(Looper.getMainLooper())

  private val updateCodes = object : Runnable {
    override fun run() {

      viewHolders.forEach {
        val account = accounts[it.adapterPosition]

        Log.d("Handler", "${account.name}: ${System.currentTimeMillis()}")
        it.update(account)
      }

      mHandler.postDelayed(this, System.currentTimeMillis().rem(250L))
    }
  }

  fun setAccounts(accounts: List<Account>) {
    this.accounts = accounts
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder =
    AccountViewHolder(ListItemAccountTotpBinding.inflate(inflater, parent, false))

  override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
    holder.update(accounts[position])
    viewHolders.add(holder)
  }

  override fun onViewRecycled(holder: AccountViewHolder) {
    viewHolders.remove(holder)
    super.onViewRecycled(holder)
  }

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    mHandler.post(updateCodes)
  }

  override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
    mHandler.removeCallbacks(updateCodes)
    super.onDetachedFromRecyclerView(recyclerView)
  }

  override fun getItemCount(): Int = accounts.size

  inner class AccountViewHolder(val binding: ListItemAccountTotpBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun update(account: Account) {
      binding.accountName.text = account.name
      binding.accountCode.text = account.oneTimePassword.generateCode().toString()
    }
  }
}