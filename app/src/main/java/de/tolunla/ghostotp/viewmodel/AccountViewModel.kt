package de.tolunla.ghostotp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import de.tolunla.ghostotp.db.AppDatabase
import de.tolunla.ghostotp.db.DataRepository
import de.tolunla.ghostotp.db.entity.AccountEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountViewModel(context: Application) : AndroidViewModel(context) {

  private val repository: DataRepository
  val allAccounts: LiveData<List<AccountEntity>>

  init {
    val database = AppDatabase.getInstance(context)
    repository = DataRepository.getInstance(database)
    allAccounts = repository.accounts
  }

  fun insert(account: AccountEntity) = viewModelScope.launch(Dispatchers.IO) {
    repository.insertAccount(account)
  }
}