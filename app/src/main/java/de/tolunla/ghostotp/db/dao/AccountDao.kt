package de.tolunla.ghostotp.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import de.tolunla.ghostotp.db.entity.AccountEntity

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Int): AccountEntity

    @Query("SELECT * FROM accounts ORDER BY name")
    fun getAll(): LiveData<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(account: AccountEntity): Long

    @Update
    fun update(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :accountID")
    fun delete(accountID: Int)
}