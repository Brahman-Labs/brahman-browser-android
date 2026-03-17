package com.brahmanlabs.browser

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReadingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ReadingItem)

    @Query("SELECT * FROM reading_list ORDER BY timestamp DESC")
    suspend fun getAll(): List<ReadingItem>

    @Query("SELECT EXISTS(SELECT 1 FROM reading_list WHERE url = :url)")
    suspend fun isInReadingList(url: String): Boolean

    @Query("DELETE FROM reading_list WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM reading_list")
    suspend fun deleteAll()
}
