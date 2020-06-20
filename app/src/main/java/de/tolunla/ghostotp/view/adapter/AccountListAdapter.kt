package de.tolunla.ghostotp.view.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import de.tolunla.ghostotp.R
import de.tolunla.ghostotp.databinding.LayoutDialogInputBinding
import de.tolunla.ghostotp.databinding.ListItemAccountOtpBinding
import de.tolunla.ghostotp.databinding.SheetAccountActionBinding
import de.tolunla.ghostotp.db.entity.AccountEntity
import de.tolunla.ghostotp.db.entity.AccountEntity.Type.HOTP
import de.tolunla.ghostotp.model.Account
import de.tolunla.ghostotp.model.OTPAccount
import de.tolunla.ghostotp.util.AccountDiffCallback
import de.tolunla.ghostotp.viewmodel.AccountViewModel

/**
 * Class used to adapt views for the account list
 * @param context context of the activity/fragment
 */
class AccountListAdapter(val context: Context) :
    RecyclerView.Adapter<AccountListAdapter.AccountViewHolder>() {

    private var accountList = emptyList<AccountEntity>()

    private val mClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private val mInflater = LayoutInflater.from(context)
    private val mTOTPHolders = HashSet<AccountViewHolder>()
    private val mHandler = Handler(Looper.getMainLooper())

    private lateinit var mViewModel: AccountViewModel

    /**
     * A task that runs periodically to update TOTP codes
     */
    private val updateTOTPCodes = object : Runnable {
        override fun run() {
            mTOTPHolders.forEach(AccountViewHolder::refresh)
            mHandler.postDelayed(this, System.currentTimeMillis().rem(200L))
        }
    }

    /**
     * Updates the account list and notifies the adapter of changes
     * @param accounts list of accounts that will be displayed
     */
    fun setAccounts(accounts: List<AccountEntity>) {
        val diffCallback = AccountDiffCallback(accountList, accounts)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        accountList = accounts
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Sets the viewmodel that will be used by the adapter
     * @param viewModel instance of the class that holds Account information
     */
    fun setViewModel(viewModel: AccountViewModel) {
        mViewModel = viewModel
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val holder = AccountViewHolder(ListItemAccountOtpBinding.inflate(mInflater, parent, false))

        holder.binding.root.setOnLongClickListener {
            holder.showActionSheet(parent)
            true
        }

        holder.binding.root.setOnClickListener {
            if (holder.account.type == HOTP) {
                holder.refresh()
                holder.timeout = System.currentTimeMillis() + DateUtils.MINUTE_IN_MILLIS

                // Re-enable the refresh button 6 seconds later
                mHandler.postDelayed({
                    holder.binding.root.isEnabled = true
                }, 6 * DateUtils.SECOND_IN_MILLIS)

                // Clear the displayed HOTP code after 1 minute
                mHandler.postDelayed({
                    if (holder.timeout < System.currentTimeMillis())
                        holder.bind(holder.account)
                }, DateUtils.MINUTE_IN_MILLIS)
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account: Account = accountList[position].getAccount()

        holder.bind(account)

        if (account.type != HOTP) {
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

    override fun getItemCount(): Int = accountList.size

    /**
     * Class containing view objects and variables for accounts
     * @param binding binding of a ListItem view
     */
    inner class AccountViewHolder(val binding: ListItemAccountOtpBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var code: String = ""
        var timeout: Long = -1
        lateinit var account: Account

        /**
         * Binds an Account to the ViewHolder
         * @param account the account that's bound
         */
        fun bind(account: Account) {
            this.account = account
            binding.accountName.text = account.getLabel()

            if (account is OTPAccount && account.type == HOTP) {
                bind(account)
                return
            }

            binding.timeBased = true
            refresh()
        }

        /**
         * Binds a OTPAccount to the ViewHolder
         * @param account the account that's bound
         */
        private fun bind(account: OTPAccount) {
            binding.accountCode.text = "- ".repeat(account.digits)
            binding.timeBased = false
        }

        /**
         * Refreshes the views within the holder
         */
        fun refresh() {
            when (account.type) {
                HOTP -> {
                    binding.root.isEnabled = false
                    mViewModel.increaseStep(account as OTPAccount)
                }
                else -> binding.countdownIndicator.setPhase(1 - account.getProgress())
            }

            // Set the code based off the current time, then update the code text
            code = account.generateCode()
            binding.accountCode.text = code
        }

        /**
         * Shows the bottom sheet for managing an account
         * @param parent view parent of the holder
         */
        fun showActionSheet(parent: View) {
            val binding = SheetAccountActionBinding.inflate(mInflater)
            val dialog = BottomSheetDialog(context)

            dialog.setContentView(binding.root)

            binding.navView.setNavigationItemSelectedListener { item ->

                when (item.itemId) {

                    R.id.action_edit_account -> {
                        editAccount()
                    }

                    R.id.action_copy_account -> {
                        copyCode(parent)
                    }

                    R.id.action_delete_account -> {
                        deleteAccount()
                    }
                }

                dialog.dismiss()
                true
            }

            dialog.show()
        }

        private fun editAccount() {
            val builder = AlertDialog.Builder(context)

            val title = String.format(
                context.resources.getString(R.string.message_rename_account), account.name
            )

            val binding = LayoutDialogInputBinding.inflate(mInflater)
            binding.etInput.setText(account.name)

            with(builder) {
                setTitle(title)

                setPositiveButton(R.string.action_rename) { _, _ ->
                    val updated = account.toEntity()
                        .copy(name = binding.etInput.text.toString())
                    mViewModel.update(updated)
                }

                setNegativeButton(R.string.action_cancel) { dialog, _ ->
                    dialog.dismiss()
                }

                setView(binding.root)
                show()
            }
        }

        private fun deleteAccount() {
            val builder = AlertDialog.Builder(context)

            val title = String.format(
                context.resources.getString(R.string.message_remove_account), account.name
            )

            with(builder) {
                setTitle(title)
                setMessage(R.string.message_account_deletion)

                setPositiveButton(R.string.action_remove) { _, _ ->
                    mViewModel.delete(account)
                }

                setNegativeButton(R.string.action_cancel) { dialog, _ ->
                    dialog.dismiss()
                }

                show()
            }
        }

        private fun copyCode(view: View) {
            val clipData = ClipData.newPlainText("text", code)
            mClipboard.setPrimaryClip(clipData)

            // Notify the user that we've copied the OTP
            Toast.makeText(
                context,
                context.getText(R.string.message_otp_copied),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}