package com.lura.domain.engine

interface EpubParser {
    suspend fun parseBook(filePath: String): BookContent
}

data class BookContent(
    val title: String,
    val author: String,
    val chapters: List<Chapter>
)

data class Chapter(
    val title: String,
    val elements: List<ReaderElement>
)

sealed class ReaderElement {
    data class Text(
        val content: String, // We will use simple string first, but UI will handle styles. or use AnnotatedString logic in UI mapper? 
        // Ideally domain shouldn't know about AnnotatedString (Compose).
        // Let's keep it generic:
        val style: ReaderTextStyle = ReaderTextStyle.Body
    ) : ReaderElement()

    data class Image(
        val imagePath: String, 
        val caption: String? = null,
        val isFullPage: Boolean = false 
    ) : ReaderElement()

    data class Table(
        val rows: List<TableRow>
    ) : ReaderElement()
}

data class TableRow(
    val cells: List<TableCell>
)

data class TableCell(
    val elements: List<ReaderElement>,
    val isHeader: Boolean = false
)

enum class ReaderTextStyle {
    Title,
    Heading,
    Body,
    Caption,
    Quote
}
