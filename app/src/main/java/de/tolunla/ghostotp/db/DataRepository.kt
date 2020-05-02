package de.tolunla.ghostotp.db

import androidx.annotation.WorkerThread
import de.tolunla.ghostotp.db.entity.AccountEntity
import de.tolunla.ghostotp.model.Account
import de.tolunla.ghostotp.model.OTPAccount

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
        database.accountDao().insert(account.toEntity())
    }

    @WorkerThread
    fun deleteAccount(account: Account) {
        account.id?.let { database.accountDao().delete(it) }
    }

    @WorkerThread
    fun increaseStep(account: OTPAccount) {
        account.step++
        database.accountDao().update(
            AccountEntity(
                account.id,
                account.name,
                account.issuer,
                account.getJSON(),
                AccountEntity.Type.HOTP
            )
        )
    }

    @WorkerThread
    fun updateAccount(account: AccountEntity) {
        database.accountDao().update(account)
    }
}