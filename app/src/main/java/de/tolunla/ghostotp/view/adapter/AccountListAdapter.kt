package de.tolunla.ghostotp.view.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import de.tolunla.ghostotp.R
import de.tolunla.ghostotp.databinding.ListItemAccountOtpBinding
import de.tolunla.ghostotp.db.AppDatabase
import de.tolunla.ghostotp.db.DataRepository
import de.tolunla.ghostotp.db.entity.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountListAdapter(val context: Context) :
  RecyclerView.Adapter<AccountListAdapter.AccountViewHolder>() {

  private var mAccounts = emptyList<Account>()

  private val mInflater = LayoutInflater.from(context)
  private val mTOTPHolders = HashSet<AccountViewHolder>()
  private val mHandler = Handler(Looper.getMainLooper())

  private val mRepository = DataRepository.getInstance(AppDatabase.getInstance(context))

  private val updateTOTPCodes = object : Runnable {

    override fun run() {
      mTOTPHolders.forEach {
        it.refreshTOTP()
      }

      mHandler.postDelayed(this, System.currentTimeMillis().rem(500L))
    }
  }

  fun setAccounts(accounts: List<Account>) {
    mTOTPHolders.clear()
    this.mAccounts = accounts
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
    val holder = AccountViewHolder(ListItemAccountOtpBinding.inflate(mInflater, parent, false))

    holder.binding.root.setOnLongClickListener {
      // Sequence to copying the selected OTP code to the user's clipboard
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      val clip: ClipData = ClipData.newPlainText("${holder.account.name} otp", holder.code)
      clipboard.setPrimaryClip(clip)

      // Notify the user that we've copied the OTP
      Snackbar.make(parent, context.getText(R.string.message_otp_copied), Snackbar.LENGTH_SHORT)
        .setAnchorView(R.id.fab)
        .show()

      true
    }

    holder.binding.btnRefresh.setOnClickListener {

      // Disable the refresh button
      holder.binding.btnRefresh.isEnabled = false

      // Re-enable the refresh button 6 seconds later
      mHandler.postDelayed(
        {
          holder.binding.btnRefresh.isEnabled = true
        },
        6 * DateUtils.SECOND_IN_MILLIS
      )

      // Clear the displayed HOTP code after 1 minute
      mHandler.postDelayed(
        {
          holder.binding.accountCode.text = "- ".repeat(holder.account.digits)
        },
        1 * DateUtils.MINUTE_IN_MILLIS
      )

      holder.refreshHOTP()

      // Save the account with it's new count to the database
      CoroutineScope(Dispatchers.IO).launch {
        mRepository.updateAccount(holder.account)
      }
    }

    return holder
  }

  override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
    val account: Account = mAccounts[position]

    holder.init(account)

    if (account.type != Account.Type.HOTP) {
      mTOTPHolders.add(holder)
    }
  }

  override fun onViewRecycled(holder: AccountViewHolder) {
    mTOTPHolders.remove(holder)
    super.onViewRecycled(holder)
  }

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    mHandler.post(updateTOTPCodes)
  }

  override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
    mHandler.removeCallbacks(updateTOTPCodes)
    super.onDetachedFromRecyclerView(recyclerView)
  }

  override fun getItemCount(): Int = mAccounts.size

  inner class AccountViewHolder(val binding: ListItemAccountOtpBinding) :
    RecyclerView.ViewHolder(binding.root) {

    var code: String = ""
    lateinit var account: Account

    fun init(account: Account) {
      this.account = account
      binding.accountName.text = account.name

      if (account.type == Account.Type.HOTP) {
        binding.accountCode.text = "- ".repeat(account.digits)
        binding.timeBase = false
      } else {
        binding.timeBase = true
        refreshTOTP()
      }
    }

    fun refreshTOTP() {
      // Convert all times stored in the account object to milliseconds for precision purposes
      val epoch = account.epoch * DateUtils.SECOND_IN_MILLIS
      val period = account.period * DateUtils.SECOND_IN_MILLIS

      // Get a float value representing the percentage of time left till our code is invalid
      val progress: Float =
        (System.currentTimeMillis() - epoch).rem(period).toFloat() / period.toFloat()

      binding.countdownIndicator.setPhase(1 - progress)

      // Set the code based off the current time, then update the code text
      code = account.oneTimePassword.generateCode()
      binding.accountCode.text = code
    }

    fun refreshHOTP() {
      code = account.oneTimePassword.generateCode()
      binding.accountCode.text = code
    }
  }
}