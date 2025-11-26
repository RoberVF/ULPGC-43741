package com.roberto.goodbooks.ui.library

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.goodbooks.GoodBooksApp
import com.roberto.goodbooks.db.Book
import com.roberto.goodbooks.network.models.BookItem
import com.roberto.goodbooks.domain.toBookItem
import kotlinx.coroutines.launch
import java.time.LocalDate

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = getApplication<GoodBooksApp>().bookRepository

    private var allBooks = listOf<Book>()

    // Lista visible en pantalla
    var books by mutableStateOf<List<Book>>(emptyList())
        private set

    // Filtros básicos
    var statusFilter by mutableStateOf<String?>(null)
        private set

    var searchQuery by mutableStateOf("")
        private set

    // Filtros avanzados: Páginas
    var minPagesFilter by mutableStateOf("")
        private set
    var maxPagesFilter by mutableStateOf("")
        private set

    // --- NUEVO: Filtros avanzados: Fechas ---
    var startDateFilter by mutableStateOf("")
        private set
    var endDateFilter by mutableStateOf("")
        private set

    var selectedBook by mutableStateOf<BookItem?>(null)
        private set

    fun loadLibrary() {
        viewModelScope.launch {
            allBooks = repository.getAllBooks()
            applyFilters()
        }
    }

    private fun applyFilters() {
        var result = allBooks

        // 1. Filtro Estado
        if (statusFilter != null) {
            result = result.filter { it.status == statusFilter }
        }

        // 2. Filtro Texto
        if (searchQuery.isNotBlank()) {
            result = result.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        (it.authors?.contains(searchQuery, ignoreCase = true) == true)
            }
        }

        // 3. Filtro Páginas
        val minP = minPagesFilter.toIntOrNull()
        val maxP = maxPagesFilter.toIntOrNull()
        if (minP != null) result = result.filter { (it.pageCount ?: 0) >= minP }
        if (maxP != null) result = result.filter { (it.pageCount ?: 0) <= maxP }

        // 4. Filtro Fechas (Comparamos texto ISO YYYY-MM-DD)
        if (startDateFilter.isNotBlank()) {
            // Solo libros con fecha fin >= filtro inicio
            result = result.filter { it.endDate != null && it.endDate!! >= startDateFilter }
        }
        if (endDateFilter.isNotBlank()) {
            // Solo libros con fecha fin <= filtro fin
            result = result.filter { it.endDate != null && it.endDate!! <= endDateFilter }
        }

        books = result
    }

    // --- ACCIONES ---
    fun onStatusFilterChanged(newStatus: String?) {
        statusFilter = if (statusFilter == newStatus) null else newStatus
        applyFilters()
    }

    fun onSearchQueryChanged(newQuery: String) {
        searchQuery = newQuery
        applyFilters()
    }

    fun onAdvancedFiltersChanged(minPages: String, maxPages: String, start: String, end: String) {
        minPagesFilter = minPages
        maxPagesFilter = maxPages
        startDateFilter = start
        endDateFilter = end
        applyFilters()
    }

    fun clearAllFilters() {
        statusFilter = null
        searchQuery = ""
        minPagesFilter = ""
        maxPagesFilter = ""
        startDateFilter = ""
        endDateFilter = ""
        applyFilters()
    }

    fun onBookClick(book: Book) {
        selectedBook = book.toBookItem()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startReading(bookId: String) {
        val today = LocalDate.now().toString()
        viewModelScope.launch {
            repository.updateBookStatus(bookId, "IN_PROGRESS", startDate = today, endDate = null)
            loadLibrary()
            updateSelectedBookStatus("IN_PROGRESS", today, null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun finishReading(bookId: String, rating: Int) {
        val today = LocalDate.now().toString()
        viewModelScope.launch {
            val startDate = selectedBook?.volumeInfo?.myStartDate
            repository.updateBookStatus(bookId, "COMPLETED", startDate = startDate, endDate = today)
            repository.updateBookRating(bookId, rating.toLong())
            loadLibrary()
            updateSelectedBookStatus("COMPLETED", startDate, today, rating)
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            repository.deleteBookById(bookId)
            loadLibrary()
        }
    }

    private fun updateSelectedBookStatus(status: String, start: String?, end: String?, rating: Int? = null) {
        selectedBook = selectedBook?.copy(
            volumeInfo = selectedBook!!.volumeInfo.copy(
                myStatus = status,
                myStartDate = start,
                myEndDate = end,
                myRating = rating ?: selectedBook!!.volumeInfo.myRating
            )
        )
    }
}