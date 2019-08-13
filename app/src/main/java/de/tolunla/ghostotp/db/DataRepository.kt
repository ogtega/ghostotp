package de.tolunla.ghostotp.db

import androidx.annotation.WorkerThread
import de.tolunla.ghostotp.db.entity.AccountEntity

class DataRepository constructor(private val database: AppDatabase) {
  companion object {

    @Volatile
    private var INSTANCE: DataRepository? = null

    fun getInstance(database: AppDatabase) = INSTANCE ?: synchronized(this) {
      INSTANCE ?: DataRepository(database)
    }
  }

  val accounts = database.accountDao().getAllAccounts()

  @WorkerThread
  suspend fun insert(account: AccountEntity) {
    database.accountDao().insertAccount(account)
  }
}