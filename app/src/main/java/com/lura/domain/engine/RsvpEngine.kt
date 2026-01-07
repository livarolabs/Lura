package com.lura.domain.engine

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.math.roundToInt

class RsvpEngine @Inject constructor() {

    fun startPulse(text: String, startWordIndex: Int = 0, wpm: Int = 300): Flow<PulseEvent> = flow {
        val words = text.split(Regex("\\s+")).filter { it.isNotEmpty() }
        val baseDelay = (60000.0 / wpm).toLong()

        for (i in startWordIndex until words.size) {
            val word = words[i]
            val pulseWord = calculatePulseWord(word, baseDelay)
            
            emit(PulseEvent.Word(pulseWord, i))
            delay(pulseWord.delayMs)
        }
        emit(PulseEvent.Finished)
    }

    private fun calculatePulseWord(word: String, baseDelay: Long): PulseWord {
        val cleanWord = word.trim()
        val length = cleanWord.length
        
        // ORP Calculation (Simplified)
        // Usually 35% into the word for optimal recognition, slightly left of center
        val pivotIndex = when {
            length <= 1 -> 0
            length in 2..5 -> 1
            length in 6..9 -> 2
            length in 10..13 -> 3
            else -> 4
        }.coerceAtMost(length - 1)

        // Timing Calculation
        var delay = baseDelay.toDouble()

        // Punctuation
        if (word.contains(",")) delay += 200
        if (word.contains(".") || word.contains("?") || word.contains("!")) delay += 400
        
        // Complexity Scaling
        if (length > 8) delay *= 1.1

        return PulseWord(word, pivotIndex, delay.toLong())
    }
}

sealed class PulseEvent {
    data class Word(val pulseWord: PulseWord, val index: Int) : PulseEvent()
    data object Finished : PulseEvent()
}
