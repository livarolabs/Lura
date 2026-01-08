package com.lura.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val filePath: String,
    
    // Cover & Enhanced Metadata
    val coverImagePath: String? = null,
    val publisher: String? = null,
    val publicationDate: String? = null,
    val isbn: String? = null,
    val description: String? = null,
    val genre: String? = null,
    val language: String? = null,
    val fileSize: Long = 0,
    val wordCount: Int = 0,
    val estimatedReadingTimeMinutes: Int = 0,
    
    // Reading Progress
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val currentChapter: Int = 0,
    val progressPercentage: Float = 0f,
    val lastReadTimestamp: Long = 0,
    val lastReadDate: Long = 0,
    
    // Reading Analytics
    val totalReadingTimeMinutes: Int = 0,
    val averageWpmStandard: Int = 0,
    val averageWpmPulse: Int = 0,
    val timeSavedWithPulseMinutes: Int = 0,
    val readingTimeMinutes: Int = 0,
    
    // Organization
    val tags: String = "", // Comma-separated
    val isFavorite: Boolean = false,
    val isCurrentlyReading: Boolean = false,
    val isArchived: Boolean = false,
    val readingStatus: String = "UNREAD",
    val folderId: String? = null,
    
    // Cloud & Sync
    val cloudStorageUrl: String? = null,
    val isCloudOnly: Boolean = false,
    
    // Legacy compatibility
    val coverUrl: String? = coverImagePath,
    val progress: Float = progressPercentage,
    val totalWords: Int = wordCount,
    val importDate: Long = System.currentTimeMillis()
)
