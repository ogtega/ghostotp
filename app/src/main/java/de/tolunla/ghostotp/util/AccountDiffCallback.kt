package de.tolunla.ghostotp.util

import androidx.recyclerview.widget.DiffUtil
import de.tolunla.ghostotp.db.entity.Account

class AccountDiffCallback(private val oldList: List<Account>,
  private val newList: List<Account>) : DiffUtil.Callback() {

  override fun getOldListSize() = oldList.size

  override fun getNewListSize() = newList.size

  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return oldList[oldItemPosition] == newList[newItemPosition]
  }

  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    val old = oldList[oldItemPosition]
    val new = newList[newItemPosition]

    return old.secret == new.secret
  }
}