package com.brahmanlabs.browser

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PasswordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PasswordItem)

    @Query("SELECT * FROM passwords ORDER BY timestamp DESC")
    suspend fun getAll(): List<PasswordItem>

    @Query("SELECT * FROM passwords WHERE domain = :domain LIMIT 1")
    suspend fun getByDomain(domain: String): PasswordItem?

    @Query("DELETE FROM passwords WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM passwords")
    suspend fun deleteAll()
}
