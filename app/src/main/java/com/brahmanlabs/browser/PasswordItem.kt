package com.brahmanlabs.browser

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class PasswordItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val domain: String,
    val username: String,
    val password: String,
    val timestamp: Long = System.currentTimeMillis()
)
