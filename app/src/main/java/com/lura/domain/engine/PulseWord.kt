package com.lura.domain.engine

data class PulseWord(
    val word: String,
    val pivotIndex: Int, // The index of the character to center (ORP)
    val delayMs: Long // How long this word should stay on screen
)

data class PulseWordInfo(
    val text: String,
    val chapterIndex: Int,
    val elementIndex: Int,
    val offsetInElement: Int,
    val globalIndex: Int,
    val sentenceIndex: Int  // Which sentence this word belongs to (global across entire book)
)
