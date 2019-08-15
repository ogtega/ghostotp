package de.tolunla.ghostotp.db

import androidx.annotation.WorkerThread
import de.tolunla.ghostotp.db.entity.Account

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
    database.accountDao().insert(account)
  }
}