package de.tolunla.ghostotp.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import de.tolunla.ghostotp.db.entity.AccountEntity

@Dao
interface AccountDao {

  @Query("SELECT * FROM accounts WHERE id = :id")
  suspend fun getAccountById(id: String): AccountEntity

  @Query("SELECT * FROM accounts ORDER BY name")
  fun getAllAccounts(): LiveData<List<AccountEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAccount(account: AccountEntity)

  @Update
  fun updateAccount(account: AccountEntity)

  @Delete
  fun deleteAccount(account: AccountEntity)
}