package de.tolunla.ghostotp.util

import androidx.recyclerview.widget.DiffUtil
import de.tolunla.ghostotp.db.entity.AccountEntity

class AccountDiffCallback(
    private val oldList: List<AccountEntity>,
    private val newList: List<AccountEntity>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition].getAccount()
        val new = newList[newItemPosition].getAccount()

        return old.getLabel() == new.getLabel()
    }
}