package de.tolunla.ghostotp.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import de.tolunla.ghostotp.db.entity.AccountEntity

@Dao
interface AccountDao {

  @Query("SELECT * FROM accounts WHERE id = :id")
  suspend fun getById(id: String): AccountEntity

  @Query("SELECT * FROM accounts ORDER BY name")
  fun getAll(): LiveData<List<AccountEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(account: AccountEntity)

  @Update
  fun update(account: AccountEntity)

  @Delete
  fun delete(account: AccountEntity)
}