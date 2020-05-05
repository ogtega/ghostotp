package de.tolunla.ghostotp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.tolunla.ghostotp.db.dao.AccountDao
import de.tolunla.ghostotp.db.entity.AccountEntity

/**
 * Singleton instance of the app's room database
 */
@TypeConverters(AccountEntity.TypeStringConverter::class)
@Database(entities = [AccountEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Gets the database access object for accounts
     */
    abstract fun accountDao(): AccountDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton instance of the application database
         * @param context the application context
         * @return a room database instance
         */
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