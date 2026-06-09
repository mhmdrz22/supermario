package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(entities = [ResistanceTask::class, TransmissionLog::class, MarioHighScore::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun resistanceDao(): ResistanceDao
    abstract fun marioDao(): MarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Cyber security override key for AES-256 local database encryption
                val passphrase = SQLiteDatabase.getBytes("SYNDICATE_OVERRIDE_KEY_9982".toCharArray())
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "resistance_secure_datastore_encrypted.db"
                )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
