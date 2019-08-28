package de.tolunla.ghostotp.view.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import android.util.Log
import android.util.TimeUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import de.tolunla.ghostotp.databinding.ListItemAccountTotpBinding
import de.tolunla.ghostotp.db.entity.Account
import java.text.DateFormat

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

      mHandler.postDelayed(this, System.currentTimeMillis().rem(500L))
    }
  }

  fun setAccounts(accounts: List<Account>) {
    viewHolders.clear()
    this.accounts = accounts
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
    val holder = AccountViewHolder(ListItemAccountTotpBinding.inflate(inflater, parent, false))

    holder.binding.root.setOnLongClickListener {
      Snackbar.make(parent, holder.code, Snackbar.LENGTH_SHORT).show()
      true
    }

    holder.binding.accountCode.setOnClickListener {
      Snackbar.make(parent, holder.binding.accountName.text, Snackbar.LENGTH_SHORT).show()
    }

    return holder
  }

  override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
    val account : Account = accounts[position]

    holder.update(account)

    if (account.type != Account.Type.HOTP) {
      viewHolders.add(holder)
    }
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

    lateinit var code : String

    fun update(account: Account) {
      // Convert all times stored in the account object to milliseconds for precision purposes
      val epoch = account.epoch * DateUtils.SECOND_IN_MILLIS
      val period = account.period * DateUtils.SECOND_IN_MILLIS

      binding.accountName.text = account.name

      code = account.oneTimePassword.generateCode()
      binding.accountCode.text = code

      // Get a float value representing the percentage of time left till our code is invalid
      val progress : Float =
        (System.currentTimeMillis() - epoch).rem(period).toFloat() / period.toFloat()

      binding.countdownIndicator.setPhase(1 - progress)
    }
  }
}