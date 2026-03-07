package com.brahmanlabs.browser
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "downloads")
data class DownloadItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val url: String,
    val mimeType: String,
    val timestamp: Long = System.currentTimeMillis()
)
