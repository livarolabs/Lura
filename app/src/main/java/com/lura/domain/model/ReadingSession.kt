package com.lura.domain.model

data class ReadingSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val bookId: String,
    val startTime: Long,
    val endTime: Long,
    val wordsRead: Int,
    val mode: ReadingMode,
    val averageWpm: Int = 0
) {
    val durationMinutes: Int
        get() = ((endTime - startTime) / 60000).toInt()
}
