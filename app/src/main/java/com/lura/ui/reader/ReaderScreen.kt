package com.lura.ui.reader

import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.zIndex
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.FormatIndentIncrease
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.SwapCalls
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.hilt.navigation.compose.hiltViewModel
import com.lura.ui.pulse.PulseView
import com.lura.ui.theme.ReaderTypography
import com.lura.ui.theme.LuraObsidian
import com.lura.ui.theme.LuraGhostWhite
import com.lura.ui.theme.LuraIndigo
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import android.view.WindowManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

// Internal representation for paginated content to preserve styles and track source position
sealed class InternalElement {
    data class Text(
        val content: androidx.compose.ui.text.AnnotatedString, 
        val style: com.lura.domain.engine.ReaderTextStyle,
        val originalElementIndex: Int,
        val startLine: Int = 0
    ) : InternalElement()
    
    data class Image(
        val element: com.lura.domain.engine.ReaderElement.Image,
        val originalElementIndex: Int
    ) : InternalElement()
    
    data class Table(
        val element: com.lura.domain.engine.ReaderElement.Table,
        val originalElementIndex: Int
    ) : InternalElement()
}

data class PageContent(
    val elements: List<Pair<Int, InternalElement>>,
    val isTitle: Boolean = false
)

private const val READER_ELEMENT_SPACING_DP = 16

fun getReaderTextStyle(
    style: com.lura.domain.engine.ReaderTextStyle,
    fontSize: Int,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    lineHeightMultiplier: Float = 1.6f
): TextStyle {
    val base = when (style) {
        com.lura.domain.engine.ReaderTextStyle.Title -> ReaderTypography.headlineSmall
        com.lura.domain.engine.ReaderTextStyle.Heading -> ReaderTypography.titleMedium
        com.lura.domain.engine.ReaderTextStyle.Caption -> ReaderTypography.bodySmall // Fallback
        com.lura.domain.engine.ReaderTextStyle.Quote -> ReaderTypography.bodyLarge.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        else -> ReaderTypography.bodyLarge
    }
    
    return base.copy(
        fontSize = if (style == com.lura.domain.engine.ReaderTextStyle.Title) (fontSize * 1.5).sp 
                  else if (style == com.lura.domain.engine.ReaderTextStyle.Heading) (fontSize * 1.2).sp
                  else fontSize.sp,
        fontFamily = fontFamily,
        lineHeight = lineHeightMultiplier.em,
        letterSpacing = 0.5.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(
    bookId: String?,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Keep screen on while reading
    val screenView = LocalView.current
    DisposableEffect(Unit) {
        val window = screenView.context.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Handle Navigation Events (Hardware Keys)
    LaunchedEffect(viewModel) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                ReaderNavigationEvent.ScrollDown -> {
                    listState.animateScrollBy(1000f)
                }
                ReaderNavigationEvent.ScrollUp -> {
                    listState.animateScrollBy(-1000f)
                }
                is ReaderNavigationEvent.ScrollToChapter -> {
                    val state = uiState as? ReaderUiState.Ready
                    if (state != null) {
                         val content = state.bookContent
                         var index = 1 // Title page
                         for (i in 0 until event.chapterIndex) {
                             val ch = content.chapters[i]
                             if (ch.title.isNotBlank() && ch.title != content.title) index++
                             index += ch.elements.size
                         }
                         
                         // Determine target
                         val targetCh = content.chapters[event.chapterIndex]
                         val hasTitle = targetCh.title.isNotBlank() && targetCh.title != content.title
                         
                         // If elementIndex is 0 (Chapter Start), scroll to Title if exists, else first element
                         val finalIndex = if (hasTitle) {
                             if (event.elementIndex == 0) index else index + 1 + event.elementIndex
                         } else {
                             index + event.elementIndex
                         }
                         
                         listState.scrollToItem(finalIndex)
                    }
                }
                is ReaderNavigationEvent.GoToPage -> {
                    // Handled by pagerState in the UI directly
                }
                ReaderNavigationEvent.NextPage -> {
                    // Handled by pagerState in the UI directly
                }
                ReaderNavigationEvent.PreviousPage -> {
                    // Handled by pagerState in the UI directly
                }
            }
        }
    }

    // Immersive Mode Logic
    val view = androidx.compose.ui.platform.LocalView.current
    if (uiState is ReaderUiState.Ready) {
        val show = (uiState as ReaderUiState.Ready).showControls
        LaunchedEffect(show) {
            val window = (view.context as? android.app.Activity)?.window
            if (window != null) {
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
                insetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                if (show) {
                    insetsController.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                } else {
                    insetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                }
            }
        }
    }

    when (val state = uiState) {
        is ReaderUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ReaderUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is ReaderUiState.Ready -> {
            val themeSurface = Color(state.theme.surfaceColor)
            val themeOnSurface = Color(state.theme.onSurfaceColor)
            
            var pages by remember { mutableStateOf<List<PageContent>>(emptyList()) }
            var isPaginating by remember { mutableStateOf(false) }

            // Add title page as first item
            val totalPages = 1 + pages.size
            
            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { totalPages }
            )

            // Progress Display Mode: 0 = Page Count, 1 = Percentage, 2 = Chapter
            var progressDisplayMode by remember { mutableIntStateOf(0) }

            val currentFontFamily = when (state.fontFamily) {
                ReaderFontFamily.SERIF -> androidx.compose.ui.text.font.FontFamily.Serif
                ReaderFontFamily.SANS_SERIF -> androidx.compose.ui.text.font.FontFamily.SansSerif
                ReaderFontFamily.MONOSPACE -> androidx.compose.ui.text.font.FontFamily.Monospace
            }

            // Chapter progress within the current chapter
            val chapterProgress = remember(pages, pagerState.currentPage) {
                androidx.compose.runtime.derivedStateOf {
                    val currentPageIdx = pagerState.currentPage - 1
                    if (currentPageIdx >= 0 && currentPageIdx < pages.size) {
                        val currentPage = pages[currentPageIdx]
                        val currentChapterIdx = currentPage.elements.firstOrNull()?.first ?: -1
                        if (currentChapterIdx != -1) {
                            val chapterPages = pages.filter { it.elements.firstOrNull()?.first == currentChapterIdx }
                            val totalInChapter = chapterPages.size
                            val posInChapter = chapterPages.indexOf(currentPage) + 1
                            "Page $posInChapter of $totalInChapter"
                        } else ""
                    } else ""
                }
            }

            // Restore initial progress once pagination is complete
            LaunchedEffect(isPaginating, state.initialProgress) {
                if (!isPaginating && state.initialProgress > 0f) {
                    val targetPage = (state.initialProgress * totalPages).toInt().coerceIn(0, totalPages - 1)
                    if (pagerState.currentPage != targetPage) {
                        pagerState.scrollToPage(targetPage)
                    }
                }
            }

            // Save progress when page changes
            LaunchedEffect(pagerState.currentPage, totalPages) {
                if (!isPaginating && totalPages > 1) {
                    val progress = pagerState.currentPage.toFloat() / (totalPages - 1)
                    viewModel.saveProgress(progress)
                }
            }

            Scaffold(
                containerColor = themeSurface,
            ) { padding ->
                // Theme Colors
                val backgroundColor = Color(state.theme.backgroundColor)
                val onBackgroundColor = Color(state.theme.onBackgroundColor)
                val surfaceColor = Color(state.theme.surfaceColor)
                
                Box(modifier = Modifier.fillMaxSize()) {
                    // 1. Content Layer (Always full screen, ignores Scaffold padding to prevent layout shifts)
                    

                    // Global Page Number Indicator (Bottom Center) - Interactive Toggle
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .zIndex(1f) // Ensure it's above content
                    ) {
                        val progressText = when (progressDisplayMode) {
                            1 -> {
                                val percent = if (totalPages > 0) ((pagerState.currentPage + 1).toFloat() / totalPages * 100).toInt() else 0
                                "$percent%"
                            }
                            2 -> {
                                if (chapterProgress.value.isNotEmpty()) "Ch: ${chapterProgress.value.replace("Page ", "")}" else "Page ${pagerState.currentPage + 1} of $totalPages"
                            }
                            else -> "Page ${pagerState.currentPage + 1} of $totalPages"
                        }
                        
                        Text(
                            text = progressText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = onBackgroundColor.copy(alpha = 0.5f),
                                fontFamily = currentFontFamily
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor) // Apply Reader Background
                            .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { offset ->
                                    val width = size.width
                                    val height = size.height
                                    if (offset.y > height * 0.85f) {
                                        progressDisplayMode = (progressDisplayMode + 1) % 3
                                    } else if (offset.x < width * 0.25f) {
                                        viewModel.readPreviousPage() 
                                    } else if (offset.x > width * 0.75f) {
                                        viewModel.readNextPage()
                                    } else {
                                        viewModel.toggleControls()
                                    }
                                }
                            )
                        }
                ) {
                    val density = LocalDensity.current
                    
                    // Precise available dimensions in PX
                    val availableWidth = constraints.maxWidth
                    val availableHeight = constraints.maxHeight
                    val horizontalPaddingDp = state.horizontalPadding
                    val verticalPaddingDp = state.verticalPadding
                    val horizontalPaddingPx = with(density) { horizontalPaddingDp.dp.toPx().toInt() * 2 }
                    val verticalPaddingPx = with(density) { verticalPaddingDp.dp.toPx().toInt() * 2 }
                    val topPaddingPx = with(density) { 48.dp.toPx().toInt() }
                    val pageIndicatorHeight = with(density) { 40.dp.toPx().toInt() }
                    val extraBottomBuffer = with(density) { 16.dp.toPx().toInt() }
                    val contentHeight = availableHeight - topPaddingPx - verticalPaddingPx - pageIndicatorHeight - extraBottomBuffer
                    
                    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
                    val availableContentWidthPx = availableWidth - horizontalPaddingPx
                    val availableContentHeightPx = contentHeight.toFloat()
                    
                    LaunchedEffect(state.bookContent, state.fontSize, availableContentHeightPx, availableContentWidthPx, currentFontFamily) {
                        if (availableContentHeightPx <= 0 || availableContentWidthPx <= 0) return@LaunchedEffect
                        
                        // ANCHOR CAPTURE: Save current position before re-paginating
                        val currentPagesSnapshot = pages
                        val currentPagerPage = pagerState.currentPage
                        var anchor: Triple<Int, Int, Int>? = null
                        
                        if (currentPagesSnapshot.isNotEmpty() && currentPagerPage > 0) {
                            val pageIdx = currentPagerPage - 1
                            if (pageIdx < currentPagesSnapshot.size) {
                                val page = currentPagesSnapshot[pageIdx]
                                page.elements.firstOrNull()?.let { (chIdx, el) ->
                                    anchor = when (el) {
                                        is InternalElement.Text -> Triple(chIdx, el.originalElementIndex, el.startLine)
                                        is InternalElement.Image -> Triple(chIdx, el.originalElementIndex, 0)
                                        is InternalElement.Table -> Triple(chIdx, el.originalElementIndex, 0)
                                    }
                                }
                            }
                        }

                        isPaginating = true
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                            val resultNodes = kotlinx.coroutines.coroutineScope {
                                state.bookContent.chapters.mapIndexed { chapterIndex, chapter ->
                                    async {
                                        val chapterPages = mutableListOf<PageContent>()
                                        var currentPageElements = mutableListOf<Pair<Int, InternalElement>>()
                                        var currentHeightPx = 0f

                                        fun flushPage() {
                                            if (currentPageElements.isNotEmpty()) {
                                                chapterPages.add(PageContent(ArrayList(currentPageElements)))
                                                currentPageElements.clear()
                                                currentHeightPx = 0f
                                            }
                                        }

                                        chapter.elements.forEachIndexed { elementIndex, element ->
                                            when (element) {
                                                is com.lura.domain.engine.ReaderElement.Text -> {
                                                    val style = getReaderTextStyle(element.style, state.fontSize, currentFontFamily, state.lineHeightMultiplier)
                                                    val spacingPx = with(density) { READER_ELEMENT_SPACING_DP.dp.toPx() }

                                                    val fullAnnotatedString = if (element.style == com.lura.domain.engine.ReaderTextStyle.Body) 
                                                        com.lura.ui.reader.utils.htmlToAnnotatedString(element.content)
                                                    else 
                                                        androidx.compose.ui.text.AnnotatedString(element.content)

                                                    val measureResult: androidx.compose.ui.text.TextLayoutResult = textMeasurer.measure(
                                                        text = fullAnnotatedString,
                                                        style = style,
                                                        constraints = androidx.compose.ui.unit.Constraints(maxWidth = availableContentWidthPx)
                                                    )

                                                    var currentLine = 0
                                                    while (currentLine < measureResult.lineCount) {
                                                        val spaceLeft = availableContentHeightPx - currentHeightPx - spacingPx
                                                        
                                                        if (spaceLeft < 10f && currentPageElements.isNotEmpty()) {
                                                            flushPage()
                                                            continue
                                                        }

                                                        val baselineTop = if (currentLine > 0) measureResult.getLineTop(currentLine) else 0f
                                                        val targetBottom = baselineTop + spaceLeft
                                                        val lastPossibleLine = measureResult.getLineForVerticalPosition(targetBottom).coerceAtMost(measureResult.lineCount - 1)
                                                        
                                                        var lineToTake = if (measureResult.getLineBottom(lastPossibleLine) - baselineTop <= spaceLeft) {
                                                            lastPossibleLine
                                                        } else {
                                                            (lastPossibleLine - 1).coerceAtLeast(currentLine)
                                                        }

                                                        if (lineToTake < currentLine && currentPageElements.isEmpty()) {
                                                            lineToTake = currentLine
                                                        }

                                                        if (lineToTake >= currentLine) {
                                                            val tookText = fullAnnotatedString.subSequence(
                                                                measureResult.getLineStart(currentLine),
                                                                measureResult.getLineEnd(lineToTake)
                                                            )
                                                            
                                                            if (tookText.isNotEmpty()) {
                                                                currentPageElements.add(chapterIndex to InternalElement.Text(tookText, element.style, elementIndex, currentLine))
                                                                currentHeightPx += (measureResult.getLineBottom(lineToTake) - baselineTop) + spacingPx
                                                            }
                                                            
                                                            currentLine = lineToTake + 1
                                                            if (currentLine < measureResult.lineCount) {
                                                                flushPage()
                                                            }
                                                        } else {
                                                            flushPage()
                                                        }
                                                    }
                                                }
                                                is com.lura.domain.engine.ReaderElement.Image -> {
                                                    if (element.isFullPage) {
                                                        if (currentPageElements.isNotEmpty()) flushPage()
                                                        currentPageElements.add(chapterIndex to InternalElement.Image(element, elementIndex))
                                                        flushPage()
                                                    } else {
                                                        val estimatedImageHeightPx = availableContentHeightPx * 0.4f
                                                        if (currentHeightPx + estimatedImageHeightPx > availableContentHeightPx && currentPageElements.isNotEmpty()) {
                                                            flushPage()
                                                        }
                                                        currentPageElements.add(chapterIndex to InternalElement.Image(element, elementIndex))
                                                        currentHeightPx += estimatedImageHeightPx + with(density) { 16.dp.toPx() }
                                                    }
                                                }
                                                is com.lura.domain.engine.ReaderElement.Table -> {
                                                    var remainingRows = element.rows
                                                    val rowHeightPx = with(density) { (state.fontSize * 2).sp.toPx() }

                                                    while (remainingRows.isNotEmpty()) {
                                                        val spaceLeft = availableContentHeightPx - currentHeightPx
                                                        val rowsToTake = mutableListOf<com.lura.domain.engine.TableRow>()
                                                        var tableHeight = 0f
                                                        
                                                        for (row in remainingRows) {
                                                            val estRowHeight = rowHeightPx
                                                            if (currentHeightPx + tableHeight + estRowHeight <= availableContentHeightPx) {
                                                                rowsToTake.add(row)
                                                                tableHeight += estRowHeight
                                                            } else {
                                                                break
                                                            }
                                                        }
                                                        
                                                        if (rowsToTake.isNotEmpty()) {
                                                            currentPageElements.add(chapterIndex to InternalElement.Table(com.lura.domain.engine.ReaderElement.Table(rowsToTake), elementIndex))
                                                            currentHeightPx += tableHeight + with(density) { 16.dp.toPx() }
                                                            remainingRows = remainingRows.drop(rowsToTake.size)
                                                            if (remainingRows.isNotEmpty()) flushPage()
                                                        } else {
                                                            if (currentPageElements.isNotEmpty()) flushPage()
                                                            else {
                                                                currentPageElements.add(chapterIndex to InternalElement.Table(com.lura.domain.engine.ReaderElement.Table(listOf(remainingRows[0])), elementIndex))
                                                                remainingRows = remainingRows.drop(1)
                                                                flushPage()
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        flushPage()
                                        chapterPages
                                    }
                                }
                            }
                            
                            val fullResult = resultNodes.awaitAll().flatten()
                            
                            // ANCHOR RESTORATION: Search for and scroll to the anchor in the new pagination
                            if (anchor != null) {
                                val anchorVal = anchor!!
                                val targetPageIdx = fullResult.indexOfFirst { page ->
                                    page.elements.any { (ch, el) ->
                                        when (el) {
                                            is InternalElement.Text -> ch == anchorVal.first && el.originalElementIndex == anchorVal.second && el.startLine >= anchorVal.third
                                            is InternalElement.Image -> ch == anchorVal.first && el.originalElementIndex == anchorVal.second
                                            is InternalElement.Table -> ch == anchorVal.first && el.originalElementIndex == anchorVal.second
                                        }
                                    }
                                }
                                
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    pages = fullResult
                                    if (targetPageIdx != -1) {
                                        pagerState.scrollToPage(targetPageIdx + 1)
                                    }
                                    isPaginating = false
                                }
                            } else {
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    pages = fullResult
                                    isPaginating = false
                                }
                            }
                        }
                    }

                        // Page content rendering
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            beyondViewportPageCount = 1
                        ) { pageIndex ->
                            if (pageIndex == 0) {
                                // Title Page
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            start = horizontalPaddingDp.dp,
                                            end = horizontalPaddingDp.dp,
                                            top = 48.dp + verticalPaddingDp.dp, 
                                            bottom = verticalPaddingDp.dp + 40.dp + 16.dp
                                        )
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Spacer(Modifier.height(48.dp))
                                        Text(
                                            text = state.bookContent.title,
                                            style = getReaderTextStyle(com.lura.domain.engine.ReaderTextStyle.Title, state.fontSize, currentFontFamily, state.lineHeightMultiplier).copy(
                                                color = onBackgroundColor
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(Modifier.height(16.dp))
                                        Text(
                                            text = state.bookContent.author,
                                            style = getReaderTextStyle(com.lura.domain.engine.ReaderTextStyle.Heading, state.fontSize, currentFontFamily, state.lineHeightMultiplier).copy(
                                                color = onBackgroundColor.copy(alpha = 0.7f)
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                // Content page - render all elements in this page
                                val pageIdx = pageIndex - 1
                                if (pageIdx < pages.size) {
                                    val page = pages[pageIdx]
                                    val isSingleFullPageImage = page.elements.size == 1 && 
                                        (page.elements[0].second as? InternalElement.Image)?.element?.isFullPage == true

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(
                                                start = if (isSingleFullPageImage) 0.dp else horizontalPaddingDp.dp,
                                                end = if (isSingleFullPageImage) 0.dp else horizontalPaddingDp.dp,
                                                top = if (isSingleFullPageImage) 0.dp else (48.dp + verticalPaddingDp.dp), 
                                                bottom = if (isSingleFullPageImage) 0.dp else (verticalPaddingDp.dp + 40.dp + 16.dp)
                                            )
                                    ) {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                                page.elements.forEach { (chapterIndex, internalElement) ->
                                                    when (internalElement) {
                                                    is InternalElement.Text -> {
                                                        SelectionContainer {
                                                            // Sentence Highlighting: Apply background to current sentence during Pulse
                                                            if (state.isPulseMode && state.currentSentenceIndex != null) {
                                                                // Render with sentence highlighting
                                                                SentenceHighlightedText(
                                                                    text = internalElement.content,
                                                                    chapterIndex = chapterIndex,
                                                                    elementIndex = internalElement.originalElementIndex,
                                                                    currentSentenceIndex = state.currentSentenceIndex,
                                                                    allPulseWords = viewModel.allPulseWords,
                                                                    style = getReaderTextStyle(internalElement.style, state.fontSize, currentFontFamily, state.lineHeightMultiplier).copy(
                                                                        color = if(internalElement.style == com.lura.domain.engine.ReaderTextStyle.Caption) onBackgroundColor.copy(alpha=0.6f) else onBackgroundColor
                                                                    ),
                                                                    modifier = Modifier.padding(bottom = READER_ELEMENT_SPACING_DP.dp)
                                                                )
                                                            } else {
                                                                // Normal rendering without highlighting
                                                                Text(
                                                                    text = internalElement.content, 
                                                                    style = getReaderTextStyle(internalElement.style, state.fontSize, currentFontFamily, state.lineHeightMultiplier).copy(
                                                                        color = if(internalElement.style == com.lura.domain.engine.ReaderTextStyle.Caption) onBackgroundColor.copy(alpha=0.6f) else onBackgroundColor
                                                                    ),
                                                                    textAlign = TextAlign.Start,
                                                                    modifier = Modifier.padding(bottom = READER_ELEMENT_SPACING_DP.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                    is InternalElement.Image -> {
                                                        val element = internalElement.element
                                                        val imageBitmap = produceState<androidx.compose.ui.graphics.ImageBitmap?>(initialValue = null, element.imagePath) {
                                                            val bytes = viewModel.loadImage(element.imagePath)
                                                            if (bytes != null) {
                                                                val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                                                value = bitmap?.asImageBitmap()
                                                            }
                                                        }

                                                        if (imageBitmap.value != null) {
                                                            if (element.isFullPage) {
                                                                Box(
                                                                    modifier = Modifier.fillMaxSize(),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    androidx.compose.foundation.Image(
                                                                        bitmap = imageBitmap.value!!,
                                                                        contentDescription = element.caption,
                                                                        modifier = Modifier.fillMaxSize(),
                                                                        contentScale = ContentScale.Fit
                                                                    )
                                                                }
                                                            } else {
                                                                Column(
                                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                                ) {
                                                                    androidx.compose.foundation.Image(
                                                                        bitmap = imageBitmap.value!!,
                                                                        contentDescription = element.caption,
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .wrapContentHeight(),
                                                                        contentScale = ContentScale.Fit
                                                                    )
                                                                    
                                                                    if (element.caption != null) {
                                                                        Text(
                                                                            text = element.caption,
                                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                                fontFamily = currentFontFamily,
                                                                                color = onBackgroundColor.copy(alpha = 0.6f)
                                                                            ),
                                                                            modifier = Modifier.padding(top = 8.dp),
                                                                            textAlign = TextAlign.Center
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(if (element.isFullPage) 300.dp else 150.dp)
                                                                    .background(surfaceColor),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                CircularProgressIndicator(color = onBackgroundColor)
                                                            }
                                                        }
                                                    }
                                                    is InternalElement.Table -> {
                                                        val element = internalElement.element
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 12.dp)
                                                                .border(1.dp, onBackgroundColor.copy(alpha = 0.1f))
                                                        ) {
                                                            element.rows.forEachIndexed { rowIndex, row ->
                                                                Row(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .background(
                                                                            if (row.cells.any { it.isHeader }) onBackgroundColor.copy(alpha = 0.05f) 
                                                                            else Color.Transparent
                                                                        )
                                                                ) {
                                                                    row.cells.forEach { cell ->
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .weight(1f)
                                                                                .padding(8.dp)
                                                                        ) {
                                                                            Column {
                                                                                cell.elements.forEach { cellEl ->
                                                                                    if (cellEl is com.lura.domain.engine.ReaderElement.Text) {
                                                                                        Text(
                                                                                            text = cellEl.content,
                                                                                            style = (if (cell.isHeader) ReaderTypography.titleSmall else ReaderTypography.bodySmall).copy(
                                                                                                fontFamily = currentFontFamily,
                                                                                                color = onBackgroundColor,
                                                                                                fontWeight = if (cell.isHeader) androidx.compose.ui.text.font.FontWeight.Bold else null
                                                                                            )
                                                                                        )
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                if (rowIndex < element.rows.size - 1) {
                                                                    HorizontalDivider(color = onBackgroundColor.copy(alpha = 0.1f))
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                     // Visual Comfort Overlays
                    // 1. Blue Light Filter (Amber)
                    if (state.blueLightFilterIntensity > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFFFB74D).copy(alpha = state.blueLightFilterIntensity * 0.4f)) // Max alpha 0.4
                                .pointerInput(Unit) {} // Pass through touches (empty pointerInput consumes nothing but Modifier.clickable would)
                                // Actually, Box doesn't block touches unless clickable. 
                                // But to be safe from 'detectTapGestures' on parent consuming? 
                                // Parent 'pointerInput' wraps THIS box too? No, parent wraps this BoxWithConstraints content.
                                // These boxes are children of BoxWithConstraints.
                                // The tap detector is on the Parent BoxWithConstraints. It receives touches if children don't consume.
                                // These boxes are effectively transparent to touches.
                        )
                    }

                    // 2. Brightness Dimmer (Black)
                    if (state.brightnessDimmer > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = state.brightnessDimmer))
                        )
                    }

                    // Pulse Overlay - GPS Highlight Mode
                    if (state.isPulseMode && state.activePulseWord != null) {
                        // Semi-transparent dark overlay (75% opacity) over the page
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.75f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { viewModel.togglePulse() }
                        )
                        
                        // Focus Lens and Controls (on top of dimmed page)
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            PulseView(pulseWord = state.activePulseWord)
                            
                            // 0. Chapter Progress & Breadcrumb (Bottom)
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Breadcrumb
                                val wordInfo = viewModel.allPulseWords.getOrNull(state.currentWordIndex)
                                val chapterTitle = wordInfo?.let { 
                                    state.bookContent.chapters.getOrNull(it.chapterIndex)?.title 
                                } ?: ""
                                
                                Text(
                                    text = chapterTitle.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 2.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = LuraGhostWhite.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(horizontal = 32.dp),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )

                                // Progress Bar
                                val chapterWords = viewModel.allPulseWords.filter { info -> 
                                    wordInfo?.let { it.chapterIndex == info.chapterIndex } ?: false 
                                }
                                val progressInChapter = if (chapterWords.isNotEmpty()) {
                                    val firstIdx = chapterWords.first().globalIndex
                                    (state.currentWordIndex - firstIdx).toFloat() / chapterWords.size.toFloat()
                                } else 0f

                                LinearProgressIndicator(
                                    progress = { progressInChapter },
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(2.dp),
                                    color = LuraIndigo.copy(alpha = 0.6f),
                                    trackColor = LuraGhostWhite.copy(alpha = 0.05f),
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            }
                            
                            // 1. Close Button (Top Right)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .statusBarsPadding()
                                    .padding(16.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.togglePulse() },
                                    modifier = Modifier.background(LuraGhostWhite.copy(alpha = 0.05f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Exit Pulse",
                                        tint = LuraGhostWhite.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // 2. WPM Controls (Bottom Center)
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 64.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = LuraGhostWhite.copy(alpha = 0.05f),
                                border = BorderStroke(1.dp, LuraGhostWhite.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.updatePulseWpm(state.pulseWpm - 50) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Slow Down",
                                            tint = LuraGhostWhite.copy(alpha = 0.6f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Text(
                                        text = "${state.pulseWpm} WPM",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = LuraGhostWhite
                                    )

                                    IconButton(
                                        onClick = { viewModel.updatePulseWpm(state.pulseWpm + 50) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Speed Up",
                                            tint = LuraGhostWhite.copy(alpha = 0.6f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } // End BoxWithConstraints

                // 2. Control Overlays (Top Floating Actions & Title)
                // Back Button (Top Left)
                AnimatedVisibility(
                    visible = state.showControls,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(16.dp),
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
                ) {
                    Surface(
                        shape = CircleShape,
                        color = LuraObsidian,
                        contentColor = LuraGhostWhite,
                        tonalElevation = 6.dp,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, LuraGhostWhite.copy(alpha = 0.15f)),
                        modifier = Modifier.size(48.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LuraGhostWhite)
                        }
                    }
                }

                // Book Title (Top Center - Minimalist)
                AnimatedVisibility(
                    visible = state.showControls,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 12.dp) // Adjusted slightly as statusBarsPadding provides base offset
                        .padding(horizontal = 64.dp), 
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
                ) {
                    Text(
                        text = state.bookContent.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.4f),
                                offset = androidx.compose.ui.geometry.Offset(0f, 2f),
                                blurRadius = 6f
                            )
                        ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        color = themeOnSurface, // Keep dynamic to background for best readability
                        textAlign = TextAlign.Center
                    )
                }

                // Search Button (Top Right)
                AnimatedVisibility(
                    visible = state.showControls,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp),
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
                ) {
                    Surface(
                        shape = CircleShape,
                        color = LuraObsidian,
                        contentColor = LuraGhostWhite,
                        tonalElevation = 6.dp,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, LuraGhostWhite.copy(alpha = 0.15f)),
                        modifier = Modifier.size(48.dp)
                    ) {
                        IconButton(onClick = { viewModel.toggleSearch() }) {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Default.Search, contentDescription = "Search", tint = LuraGhostWhite)
                        }
                    }
                }

                AnimatedVisibility(
                    visible = state.showControls,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 60.dp)
                        .padding(horizontal = 24.dp),
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                ) {
                    Surface(
                        shape = CircleShape,
                        color = LuraObsidian,
                        contentColor = LuraGhostWhite,
                        tonalElevation = 8.dp,
                        shadowElevation = 12.dp,
                        modifier = Modifier.wrapContentSize(),
                        border = BorderStroke(1.dp, LuraGhostWhite.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(28.dp)
                        ) {
                            var showToc by remember { mutableStateOf(false) }
                            
                            IconButton(onClick = { showToc = true }) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.List, 
                                    contentDescription = "Table of Contents", 
                                    tint = LuraGhostWhite
                                )
                            }

                            IconButton(onClick = { 
                                // Find the first word on the current visible page
                                val pageIdx = pagerState.currentPage - 1
                                if (pageIdx >= 0 && pageIdx < pages.size) {
                                    val firstElement = pages[pageIdx].elements.firstOrNull()
                                    if (firstElement != null) {
                                        val (chIdx, element) = firstElement
                                        val elIdx = when (element) {
                                            is InternalElement.Text -> element.originalElementIndex
                                            is InternalElement.Image -> element.originalElementIndex
                                            is InternalElement.Table -> element.originalElementIndex
                                        }
                                        // Start Pulse from the first word of this chapter/element
                                        viewModel.startPulseFromPage(chIdx, elIdx)
                                    } else {
                                        viewModel.togglePulse()
                                    }
                                } else {
                                    viewModel.togglePulse()
                                }
                            }) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.PlayArrow, 
                                    contentDescription = if (state.isPulseMode) "Stop Pulse" else "Start Pulse", 
                                    tint = if (state.isPulseMode) LuraIndigo else LuraGhostWhite
                                )
                            }

                            var showSettings by remember { mutableStateOf(false) }
                            IconButton(onClick = { showSettings = true }) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Settings, 
                                    contentDescription = "Settings", 
                                    tint = LuraGhostWhite
                                )
                            }
                            
                            // Sheets (Inside Row but detached UI-wise)
                            // 4. Elegant Table of Contents
                            if (showToc) {
                                ModalBottomSheet(
                                    onDismissRequest = { showToc = false },
                                    dragHandle = null,
                                    containerColor = LuraObsidian,
                                    tonalElevation = 0.dp
                                ) {
                                    val currentChapterIdx = remember(pages, pagerState.currentPage) {
                                        val currentPageIdx = pagerState.currentPage - 1
                                        if (currentPageIdx >= 0 && currentPageIdx < pages.size) {
                                            pages[currentPageIdx].elements.firstOrNull()?.first ?: -1
                                        } else -1
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 32.dp)
                                    ) {
                                        // Header (Matches Appearance Design)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 24.dp, vertical = 20.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Contents",
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.5.sp
                                                ),
                                                color = LuraGhostWhite
                                            )
                                            IconButton(onClick = { viewModel.toggleSearch(); showToc = false }) {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = "Search",
                                                    tint = LuraGhostWhite.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }

                                        LazyColumn(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(bottom = 16.dp)
                                        ) {
                                            items(state.bookContent.chapters.mapIndexed { i, c -> i to c }.filter { it.second.title.isNotBlank() }) { (index, chapter) ->
                                                val isSelected = index == currentChapterIdx
                                                
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            viewModel.jumpToChapter(index)
                                                            showToc = false
                                                        }
                                                        .background(if (isSelected) LuraIndigo.copy(alpha = 0.1f) else Color.Transparent)
                                                        .padding(horizontal = 24.dp, vertical = 16.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = chapter.title,
                                                        style = MaterialTheme.typography.bodyLarge.copy(
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                        ),
                                                        color = if (isSelected) LuraGhostWhite else LuraGhostWhite.copy(alpha = 0.7f),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    
                                                    if (isSelected) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .background(LuraIndigo, CircleShape)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 5. Elegant Search Modal
                            if (state.isSearching) {
                                ModalBottomSheet(
                                    onDismissRequest = { viewModel.clearSearch() },
                                    dragHandle = null,
                                    containerColor = LuraObsidian,
                                    tonalElevation = 0.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 32.dp)
                                    ) {
                                        // Header
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 24.dp, vertical = 20.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Search",
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.5.sp
                                                ),
                                                color = LuraGhostWhite
                                            )
                                            IconButton(onClick = { viewModel.clearSearch() }) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Close",
                                                    tint = LuraGhostWhite.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }

                                        // Search Input
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = state.searchQuery,
                                                onValueChange = { viewModel.performSearch(it) },
                                                placeholder = { 
                                                    Text(
                                                        "Find in book...", 
                                                        color = LuraGhostWhite.copy(alpha = 0.3f)
                                                    ) 
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = LuraGhostWhite,
                                                    unfocusedTextColor = LuraGhostWhite,
                                                    focusedBorderColor = LuraIndigo,
                                                    unfocusedBorderColor = LuraGhostWhite.copy(alpha = 0.1f),
                                                    cursorColor = LuraIndigo
                                                ),
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Search,
                                                        contentDescription = null,
                                                        tint = LuraGhostWhite.copy(alpha = 0.4f),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))
               
                                        if (state.searchResults.isNotEmpty()) {
                                            LazyColumn(
                                                modifier = Modifier.fillMaxHeight(0.6f),
                                                contentPadding = PaddingValues(bottom = 16.dp)
                                            ) {
                                                items(state.searchResults) { result ->
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                viewModel.jumpToSearchResult(result)
                                                                viewModel.clearSearch()
                                                            }
                                                            .padding(horizontal = 24.dp, vertical = 16.dp)
                                                    ) {
                                                        Text(
                                                            text = "CHAPTER ${result.chapterIndex + 1}",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                letterSpacing = 1.sp,
                                                                fontWeight = FontWeight.Bold
                                                            ),
                                                            color = LuraIndigo
                                                        )
                                                        Spacer(Modifier.height(4.dp))
                                                        Text(
                                                            text = result.snippet,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = LuraGhostWhite.copy(alpha = 0.8f),
                                                            maxLines = 3
                                                        )
                                                    }
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(horizontal = 24.dp),
                                                        color = LuraGhostWhite.copy(alpha = 0.05f)
                                                    )
                                                }
                                            }
                                        } else if (state.searchQuery.isNotBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(48.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "No results found",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = LuraGhostWhite.copy(alpha = 0.4f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (showSettings) {
                                ModalBottomSheet(
                                    onDismissRequest = { showSettings = false },
                                    dragHandle = null,
                                    containerColor = LuraObsidian,
                                    tonalElevation = 0.dp
                                ) {
                                    var isMoreSettingsExpanded by remember { mutableStateOf(false) }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 32.dp)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        // 1. Header
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 24.dp, vertical = 20.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Appearance",
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.5.sp
                                                ),
                                                color = LuraGhostWhite
                                            )
                                            IconButton(onClick = { viewModel.toggleSearch(); showSettings = false }) {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = "Search",
                                                    tint = LuraGhostWhite.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 24.dp),
                                            verticalArrangement = Arrangement.spacedBy(32.dp)
                                        ) {
                                            // 2. THEME Section
                                            Column {
                                                Text(
                                                    "THEME",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        letterSpacing = 1.2.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = LuraGhostWhite.copy(alpha = 0.4f),
                                                    modifier = Modifier.padding(bottom = 16.dp)
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    ReaderTheme.entries.forEach { theme ->
                                                        val isSelected = state.theme == theme
                                                        Box(
                                                            modifier = Modifier
                                                                .size(56.dp)
                                                                .background(
                                                                    color = Color(theme.backgroundColor),
                                                                    shape = CircleShape
                                                                )
                                                                .border(
                                                                    width = if (isSelected) 2.dp else 1.dp,
                                                                    color = if (isSelected) LuraIndigo else LuraGhostWhite.copy(alpha = 0.1f),
                                                                    shape = CircleShape
                                                                )
                                                                .clickable { viewModel.updateTheme(theme) },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            if (isSelected) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Done,
                                                                    contentDescription = null,
                                                                    tint = if (theme == ReaderTheme.DARK || theme == ReaderTheme.NIGHT) Color.White else Color.Black,
                                                                    modifier = Modifier.size(24.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            // 3. TYPOGRAPHY Section
                                            Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
                                                SegmentedControl(
                                                    label = "FONT FAMILY",
                                                    options = listOf("Serif", "Sans", "Mono"),
                                                    selectedIndex = state.fontFamily.ordinal,
                                                    onOptionSelected = { viewModel.updateFontFamily(ReaderFontFamily.entries[it]) }
                                                )

                                                ElegantSlider(
                                                    label = "FONT SIZE",
                                                    value = state.fontSize.toFloat(),
                                                    onValueChange = { viewModel.updateFontSize(it.toInt()) },
                                                    valueRange = 12f..32f,
                                                    startIcon = Icons.Default.Remove,
                                                    endIcon = Icons.Default.Add
                                                )

                                                ElegantSlider(
                                                    label = "LINE HEIGHT",
                                                    value = state.lineHeightMultiplier,
                                                    onValueChange = { viewModel.updateLineHeight(it) },
                                                    valueRange = 1.0f..2.5f,
                                                    startIcon = Icons.Default.FormatLineSpacing
                                                )
                                            }

                                            // 4. LAYOUT & COMFORT
                                            Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
                                                ElegantSlider(
                                                    label = "MARGINS",
                                                    value = state.horizontalPadding.toFloat(),
                                                    onValueChange = { viewModel.updateHorizontalPadding(it.toInt()) },
                                                    valueRange = 8f..48f,
                                                    startIcon = Icons.Default.FormatIndentIncrease
                                                )

                                                ElegantSlider(
                                                    label = "BRIGHTNESS",
                                                    value = 1f - state.brightnessDimmer,
                                                    onValueChange = { viewModel.updateBrightnessDimmer(1f - it) },
                                                    valueRange = 0.2f..1f,
                                                    startIcon = Icons.Default.WbSunny
                                                )
                                            }

                                            // 5. MORE SETTINGS Section
                                            Column {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(LuraGhostWhite.copy(alpha = 0.05f))
                                                        .clickable { isMoreSettingsExpanded = !isMoreSettingsExpanded }
                                                        .padding(16.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        "ADVANCED SETTINGS",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            letterSpacing = 1.2.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = LuraGhostWhite.copy(alpha = 0.6f)
                                                    )
                                                    Icon(
                                                        imageVector = if (isMoreSettingsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                        contentDescription = null,
                                                        tint = LuraGhostWhite.copy(alpha = 0.4f),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }

                                                androidx.compose.animation.AnimatedVisibility(visible = isMoreSettingsExpanded) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 16.dp),
                                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                                    ) {
                                                        // Example Advanced Items
                                                        AdvancedSettingItem(Icons.Default.ScreenRotation, "Lock Orientation")
                                                        AdvancedSettingItem(Icons.Default.AutoStories, "Page Flip Animation")
                                                        AdvancedSettingItem(Icons.Default.SwapCalls, "Left Hand Mode")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } // End AnimatedVisibility (Bottom Bar)

                // 4. Loading Overlay (Atomic & Non-Intrusive)
                androidx.compose.animation.AnimatedVisibility(
                    visible = isPaginating,
                    enter = androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (pages.isEmpty()) themeSurface else Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = if (pages.isEmpty()) MaterialTheme.colorScheme.primary else Color.White,
                                strokeWidth = 3.dp
                            )
                            if (pages.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Optimizing layout...",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            } // End root Box (line 165)
        } // End Scaffold content lambda (line 159)
        } // End is ReaderUiState.Ready (line 153)
    } // End when(state) (line 142)
} // End ReaderScreen

@Composable
fun ZoomableImageDialog(
    bitmap: androidx.compose.ui.graphics.ImageBitmap,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false, // Full screen
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() }, // Click bg to dismiss
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceAtLeast(1f)
                            if (scale == 1f) offset = androidx.compose.ui.geometry.Offset.Zero
                            else offset += pan
                        }
                    },
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        }
    }
}
@Composable
fun ElegantSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    startIcon: ImageVector? = null,
    endIcon: ImageVector? = null,
    label: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = LuraGhostWhite.copy(alpha = 0.4f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (startIcon != null) {
                Icon(
                    imageVector = startIcon,
                    contentDescription = null,
                    tint = LuraGhostWhite.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = LuraGhostWhite,
                    activeTrackColor = LuraIndigo,
                    inactiveTrackColor = LuraGhostWhite.copy(alpha = 0.1f)
                )
            )
            if (endIcon != null) {
                Icon(
                    imageVector = endIcon,
                    contentDescription = null,
                    tint = LuraGhostWhite.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    label: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = LuraGhostWhite.copy(alpha = 0.4f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            color = LuraGhostWhite.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, LuraGhostWhite.copy(alpha = 0.1f))
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                options.forEachIndexed { index, option ->
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) LuraIndigo else Color.Transparent)
                            .clickable { onOptionSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) LuraGhostWhite else LuraGhostWhite.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun AdvancedSettingItem(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Feature implementation */ }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = LuraGhostWhite.copy(alpha = 0.05f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = LuraGhostWhite.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = LuraGhostWhite.copy(alpha = 0.8f)
        )
    }
}

// Helper extension to find Activity from Context
private tailrec fun android.content.Context.findActivity(): android.app.Activity? = when (this) {
    is android.app.Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}
