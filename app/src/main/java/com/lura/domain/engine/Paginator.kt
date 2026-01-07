package com.lura.domain.engine

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

data class Page(
    val chapterIndex: Int,
    val pageIndexInChapter: Int,
    val elements: List<ReaderElement>
)

class Paginator {

    /**
     * Splits a list of ReaderElements into Pages that fit within the given constraints.
     */
    fun paginate(
        chapterIndex: Int,
        elements: List<ReaderElement>,
        textMeasurer: TextMeasurer,
        constraints: Constraints,
        bodyStyle: TextStyle,
        titleStyle: TextStyle,
        headingStyle: TextStyle,
        density: Density,
        availableHeight: Int
    ): List<Page> {
        val pages = mutableListOf<Page>()
        var currentPageElements = mutableListOf<ReaderElement>()
        var currentHeight = 0
        
        // Helper to finalize current page
        fun flushPage() {
            if (currentPageElements.isNotEmpty()) {
                pages.add(Page(chapterIndex, pages.size, ArrayList(currentPageElements)))
                currentPageElements.clear()
                currentHeight = 0
            }
        }

        // We assume some padding between elements
        val elementSpacing = with(density) { 16.dp.toPx().toInt() }

        for (element in elements) {
            when (element) {
                is ReaderElement.Text -> {
                    val style = when (element.style) {
                        ReaderTextStyle.Title -> titleStyle
                        ReaderTextStyle.Heading -> headingStyle
                        else -> bodyStyle
                    }

                    // Measure the full text
                    val measured = textMeasurer.measure(
                        text = AnnotatedString(element.content),
                        style = style,
                        constraints = constraints
                    )

                    // Does it fit entirely?
                    if (currentHeight + measured.size.height + elementSpacing <= availableHeight) {
                        // Yes, add it
                        currentPageElements.add(element)
                        currentHeight += measured.size.height + elementSpacing
                    } else {
                        // No. We need to split.
                        // Calculate how much duplicate space we have
                        var remainingText = element.content
                        
                        while (remainingText.isNotEmpty()) {
                            val spaceOnPage = availableHeight - currentHeight
                            
                            // If practically no space, flush first
                            if (spaceOnPage < style.fontSize.value * density.density) { // minimal line height check
                                flushPage()
                                continue
                            }
                            
                            // Measure how many lines fit in 'spaceOnPage'
                            // This is tricky without StaticLayout.getLineForVertical...
                            // Compose TextMeasurer returns a TextLayoutResult which has getLineForVertical
                            
                            val result = textMeasurer.measure(
                                text = AnnotatedString(remainingText),
                                style = style,
                                constraints = constraints
                            )
                            
                            if (result.size.height <= spaceOnPage) {
                                // Fits now (new page or remaining space)
                                currentPageElements.add(ReaderElement.Text(remainingText, element.style))
                                currentHeight += result.size.height + elementSpacing
                                remainingText = ""
                            } else {
                                // Calculate how many lines fit in available space
                                // Use getLineForOffset approach instead
                                var lastFittingLine = -1
                                for (line in 0 until result.lineCount) {
                                    if (result.getLineBottom(line).toInt() <= spaceOnPage) {
                                        lastFittingLine = line
                                    } else {
                                        break
                                    }
                                }
                                
                                if (lastFittingLine < 0) {
                                    // Even the first line doesn't fit
                                    flushPage()
                                    continue
                                }
                                
                                val splitIndex = result.getLineEnd(lastFittingLine)
                                
                                if (splitIndex == 0 || splitIndex >= remainingText.length) {
                                    // Can't split, just add whole thing to new page
                                    flushPage()
                                    currentPageElements.add(ReaderElement.Text(remainingText, element.style))
                                    val remeasured = textMeasurer.measure(
                                        text = AnnotatedString(remainingText),
                                        style = style,
                                        constraints = constraints
                                    )
                                    currentHeight += remeasured.size.height + elementSpacing
                                    remainingText = ""
                                } else {
                                    val textOnPage = remainingText.substring(0, splitIndex)
                                    val textNext = remainingText.substring(splitIndex)
                                    
                                    currentPageElements.add(ReaderElement.Text(textOnPage, element.style))
                                    flushPage()
                                    
                                    remainingText = textNext.trimStart()
                                }
                            }
                        }
                    }
                }
                is ReaderElement.Image -> {
                    // Image logic: Assume generous height or fixed aspect ratio.
                    // For MVP, if it doesn't fit, push to next page.
                    // If it's taller than a full page, scale it to fit.
                    
                    // Estimate height (stub 300dp) or use aspect ratio if we parsed it.
                    // Currently ReaderElement doesn't have dimensions.
                    // We'll treat it as taking 40% of screen or available space.
                    
                    val estimatedHeight = availableHeight / 2 // Rough guess
                    
                    if (currentHeight + estimatedHeight > availableHeight) {
                        flushPage()
                    }
                    
                    currentPageElements.add(element)
                    currentHeight += estimatedHeight + elementSpacing
                }
                is ReaderElement.Table -> {
                    // Estimate table height based on rows
                    val rowHeight = with(density) { 32.dp.toPx().toInt() } // Approximate row height
                    val estimatedTableHeight = element.rows.size * rowHeight
                    
                    if (currentHeight + estimatedTableHeight + elementSpacing > availableHeight && currentPageElements.isNotEmpty()) {
                        flushPage()
                    }
                    
                    currentPageElements.add(element)
                    currentHeight += estimatedTableHeight + elementSpacing
                }
            }
        }
        
        flushPage()
        
        return pages
    }
}
