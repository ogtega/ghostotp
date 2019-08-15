package de.tolunla.ghostotp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.tolunla.ghostotp.db.dao.AccountDao
import de.tolunla.ghostotp.db.entity.Account

@TypeConverters(
  Account.TypeStringConverter::class,
  Account.CryptoStringConverter::class
)
@Database(entities = arrayOf(Account::class), version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

  abstract fun accountDao(): AccountDao

  companion object {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
      INSTANCE ?: buildDatabase(context)
    }

    private fun buildDatabase(context: Context) =
      Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java, "ghost.db"
      ).build()
  }
}