package com.lura.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lura.domain.model.Book
import com.lura.ui.theme.LuraGhostWhite
import com.lura.ui.theme.LuraIndigo
import com.lura.ui.theme.LuraObsidian

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onBookClick: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val books by viewModel.books.collectAsState()
    var viewMode by remember { mutableStateOf(ViewMode.GRID) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(SortOption.RECENTLY_OPENED) }
    var showSortSheet by remember { mutableStateOf(false) }
    var bookToDelete by remember { mutableStateOf<Book?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Find currently reading book for Hero slot
    val currentlyReadingBook = remember(books) {
        books.firstOrNull { it.isCurrentlyReading || it.readingStatus == com.lura.domain.model.ReadingStatus.READING }
    }

    // Filter and sort books
    val filteredBooks = remember(books, searchQuery, sortOption) {
        var filtered = books.filter {
            searchQuery.isEmpty() || 
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.author.contains(searchQuery, ignoreCase = true) ||
            it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
        }
        
        filtered = when (sortOption) {
            SortOption.RECENTLY_OPENED -> filtered.sortedByDescending { it.lastReadTimestamp }
            SortOption.TITLE_AZ -> filtered.sortedBy { it.title }
            SortOption.AUTHOR_AZ -> filtered.sortedBy { it.author }
            SortOption.PROGRESS_DESC -> filtered.sortedByDescending { it.progressPercentage }
            SortOption.PROGRESS_ASC -> filtered.sortedBy { it.progressPercentage }
            SortOption.DATE_ADDED -> filtered.sortedByDescending { it.importDate }
            SortOption.TIME_TO_FINISH -> filtered.sortedBy { 
                if (it.estimatedReadingTimeMinutes > 0) {
                    ((100 - it.progressPercentage) / 100 * it.estimatedReadingTimeMinutes).toInt()
                } else Int.MAX_VALUE
            }
        }
        filtered
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is LibraryViewModel.LibraryEvent.ImportSuccess -> 
                    snackbarHostState.showSnackbar("Book imported successfully")
                is LibraryViewModel.LibraryEvent.ImportError -> 
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.importBook(it.toString()) }
    }

    // Delete confirmation dialog
    if (bookToDelete != null) {
        AlertDialog(
            onDismissRequest = { bookToDelete = null },
            title = { Text("Delete Book") },
            text = { Text("Are you sure you want to delete \"${bookToDelete?.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        bookToDelete?.let { viewModel.deleteBook(it.id) }
                        bookToDelete = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Sort sheet
    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            containerColor = LuraObsidian
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Sort By",
                    style = MaterialTheme.typography.titleLarge,
                    color = LuraGhostWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                SortOption.values().forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = sortOption == option,
                            onClick = {
                                sortOption = option
                                showSortSheet = false
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = LuraIndigo,
                                unselectedColor = LuraGhostWhite.copy(alpha = 0.3f)
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = option.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = LuraGhostWhite
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    Scaffold(
        containerColor = LuraObsidian,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { launcher.launch(arrayOf("application/epub+zip")) },
                containerColor = LuraIndigo,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Import Book")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Library",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = LuraGhostWhite
                    )
                    Text(
                        text = "${filteredBooks.size} ${if (filteredBooks.size == 1) "book" else "books"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LuraGhostWhite.copy(alpha = 0.4f)
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // View mode toggle
                    IconButton(
                        onClick = { viewMode = if (viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID },
                        modifier = Modifier.background(LuraGhostWhite.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (viewMode == ViewMode.GRID) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Toggle view",
                            tint = LuraIndigo
                        )
                    }
                    
                    // Sort button
                    IconButton(
                        onClick = { showSortSheet = true },
                        modifier = Modifier.background(LuraGhostWhite.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = LuraIndigo
                        )
                    }
                }
            }

            // Hero Slot
            if (currentlyReadingBook != null) {
                HeroBookSlot(
                    currentBook = currentlyReadingBook,
                    onResumeClick = { onBookClick(currentlyReadingBook.id) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Search Bar
            LibrarySearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Books Grid/List
            if (filteredBooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "No books yet\nTap + to import" else "No books found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = LuraGhostWhite.copy(alpha = 0.4f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                when (viewMode) {
                    ViewMode.GRID -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredBooks) { book ->
                                BookGridItem(
                                    book = book,
                                    onClick = { onBookClick(book.id) },
                                    onLongClick = { bookToDelete = book }
                                )
                            }
                        }
                    }
                    ViewMode.LIST -> {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredBooks) { book ->
                                BookListItem(
                                    book = book,
                                    onClick = { onBookClick(book.id) },
                                    onLongClick = { bookToDelete = book }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
