package com.brahmanlabs.browser

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: BookmarkItem)

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    suspend fun getAll(): List<BookmarkItem>

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url)")
    suspend fun isBookmarked(url: String): Boolean

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteByUrl(url: String)

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAll()

    @Query("SELECT * FROM bookmarks WHERE url LIKE :query OR title LIKE :query ORDER BY timestamp DESC")
    suspend fun search(query: String): List<BookmarkItem>
}
