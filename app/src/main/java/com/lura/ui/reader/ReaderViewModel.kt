package com.lura.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lura.domain.engine.BookContent
import com.lura.domain.engine.EpubParser
import com.lura.domain.engine.PulseEvent
import com.lura.domain.engine.PulseWord
import com.lura.domain.engine.PulseWordInfo
import com.lura.domain.engine.RsvpEngine
import com.lura.domain.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: LibraryRepository,
    private val epubParser: EpubParser,
    private val rsvpEngine: RsvpEngine,
    private val hardwareKeyManager: com.lura.domain.hardware.HardwareKeyManager
) : ViewModel() {

    private val bookId: String = checkNotNull(savedStateHandle["bookId"])

    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var pulseJob: Job? = null
    private var highlightsJob: Job? = null
    var allPulseWords: List<PulseWordInfo> = emptyList()
        private set

    // Navigation/Scroll Events
    private val _navigationEvents = MutableSharedFlow<ReaderNavigationEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navigationEvents: SharedFlow<ReaderNavigationEvent> = _navigationEvents.asSharedFlow()

    // Common abbreviations that should NOT end a sentence
    private val commonAbbreviations = setOf(
        "Mr", "Mrs", "Ms", "Dr", "Prof", "Sr", "Jr",
        "vs", "etc", "e.g", "i.e", "Inc", "Ltd", "Co"
    )

    /**
     * Determines if a word ends a sentence.
     * Checks for sentence-ending punctuation while avoiding false positives from abbreviations.
     */
    private fun isSentenceEnd(word: String, currentSentence: String): Boolean {
        // Check if word ends with sentence-ending punctuation
        val endsWithPunctuation = word.endsWith(".") || word.endsWith("!") || 
                                  word.endsWith("?") || word.endsWith("...")
        
        if (!endsWithPunctuation) return false
        
        // Remove punctuation to get the base word
        val baseWord = word.trimEnd('.', '!', '?', ' ')
        
        // Don't end sentence if it's a common abbreviation
        if (commonAbbreviations.contains(baseWord)) return false
        
        // Don't end if it looks like an abbreviation (single letter followed by period)
        if (baseWord.length == 1 && word.endsWith(".")) return false
        
        return true
    }

    init {
        loadBook()
        observeHardwareKeys()
        observeHighlights()
    }

    private fun observeHardwareKeys() {
        viewModelScope.launch {
            hardwareKeyManager.events.collect { event ->
                handleHardwareKey(event)
            }
        }
    }

    private fun handleHardwareKey(event: com.lura.domain.hardware.KeyEventType) {
        val currentState = _uiState.value as? ReaderUiState.Ready ?: return
        
        when (event) {
            com.lura.domain.hardware.KeyEventType.VolumeDownLong -> {
                // Long press toggles Pulse ON if OFF
                if (!currentState.isPulseMode) {
                    startPulse(currentState.currentWordIndex)
                }
            }
            com.lura.domain.hardware.KeyEventType.VolumeDown -> {
                // Short press stops Pulse if ON, else Scroll Down
                if (currentState.isPulseMode) {
                    stopPulse()
                } else {
                    _navigationEvents.tryEmit(ReaderNavigationEvent.ScrollDown)
                }
            }
            com.lura.domain.hardware.KeyEventType.VolumeUp -> {
                 if (currentState.isPulseMode) {
                    // Optional: Scroll Up or do nothing
                   _navigationEvents.tryEmit(ReaderNavigationEvent.ScrollUp)
                } else {
                    _navigationEvents.tryEmit(ReaderNavigationEvent.ScrollUp)
                }
            }
        }
    }

    private fun loadBook() {
        viewModelScope.launch {
            val book = repository.getBookById(bookId)
            if (book != null) {
                // Use runCatching to handle parsing errors
                try {
                    val content = epubParser.parseBook(book.filePath)
                    // Flatten content for pulse with metadata and sentence detection
                    // OPTIMIZATION: Moved to lazy initialization when Pulse is activated
                    /*
                    val words = mutableListOf<PulseWordInfo>()
                    var globalIdx = 0
                    var sentenceIdx = 0
                    var currentSentence = StringBuilder()
                    
                    content.chapters.forEachIndexed { chapterIdx, chapter ->
                        chapter.elements.forEachIndexed { elementIdx, element ->
                            if (element is com.lura.domain.engine.ReaderElement.Text) {
                                val elementWords = element.content.split(Regex("\\s+")).filter { it.isNotEmpty() }
                                elementWords.forEachIndexed { wordInElementIdx, wordText ->
                                    // Add word to current sentence
                                    currentSentence.append(wordText).append(" ")
                                    
                                    words.add(
                                        PulseWordInfo(
                                            text = wordText,
                                            chapterIndex = chapterIdx,
                                            elementIndex = elementIdx,
                                            offsetInElement = wordInElementIdx,
                                            globalIndex = globalIdx++,
                                            sentenceIndex = sentenceIdx
                                        )
                                    )
                                    
                                    // Check if this word ends a sentence
                                    if (isSentenceEnd(wordText, currentSentence.toString())) {
                                        sentenceIdx++
                                        currentSentence.clear()
                                    }
                                }
                            }
                        }
                    }
                    */
                    allPulseWords = emptyList() // Will be populated lazily when Pulse is activated
                    
                    if (allPulseWords.isEmpty()) { 
                         println("ReaderViewModel: Parsed content is empty")
                    }

                _uiState.value = ReaderUiState.Ready(
                    bookContent = content,
                    currentWordIndex = 0,
                    initialProgress = book.progress / 100f  // Convert percentage (0-100) to fraction (0.0-1.0)
                )
            } catch (e: Exception) {
                _uiState.value = ReaderUiState.Error("Failed to parse book: ${e.message}")
            }
        } else {
            _uiState.value = ReaderUiState.Error("Book not found")
        }
    }
}


    /**
     * Lazy initialization of Pulse words - only called when Pulse is activated
     */
    private suspend fun initializePulseWords() {
        val currentState = _uiState.value as? ReaderUiState.Ready ?: return
        if (allPulseWords.isNotEmpty()) return
        
        android.util.Log.d("ReaderViewModel", "Initializing Pulse words...")
        
        val words = mutableListOf<PulseWordInfo>()
        var globalIdx = 0
        var sentenceIdx = 0
        var currentSentence = StringBuilder()
        
        currentState.bookContent.chapters.forEachIndexed { chapterIdx, chapter ->
            chapter.elements.forEachIndexed { elementIdx, element ->
                if (element is com.lura.domain.engine.ReaderElement.Text) {
                    val elementWords = element.content.split(Regex("\\s+")).filter { it.isNotEmpty() }
                    elementWords.forEachIndexed { wordInElementIdx, wordText ->
                        currentSentence.append(wordText).append(" ")
                        words.add(
                            PulseWordInfo(
                                text = wordText,
                                chapterIndex = chapterIdx,
                                elementIndex = elementIdx,
                                offsetInElement = wordInElementIdx,
                                globalIndex = globalIdx++,
                                sentenceIndex = sentenceIdx
                            )
                        )
                        if (isSentenceEnd(wordText, currentSentence.toString())) {
                            sentenceIdx++
                            currentSentence.clear()
                        }
                    }
                }
            }
        }
        allPulseWords = words
        android.util.Log.d("ReaderViewModel", "Pulse initialized: ${words.size} words")
    }

    private fun observeHighlights() {
        highlightsJob?.cancel()
        highlightsJob = viewModelScope.launch {
            // For MVP: Observe chapter 0.Ideally update this when chapter changes
            repository.getHighlights(bookId, 0).collect { list ->
                 val currentState = _uiState.value as? ReaderUiState.Ready
                 if (currentState != null) {
                    _uiState.value = currentState.copy(highlights = list)
                 }
            }
        }
    }

    fun addHighlight(chapterIndex: Int, elementIndex: Int, startIndex: Int, endIndex: Int, color: Int) {
         viewModelScope.launch {
            repository.addHighlight(
                com.lura.data.db.entity.Highlight(
                    bookId = bookId,
                    chapterIndex = chapterIndex,
                    elementIndex = elementIndex,
                    startIndex = startIndex,
                    endIndex = endIndex,
                    color = color
                )
            )
        }
    }

    fun deleteHighlight(id: Long) {
        viewModelScope.launch {
            repository.deleteHighlight(id)
        }
    }

    fun readNextPage() {
        _navigationEvents.tryEmit(ReaderNavigationEvent.ScrollDown)
    }

    fun readPreviousPage() {
        _navigationEvents.tryEmit(ReaderNavigationEvent.ScrollUp)
    }

    fun performSearch(query: String) {
        val currentState = _uiState.value as? ReaderUiState.Ready ?: return
        if (query.isBlank()) {
            _uiState.value = currentState.copy(searchQuery = "", searchResults = emptyList(), isSearching = false)
            return
        }

        _uiState.value = currentState.copy(searchQuery = query, isSearching = true)

        viewModelScope.launch(Dispatchers.Default) {
            val results = mutableListOf<SearchResult>()
            currentState.bookContent.chapters.forEachIndexed { chapterIdx, chapter ->
                chapter.elements.forEachIndexed { elementIdx, element ->
                    if (element is com.lura.domain.engine.ReaderElement.Text) {
                        val text = element.content
                        var startIndex = 0
                        while (true) {
                            val index = text.indexOf(query, startIndex, ignoreCase = true)
                            if (index == -1) break
                            
                            // Create snippet
                            val snippetStart = (index - 20).coerceAtLeast(0)
                            val snippetEnd = (index + query.length + 20).coerceAtMost(text.length)
                            val snippet = "..." + text.substring(snippetStart, snippetEnd).replace("\n", " ") + "..."
                            
                            results.add(SearchResult(chapterIdx, elementIdx, snippet, index))
                            startIndex = index + 1
                        }
                    }
                }
            }
            
            // Switch back to Main to update state
             withContext(Dispatchers.Main) {
                 val loadedState = _uiState.value as? ReaderUiState.Ready
                 if (loadedState != null && loadedState.searchQuery == query) {
                     _uiState.value = loadedState.copy(searchResults = results, isSearching = false)
                 }
             }
        }
    }

    fun clearSearch() {
        val currentState = _uiState.value as? ReaderUiState.Ready ?: return
        _uiState.value = currentState.copy(searchQuery = "", searchResults = emptyList(), isSearching = false)
    }

    fun toggleSearch() {
        val currentState = _uiState.value as? ReaderUiState.Ready ?: return
        if (currentState.isSearching) {
            clearSearch()
        } else {
            _uiState.value = currentState.copy(isSearching = true)
        }
    }

    fun jumpToSearchResult(result: SearchResult) {
        _navigationEvents.tryEmit(ReaderNavigationEvent.ScrollToChapter(result.chapterIndex, result.elementIndex))
        // Close search or handle UI side? UI should observe navigation and close.
    }

    fun toggleControls() {
        val currentState = _uiState.value as? ReaderUiState.Ready ?: return
        _uiState.value = currentState.copy(showControls = !currentState.showControls)
    }

    
    fun startPulseFromPage(chapterIndex: Int, elementIndex: Int) {
        val currentState = _uiState.value as? ReaderUiState.Ready ?: return
        
        if (currentState.isPulseMode) {
            stopPulse()
        } else {
            // Find the FIRST word in this chapter/element (the top of the page)
            val startIndex = allPulseWords.indexOfFirst { 
                it.chapterIndex == chapterIndex && it.elementIndex == elementIndex 
            }.coerceAtLeast(0)
            
            startPulse(startIndex)
        }
    }

    fun togglePulse(startChapter: Int? = null, startElement: Int? = null) {
        val currentState = _uiState.value as? ReaderUiState.Ready ?: return
        
        if (currentState.isPulseMode) {
            stopPulse()
        } else {
            // Find start index if provided, else use current saved index
            val startIndex = if (startChapter != null && startElement != null) {
                allPulseWords.indexOfFirst { it.chapterIndex == startChapter && it.elementIndex == startElement }
                    .coerceAtLeast(0)
            } else {
                currentState.currentWordIndex
            }
            startPulse(startIndex)
        }
    }

    private fun startPulse(startIndex: Int) {
        val currentState = _uiState.value as? ReaderUiState.Ready ?: return
        
        viewModelScope.launch {
            // Lazy initialization
            if (allPulseWords.isEmpty()) {
                initializePulseWords()
            }
            
            if (allPulseWords.isEmpty()) {
                println("Cannot start pulse: No words")
                return@launch
            }

            val safeStartIndex = startIndex.coerceIn(0, allPulseWords.size - 1)
            val textToRead = allPulseWords.drop(safeStartIndex).joinToString(" ") { it.text }

            _uiState.value = currentState.copy(isPulseMode = true)
        
        pulseJob?.cancel()
        pulseJob = viewModelScope.launch {
            var lastScrolledChapter = -1
            var lastScrolledElement = -1
            
            rsvpEngine.startPulse(textToRead, startWordIndex = 0, wpm = currentState.pulseWpm).collect { event ->
                when (event) {
                    is PulseEvent.Word -> {
                        val globalWordIndex = safeStartIndex + event.index
                        val wordInfo = allPulseWords.getOrNull(globalWordIndex)
                        
                        _uiState.value = (_uiState.value as? ReaderUiState.Ready)?.copy(
                            activePulseWord = event.pulseWord,
                            currentWordIndex = globalWordIndex,
                            currentSentenceIndex = wordInfo?.sentenceIndex  // Track current sentence
                        ) ?: return@collect
                        
                        // Synchronized Scrolling: Only scroll when chapter/element changes
                        if (wordInfo != null) {
                            if (wordInfo.chapterIndex != lastScrolledChapter || wordInfo.elementIndex != lastScrolledElement) {
                                lastScrolledChapter = wordInfo.chapterIndex
                                lastScrolledElement = wordInfo.elementIndex
                                _navigationEvents.tryEmit(
                                    ReaderNavigationEvent.ScrollToChapter(
                                        wordInfo.chapterIndex,
                                        wordInfo.elementIndex
                                    )
                                )
                            }
                        }
                    }
                    PulseEvent.Finished -> {
                        stopPulse()
                    }
                }
            }
        }
        }
    }

    private fun stopPulse() {
        pulseJob?.cancel()
        val currentState = _uiState.value as? ReaderUiState.Ready ?: return
        
        // SYNC: Find the structural position of the last read word
        val lastWordInfo = allPulseWords.getOrNull(currentState.currentWordIndex)
        if (lastWordInfo != null) {
            _navigationEvents.tryEmit(
                ReaderNavigationEvent.ScrollToChapter(
                    lastWordInfo.chapterIndex, 
                    lastWordInfo.elementIndex
                )
            )
        }
        
        _uiState.value = currentState.copy(isPulseMode = false, activePulseWord = null)
    }

    fun updateFontSize(newSize: Int) {
        val currentState = _uiState.value as? ReaderUiState.Ready ?: return
        _uiState.value = currentState.copy(fontSize = newSize.coerceIn(12, 40))
    }
    
    fun updateFontFamily(newFamily: ReaderFontFamily) {
        val currentState = _uiState.value as? ReaderUiState.Ready ?: return
        _uiState.value = currentState.copy(fontFamily = newFamily)
    }

    fun updateTheme(newTheme: ReaderTheme) {
        val currentState = _uiState.value as? ReaderUiState.Ready ?: return
        _uiState.value = currentState.copy(theme = newTheme)
    }

    fun updateBlueLightFilter(intensity: Float) {
        val currentState = _uiState.value as? ReaderUiState.Ready ?: return
        _uiState.value = currentState.copy(blueLightFilterIntensity = intensity.coerceIn(0f, 1f))
    }

    fun updatePulseWpm(newWpm: Int) {
        val currentState = _uiState.value as? ReaderUiState.Ready ?: return
        val wpm = newWpm.coerceIn(100, 1500)
        _uiState.value = currentState.copy(pulseWpm = wpm)
        
        // Restart pulse if it's running to apply new WPM
        if (currentState.isPulseMode) {
            startPulse(currentState.currentWordIndex)
        }
    }

    fun updateBrightnessDimmer(dimmer: Float) {
        val currentState = _uiState.value as? ReaderUiState.Ready ?: return
        _uiState.value = currentState.copy(brightnessDimmer = dimmer.coerceIn(0f, 0.8f))
    }
    
    fun updateHorizontalPadding(padding: Int) {
        val currentState = _uiState.value as? ReaderUiState.Ready ?: return
        _uiState.value = currentState.copy(horizontalPadding = padding.coerceIn(8, 48))
    }
    
    fun updateVerticalPadding(padding: Int) {
        val currentState = _uiState.value as? ReaderUiState.Ready ?: return
        _uiState.value = currentState.copy(verticalPadding = padding.coerceIn(8, 48))
    }
    
    fun updateLineHeight(multiplier: Float) {
        val currentState = _uiState.value as? ReaderUiState.Ready ?: return
        _uiState.value = currentState.copy(lineHeightMultiplier = multiplier.coerceIn(1.0f, 2.5f))
    }
    
    fun jumpToChapter(chapterIndex: Int) {
        viewModelScope.launch {
            _navigationEvents.emit(ReaderNavigationEvent.ScrollToChapter(chapterIndex))
        }
    }

    suspend fun loadImage(path: String): ByteArray? {
        return repository.getBookImage(bookId, path)
    }

    fun saveProgress(progress: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            // Convert 0-1 progress to 0-100 percentage
            val progressPercentage = progress * 100f
            repository.updateProgress(bookId, progressPercentage)
        }
    }
}

sealed class ReaderNavigationEvent {
    data object ScrollDown : ReaderNavigationEvent()
    data object ScrollUp : ReaderNavigationEvent()
    data class ScrollToChapter(val chapterIndex: Int, val elementIndex: Int = 0) : ReaderNavigationEvent()
    data class GoToPage(val pageIndex: Int) : ReaderNavigationEvent()
    data object NextPage : ReaderNavigationEvent()
    data object PreviousPage : ReaderNavigationEvent()
}

data class SearchResult(
    val chapterIndex: Int,
    val elementIndex: Int,
    val snippet: String,
    val matchIndex: Int // Index within the snippet or element? Let's say index in element content
)

enum class ReaderFontFamily {
    SERIF, SANS_SERIF, MONOSPACE
}

enum class ReaderTheme(
    val backgroundColor: Long, 
    val onBackgroundColor: Long,
    val surfaceColor: Long,
    val onSurfaceColor: Long
) {
    LIGHT(0xFFFFFFFF, 0xFF000000, 0xFFF5F5F5, 0xFF000000),
    SEPIA(0xFFF4ECD8, 0xFF5B4636, 0xFFE9E0C9, 0xFF433022),
    DARK(0xFF121212, 0xFFE0E0E0, 0xFF1E1E1E, 0xFFE0E0E0),
    NIGHT(0xFF000000, 0xFFB0B0B0, 0xFF121212, 0xFFA0A0A0)
}

sealed class ReaderUiState {
    data object Loading : ReaderUiState()
    data class Error(val message: String) : ReaderUiState()
    data class Ready(
        val bookContent: BookContent,
        val isPulseMode: Boolean = false,
        val showControls: Boolean = true, // Default to true initially
        val currentWordIndex: Int = 0,
        val activePulseWord: PulseWord? = null,
        val fontSize: Int = 18,
        val fontFamily: ReaderFontFamily = ReaderFontFamily.SERIF,
        val theme: ReaderTheme = ReaderTheme.LIGHT,
        val blueLightFilterIntensity: Float = 0f,
        val brightnessDimmer: Float = 0f,
        val initialProgress: Float = 0f,

        val highlights: List<com.lura.data.db.entity.Highlight> = emptyList(),
        val searchQuery: String = "",
        val searchResults: List<SearchResult> = emptyList(),
        val isSearching: Boolean = false,
        
        // Pagination
        val currentPageIndex: Int = 0,
        val totalPages: Int = 0,
        val isPaginated: Boolean = true, // Toggle between scroll/page modes
        
        // Adjustable Margins (in dp)
        val horizontalPadding: Int = 24, // Left/Right padding
        val verticalPadding: Int = 16,    // Top/Bottom padding
        val lineHeightMultiplier: Float = 1.6f,

        val pulseWpm: Int = 300,
        
        // Sentence Highlighting (The Follower)
        val currentSentenceIndex: Int? = null  // Track which sentence is being pulsed
    ) : ReaderUiState()
}
