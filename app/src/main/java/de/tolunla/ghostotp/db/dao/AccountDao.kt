package de.tolunla.ghostotp.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import de.tolunla.ghostotp.db.entity.AccountEntity

/**
 * Class containing methods to execute database queries
 */
@Dao
interface AccountDao {

    /**
     * Gets an account by id
     * @param id the id used for the account search
     * @return an AccountEntity as a result
     */
    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Int): AccountEntity

    /**
     * Gets all accounts in the database
     * @return a list of AccountEntity
     */
    @Query("SELECT * FROM accounts ORDER BY name")
    fun getAll(): LiveData<List<AccountEntity>>

    /**
     * Inserts a new AccountEntity to the database
     * @param account the account to insert
     * @return the id of the newly inserted AccountEntity
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(account: AccountEntity): Long

    /**
     * Updates an AccountEntity in the database
     * @param account the new AccountEntity used for the update
     */
    @Update
    fun update(account: AccountEntity)

    /**
     * Deletes an AccountEntity by it's id
     * @param accountID id of the account to be deleted
     */
    @Query("DELETE FROM accounts WHERE id = :accountID")
    fun delete(accountID: Long)
}