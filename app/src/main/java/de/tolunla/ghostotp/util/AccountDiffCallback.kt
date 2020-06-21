package de.tolunla.ghostotp.util

import androidx.recyclerview.widget.DiffUtil
import de.tolunla.ghostotp.db.entity.AccountEntity

/**
 * Class used for getting the difference of two AccountEntity lists
 * @param oldList the old list
 * @param newList the new and updated list
 */
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
