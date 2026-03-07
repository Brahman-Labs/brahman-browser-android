package com.brahmanlabs.browser
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DownloadItem)
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    suspend fun getAll(): List<DownloadItem>
    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: Int)
    @Query("DELETE FROM downloads")
    suspend fun deleteAll()
}
