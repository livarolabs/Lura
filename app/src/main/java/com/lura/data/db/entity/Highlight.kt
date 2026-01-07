package com.lura.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "highlights")
data class Highlight(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: String,
    val chapterIndex: Int,
    val elementIndex: Int, // The paragraph/element index within the chapter
    val startIndex: Int,   // Character start index within the element text
    val endIndex: Int,     // Character end index within the element text
    val color: Int,        // ARGB color int
    val createdAt: Long = System.currentTimeMillis()
)
