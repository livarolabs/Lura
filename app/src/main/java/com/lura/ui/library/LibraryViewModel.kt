package com.lura.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lura.domain.model.Book
import com.lura.domain.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository
) : ViewModel() {

    val books: StateFlow<List<Book>> = repository.getLibraryBooks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Extract covers for existing books that don't have them
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                (repository as? com.lura.data.repository.LibraryRepositoryImpl)?.extractMissingCovers()
            } catch (e: Exception) {
                android.util.Log.e("LibraryViewModel", "Error extracting missing covers", e)
            }
        }
    }

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            // Auto-import default book if library is empty
            if (repository.getLibraryBooks().first().isEmpty()) {
                importDefaultBook()
            }
        }
    }

    private val _uiEvent = kotlinx.coroutines.channels.Channel<LibraryEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    sealed class LibraryEvent {
        object ImportSuccess : LibraryEvent()
        data class ImportError(val message: String) : LibraryEvent()
    }

    fun importBook(uri: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.importBook(uri)
                _uiEvent.send(LibraryEvent.ImportSuccess)
            } catch (e: Exception) {
                e.printStackTrace()
                val msg = e.message ?: "Unknown error"
                _uiEvent.send(LibraryEvent.ImportError("Import failed: $msg"))
            }
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.deleteBook(bookId)
        }
    }

    private fun importDefaultBook() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // "The Will of the Many" (test.epub in assets)
                repository.importBookFromAssets("test.epub")
                _uiEvent.send(LibraryEvent.ImportSuccess)
             } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
