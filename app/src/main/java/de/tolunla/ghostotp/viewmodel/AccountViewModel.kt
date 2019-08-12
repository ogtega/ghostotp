package de.tolunla.ghostotp.viewmodel

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import de.tolunla.ghostotp.AppContext
import de.tolunla.ghostotp.db.AppDatabase
import de.tolunla.ghostotp.db.dao.AccountDao
import de.tolunla.ghostotp.db.entity.AccountEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountViewModel(context: AppContext) : AndroidViewModel(context) {

    private val repository: AccountDao
    private val allAccounts: LiveData<List<AccountEntity>>

    init {
        val database = AppDatabase.getInstance(context)
        repository = database.accountDao()
        allAccounts = repository.getAllAccounts()
    }

    fun insert(account: AccountEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertAccount(account)
    }
}