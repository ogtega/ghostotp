package de.tolunla.ghostotp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import de.tolunla.ghostotp.db.AppDatabase
import de.tolunla.ghostotp.db.DataRepository
import de.tolunla.ghostotp.db.entity.AccountEntity
import de.tolunla.ghostotp.model.Account
import de.tolunla.ghostotp.model.OTPAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AccountViewModel(context: Application) : AndroidViewModel(context) {

    private val repository: DataRepository

    val allAccounts: LiveData<List<AccountEntity>>

    init {
        val database = AppDatabase.getInstance(context)
        repository = DataRepository.getInstance(database)
        allAccounts = repository.accounts
    }

    fun delete(account: Account) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAccount(account)
    }

    fun delete(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAccount(id)
    }

    fun insert(account: Account): Long = runBlocking(Dispatchers.IO) {
        repository.insertAccount(account)
    }

    fun update(account: AccountEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateAccount(account)
    }

    fun increaseStep(account: OTPAccount) = viewModelScope.launch(Dispatchers.IO) {
        repository.increaseStep(account)
    }
}