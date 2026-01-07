package com.lura.domain.repository

import com.lura.domain.model.Book
import com.lura.data.db.entity.Highlight
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun getLibraryBooks(): Flow<List<Book>>
    suspend fun getBookById(id: String): Book?
    suspend fun importBook(uri: String): Book
    suspend fun importBookFromAssets(assetFileName: String): Book
    suspend fun updateProgress(bookId: String, progress: Float)
    suspend fun deleteBook(bookId: String)
    suspend fun getBookImage(bookId: String, imagePath: String): ByteArray?
    
    // Highlights
    fun getHighlights(bookId: String, chapterIndex: Int): Flow<List<Highlight>>
    suspend fun addHighlight(highlight: Highlight): Long
    suspend fun deleteHighlight(id: Long)
}
