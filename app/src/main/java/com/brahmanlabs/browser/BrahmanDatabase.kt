package com.brahmanlabs.browser

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HistoryItem::class], version = 1, exportSchema = false)
abstract class BrahmanDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: BrahmanDatabase? = null

        fun getInstance(context: Context): BrahmanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrahmanDatabase::class.java,
                    "brahman_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
