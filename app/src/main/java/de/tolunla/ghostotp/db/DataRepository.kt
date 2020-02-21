package de.tolunla.ghostotp.db

import androidx.annotation.WorkerThread
import de.tolunla.ghostotp.db.entity.Account
import java.util.*

class DataRepository private constructor(private val database: AppDatabase) {
    companion object {

        @Volatile
        private var INSTANCE: DataRepository? = null

        fun getInstance(database: AppDatabase) = INSTANCE ?: synchronized(this) {
            INSTANCE ?: DataRepository(database)
        }
    }

    val accounts = database.accountDao().getAll()

    @WorkerThread
    fun insertAccount(account: Account) {
        var tries = 1
        val res = database.accountDao().insert(account)

        // If we're adding a duplicate account
        if (res == -1L) {

            // If there is an issuer provided we simply update the account within the database
            if (account.issuer.isNotEmpty()) {
                database.accountDao().update(account)
                return
            }

            // Repeatedly increment the number suffix until we succeed in inserting the account
            while (insertAccount(account, tries) == -1L) {
                tries++
            }
        }
    }

    private fun insertAccount(account: Account, tries: Int): Long {
        val copy = account.copy(
                name = "${account.name}($tries)",
                id = "${account.issuer.toLowerCase(Locale.ROOT)}${account.name}_$tries"
        )

        return database.accountDao().insert(copy)
    }

    @WorkerThread
    fun deleteAccount(account: Account) {
        database.accountDao().delete(account)
    }

    @WorkerThread
    fun increaseStep(account: Account) {
        database.accountDao().update(account.copy(step = account.step + 1))
    }

    @WorkerThread
    fun updateAccount(account: Account) {
        database.accountDao().update(account)
    }
}