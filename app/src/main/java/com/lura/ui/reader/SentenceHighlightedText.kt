package com.lura.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.lura.domain.engine.PulseWordInfo
import com.lura.ui.theme.LuraIndigo

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SentenceHighlightedText(
    text: AnnotatedString,
    chapterIndex: Int,
    elementIndex: Int,
    currentSentenceIndex: Int,
    allPulseWords: List<PulseWordInfo>,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier
) {
    // Convert AnnotatedString to plain text and split into words
    val plainText = text.text
    val words = remember(plainText) { plainText.split(Regex("\\s+")).filter { it.isNotEmpty() } }
    
    // Find which words belong to the current sentence in this element
    val wordsInCurrentSentence = remember(currentSentenceIndex, chapterIndex, elementIndex, allPulseWords) {
        allPulseWords
            .filter { it.chapterIndex == chapterIndex && 
                     it.elementIndex == elementIndex && 
                     it.sentenceIndex == currentSentenceIndex }
            .map { it.offsetInElement }
            .toSet()
    }
    
    // Render text with highlighted words
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start
    ) {
        words.forEachIndexed { index, word ->
            val isInCurrentSentence = wordsInCurrentSentence.contains(index)
            
            BasicText(
                text = "$word ",
                style = style,
                modifier = Modifier
                    .background(
                        if (isInCurrentSentence) {
                            LuraIndigo.copy(alpha = 0.15f)  // Soft Indigo glow for current sentence
                        } else {
                            Color.Transparent
                        },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                    )
                    .padding(horizontal = 1.dp)
            )
        }
    }
}
