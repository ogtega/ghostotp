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

/**
 * Class used to hold the Account info used by the application
 * @param context application context
 */
class AccountViewModel(context: Application) : AndroidViewModel(context) {

    private val repository: DataRepository

    val allAccounts: LiveData<List<AccountEntity>>

    init {
        val database = AppDatabase.getInstance(context)
        repository = DataRepository.getInstance(database)
        allAccounts = repository.accounts
    }

    /**
     * Deletes accounts using the repository
     * @param account Account to be deleted
     */
    fun delete(account: Account) {
        val id = account.id ?: return
        delete(id)
    }

    /**
     * Deletes accounts using the repository
     * @param account Account to be deleted
     */
    private fun delete(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAccount(id)
    }

    /**
     * Inserts a new account into the database
     * @param account Account to be inserted into the database
     * @return the account id of the inserted account.
     */
    fun insert(account: Account): Long = runBlocking(Dispatchers.IO) {
        repository.insertAccount(account)
    }

    /**
     * Updates a single AccountEntity in the database
     * @param account AccountEntity to be updated
     */
    fun update(account: AccountEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateAccount(account)
    }

    /**
     * Increments a OTPAccount's steps then save it in the database
     * @param account OTPAccount object who's step will increase
     */
    fun increaseStep(account: OTPAccount) = viewModelScope.launch(Dispatchers.IO) {
        repository.increaseStep(account)
    }
}
