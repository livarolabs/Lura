package com.lura.data.repository

import com.lura.data.db.dao.BookDao
import com.lura.data.db.dao.HighlightDao
import com.lura.data.db.entity.BookEntity
import com.lura.data.db.entity.Highlight
import com.lura.domain.model.Book
import com.lura.domain.repository.LibraryRepository
import com.lura.domain.engine.ReaderElement
import com.lura.data.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

class LibraryRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val highlightDao: HighlightDao,
    @ApplicationContext private val context: android.content.Context,
    private val epubParser: com.lura.domain.engine.EpubParser,
    private val coverExtractor: com.lura.data.engine.CoverExtractor
) : LibraryRepository {

    override fun getLibraryBooks(): Flow<List<Book>> {
        return bookDao.getAllBooks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getBookById(id: String): Book? {
        return bookDao.getBookById(id)?.toDomain()
    }

    override suspend fun importBook(uri: String): Book {
        val uriObj = android.net.Uri.parse(uri)
        val filesDir = context.filesDir
        val booksDir = java.io.File(filesDir, "books")
        if (!booksDir.exists()) booksDir.mkdirs()

        // Generate unique ID
        val bookId = java.util.UUID.randomUUID().toString()
        val destFile = java.io.File(booksDir, "$bookId.epub")

        // Copy content
        context.contentResolver.openInputStream(uriObj)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw Exception("Could not open file stream")

        val content = epubParser.parseBook(destFile.absolutePath)
        
        // Calculate total words from all chapters (Updated for ReaderElement)
        val totalWordCount = content.chapters.sumOf { chapter ->
            chapter.elements
                .filterIsInstance<ReaderElement.Text>()
                .sumOf { it.content.split("\\s+".toRegex()).size }
        }
        
        // Extract cover image
        val coverImagePath = coverExtractor.extractCover(destFile.absolutePath, bookId)
        
        val newBookEntity = BookEntity(
            id = bookId,
            title = content.title.ifEmpty { "Imported Book" },
            author = content.author.ifEmpty { "Unknown Author" },
            filePath = destFile.absolutePath,
            coverImagePath = coverImagePath,
            wordCount = totalWordCount,
            totalWords = totalWordCount,
            progressPercentage = 0f,
            progress = 0f,
            importDate = System.currentTimeMillis()
        )
        
        bookDao.insertBook(newBookEntity)
        return newBookEntity.toDomain()
    }

    override suspend fun importBookFromAssets(assetFileName: String): Book {
        // Ignored assetFileName for now, we scan for any epub
        val assets = context.assets.list("debug_samples") ?: emptyArray()
        val targetFile = assets.firstOrNull { it.endsWith(".epub", ignoreCase = true) }
            ?: throw Exception("No .epub file found in debug_samples folder")

        val filesDir = context.filesDir
        val booksDir = java.io.File(filesDir, "books")
        if (!booksDir.exists()) booksDir.mkdirs()

        val bookId = java.util.UUID.randomUUID().toString()
        val destFile = java.io.File(booksDir, "$bookId.epub")

        context.assets.open("debug_samples/$targetFile").use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val content = epubParser.parseBook(destFile.absolutePath)
        
        val totalWordCount = content.chapters.sumOf { chapter ->
            chapter.elements
                .filterIsInstance<ReaderElement.Text>()
                .sumOf { it.content.split("\\s+".toRegex()).size }
        }
        
        // Extract cover image
        val coverImagePath = coverExtractor.extractCover(destFile.absolutePath, bookId)
        
        val newBookEntity = BookEntity(
            id = bookId,
            title = content.title.ifEmpty { "Debug Book" },
            author = content.author.ifEmpty { "Debug Author" },
            filePath = destFile.absolutePath,
            coverImagePath = coverImagePath,
            wordCount = totalWordCount,
            totalWords = totalWordCount,
            progressPercentage = 0f,
            progress = 0f,
            importDate = System.currentTimeMillis()
        )
        
        bookDao.insertBook(newBookEntity)
        return newBookEntity.toDomain()
    }
    
    override suspend fun updateProgress(bookId: String, progress: Float) {
        bookDao.updateProgress(bookId, progress)
    }

    override suspend fun deleteBook(bookId: String) {
        val book = bookDao.getBookById(bookId)
        if (book != null) {
            bookDao.deleteBook(book)
        }
    }

    /**
     * Extract covers for all books that don't have one yet.
     */
    suspend fun extractMissingCovers() {
        try {
            android.util.Log.d("LibraryRepository", "Starting cover extraction")
            val books = bookDao.getAllBooks().first()
            val booksWithoutCovers = books.filter { it.coverImagePath == null }
            
            android.util.Log.d("LibraryRepository", "Found ${booksWithoutCovers.size} books without covers")
            
            booksWithoutCovers.forEach { bookEntity ->
                try {
                    val coverPath = coverExtractor.extractCover(bookEntity.filePath, bookEntity.id)
                    if (coverPath != null) {
                        val updatedEntity = bookEntity.copy(coverImagePath = coverPath)
                        bookDao.insertBook(updatedEntity)
                        android.util.Log.d("LibraryRepository", "Extracted cover for: ${bookEntity.title}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("LibraryRepository", "Error extracting cover", e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("LibraryRepository", "Error in extractMissingCovers", e)
        }
    }

    override suspend fun getBookImage(bookId: String, imagePath: String): ByteArray? {
        val book = bookDao.getBookById(bookId) ?: return null
        val file = java.io.File(book.filePath)
        if (!file.exists()) return null

        return try {
            java.util.zip.ZipFile(file).use { zipFile ->
                // Basic case-insensitive search (similar to Parser)
                val target = imagePath.replace("\\", "/")
                var entry = zipFile.getEntry(target)
                
                if (entry == null) {
                    val entries = zipFile.entries()
                    while (entries.hasMoreElements()) {
                        val e = entries.nextElement()
                        if (e.name.replace("\\", "/").equals(target, ignoreCase = true) ||
                            e.name.endsWith("/$target", ignoreCase = true)) {
                            entry = e
                            break
                        }
                    }
                }
                
                entry?.let { zipFile.getInputStream(it).readBytes() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getHighlights(bookId: String, chapterIndex: Int): Flow<List<Highlight>> {
        return highlightDao.getHighlights(bookId, chapterIndex)
    }

    override suspend fun addHighlight(highlight: Highlight): Long {
        return highlightDao.insertHighlight(highlight)
    }

    override suspend fun deleteHighlight(id: Long) {
        highlightDao.deleteHighlight(id)
    }
}
