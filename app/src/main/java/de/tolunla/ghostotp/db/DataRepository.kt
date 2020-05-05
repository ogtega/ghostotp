package de.tolunla.ghostotp.db

import androidx.annotation.WorkerThread
import de.tolunla.ghostotp.db.entity.AccountEntity
import de.tolunla.ghostotp.model.Account
import de.tolunla.ghostotp.model.OTPAccount

class DataRepository private constructor(private val database: AppDatabase) {
    companion object {
        @Volatile
        private var INSTANCE: DataRepository? = null

        /**
         * Gets the application's data repository
         * @param database database instance used by the application
         * @return the singleton DataRepository
         */
        fun getInstance(database: AppDatabase) = INSTANCE ?: synchronized(this) {
            INSTANCE ?: DataRepository(database)
        }
    }

    val accounts = database.accountDao().getAll()

    /**
     * Inserts a new account into the database
     * @param account Account to be inserted into the database
     * @return the account id of the inserted account.
     */
    @WorkerThread
    fun insertAccount(account: Account): Long = database.accountDao().insert(account.toEntity())

    /**
     * Deletes an account by id
     * @param id the id of the AccountEntity to be deleted
     */
    @WorkerThread
    fun deleteAccount(id: Long) {
        database.accountDao().delete(id)
    }

    /**
     * Increments a OTPAccount's steps then save it in the database
     * @param account OTPAccount object who's step will increase
     */
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

    /**
     * Updates a single AccountEntity in the database
     * @param account AccountEntity to be updated
     */
    @WorkerThread
    fun updateAccount(account: AccountEntity) {
        database.accountDao().update(account)
    }
}