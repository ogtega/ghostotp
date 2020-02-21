package de.tolunla.ghostotp.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import de.tolunla.ghostotp.db.entity.Account

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: String): Account

    @Query("SELECT * FROM accounts ORDER BY name")
    fun getAll(): LiveData<List<Account>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(account: Account): Long

    @Update
    fun update(account: Account)

    @Delete
    fun delete(account: Account)
}