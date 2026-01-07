package com.lura.domain.model

data class Folder(
    val id: String,
    val name: String,
    val isDefault: Boolean = false,
    val bookCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

// Default folder IDs
object DefaultFolders {
    const val ALL_BOOKS = "all_books"
    const val CURRENTLY_READING = "currently_reading"
    const val FINISHED = "finished"
    const val FAVORITES = "favorites"
    
    fun getDefaults(): List<Folder> = listOf(
        Folder(id = ALL_BOOKS, name = "All Books", isDefault = true),
        Folder(id = CURRENTLY_READING, name = "Currently Reading", isDefault = true),
        Folder(id = FINISHED, name = "Finished", isDefault = true),
        Folder(id = FAVORITES, name = "Favorites", isDefault = true)
    )
}
