package com.lura.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val filePath: String,
    
    // Cover & Metadata
    val coverImagePath: String? = null,
    val publisher: String? = null,
    val publicationDate: String? = null,
    val isbn: String? = null,
    val description: String? = null,
    val fileSize: Long = 0,
    
    // Reading Progress
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val currentChapter: Int = 0,
    val progressPercentage: Float = 0f,
    val lastReadTimestamp: Long = 0,
    val readingTimeMinutes: Int = 0,
    
    // Organization
    val folderId: String? = null,
    val tags: String = "", // Comma-separated tags
    val isFavorite: Boolean = false,
    val readingStatus: String = "UNREAD", // UNREAD, READING, FINISHED
    
    // Legacy fields (for compatibility)
    val coverUrl: String? = coverImagePath,
    val progress: Float = progressPercentage,
    val totalWords: Int = 0,
    val importDate: Long = System.currentTimeMillis()
)
