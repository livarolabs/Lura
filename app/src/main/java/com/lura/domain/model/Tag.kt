package com.lura.domain.model

import androidx.compose.ui.graphics.Color
import com.lura.ui.theme.LuraIndigo

data class Tag(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val color: Long = 0xFF6366F1, // Default Indigo
    val isSystem: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// System tags that are always available
object SystemTags {
    val TO_READ = Tag(
        id = "system_to_read",
        name = "To Read",
        color = 0xFF6366F1, // Indigo
        isSystem = true
    )
    
    val READING = Tag(
        id = "system_reading",
        name = "Reading",
        color = 0xFF3B82F6, // Blue
        isSystem = true
    )
    
    val FINISHED = Tag(
        id = "system_finished",
        name = "Finished",
        color = 0xFF10B981, // Green
        isSystem = true
    )
    
    val FAVORITES = Tag(
        id = "system_favorites",
        name = "Favorites",
        color = 0xFFF59E0B, // Amber
        isSystem = true
    )
    
    fun getAll() = listOf(TO_READ, READING, FINISHED, FAVORITES)
}
