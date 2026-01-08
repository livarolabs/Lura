package com.lura.data.mapper

import com.lura.data.db.entity.BookEntity
import com.lura.data.db.entity.FolderEntity
import com.lura.domain.model.Book
import com.lura.domain.model.Folder
import com.lura.domain.model.ReadingStatus

// Book mappers
fun BookEntity.toDomain(): Book {
    return Book(
        id = id,
        title = title,
        author = author,
        filePath = filePath,
        coverImagePath = coverImagePath,
        publisher = publisher,
        publicationDate = publicationDate,
        isbn = isbn,
        description = description,
        genre = genre,
        language = language,
        fileSize = fileSize,
        wordCount = wordCount,
        estimatedReadingTimeMinutes = estimatedReadingTimeMinutes,
        currentPage = currentPage,
        totalPages = totalPages,
        currentChapter = currentChapter,
        progressPercentage = progressPercentage,
        lastReadTimestamp = lastReadTimestamp,
        lastReadDate = lastReadDate,
        totalReadingTimeMinutes = totalReadingTimeMinutes,
        averageWpmStandard = averageWpmStandard,
        averageWpmPulse = averageWpmPulse,
        timeSavedWithPulseMinutes = timeSavedWithPulseMinutes,
        readingTimeMinutes = readingTimeMinutes,
        tags = tags.split(",").filter { it.isNotBlank() },
        isFavorite = isFavorite,
        isCurrentlyReading = isCurrentlyReading,
        isArchived = isArchived,
        readingStatus = try {
            ReadingStatus.valueOf(readingStatus)
        } catch (e: Exception) {
            ReadingStatus.UNREAD
        },
        folderId = folderId,
        cloudStorageUrl = cloudStorageUrl,
        isCloudOnly = isCloudOnly,
        totalWords = totalWords,
        importDate = importDate
    )
}

fun Book.toEntity(): BookEntity {
    return BookEntity(
        id = id,
        title = title,
        author = author,
        filePath = filePath,
        coverImagePath = coverImagePath,
        publisher = publisher,
        publicationDate = publicationDate,
        isbn = isbn,
        description = description,
        genre = genre,
        language = language,
        fileSize = fileSize,
        wordCount = wordCount,
        estimatedReadingTimeMinutes = estimatedReadingTimeMinutes,
        currentPage = currentPage,
        totalPages = totalPages,
        currentChapter = currentChapter,
        progressPercentage = progressPercentage,
        lastReadTimestamp = lastReadTimestamp,
        lastReadDate = lastReadDate,
        totalReadingTimeMinutes = totalReadingTimeMinutes,
        averageWpmStandard = averageWpmStandard,
        averageWpmPulse = averageWpmPulse,
        timeSavedWithPulseMinutes = timeSavedWithPulseMinutes,
        readingTimeMinutes = readingTimeMinutes,
        tags = tags.joinToString(","),
        isFavorite = isFavorite,
        isCurrentlyReading = isCurrentlyReading,
        isArchived = isArchived,
        readingStatus = readingStatus.name,
        folderId = folderId,
        cloudStorageUrl = cloudStorageUrl,
        isCloudOnly = isCloudOnly,
        totalWords = totalWords,
        importDate = importDate
    )
}

// Folder mappers
fun FolderEntity.toDomain(bookCount: Int = 0): Folder {
    return Folder(
        id = id,
        name = name,
        isDefault = isDefault,
        bookCount = bookCount,
        createdAt = createdAt
    )
}

fun Folder.toEntity(): FolderEntity {
    return FolderEntity(
        id = id,
        name = name,
        isDefault = isDefault,
        createdAt = createdAt
    )
}
