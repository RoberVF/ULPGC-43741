package com.roberto.goodbooks.ui.stats

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.goodbooks.GoodBooksApp
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = getApplication<GoodBooksApp>().bookRepository

    // --- VARIABLES DE ESTADO (Lo que ve la pantalla) ---

    var totalBooks by mutableStateOf(0)
        private set

    var booksRead by mutableStateOf(0)
        private set

    var booksReading by mutableStateOf(0)
        private set

    var booksPending by mutableStateOf(0)
        private set

    var totalPagesRead by mutableStateOf(0)
        private set

    var averageRating by mutableStateOf(0.0)
        private set

    var booksReadThisYear by mutableStateOf(0)
        private set

    // --- CÁLCULOS ---

    fun loadStats() {
        viewModelScope.launch {
            val allBooks = repository.getAllBooks()
            val currentYear = LocalDate.now().year.toString() // "2025"

            totalBooks = allBooks.size

            // Filtramos por estado
            val completed = allBooks.filter { it.status == "COMPLETED" }

            booksRead = completed.size
            booksReading = allBooks.count { it.status == "IN_PROGRESS" }
            booksPending = allBooks.count { it.status == "PENDING" || it.status == null }

            // 1. Suma de páginas (Solo de libros terminados)
            totalPagesRead = completed.sumOf { it.pageCount?.toInt() ?: 0 }

            // 2. Libros leídos este año (Miramos si la fecha fin empieza por "2025")
            booksReadThisYear = completed.count { it.endDate?.startsWith(currentYear) == true }

            // 3. Nota media
            val ratedBooks = completed.filter { it.rating != null && it.rating!! > 0 }
            if (ratedBooks.isNotEmpty()) {
                val avg = ratedBooks.map { it.rating!!.toDouble() }.average()
                // Redondeamos a 1 decimal (ej. 8.5)
                averageRating = (avg * 10.0).roundToInt() / 10.0
            } else {
                averageRating = 0.0
            }
        }
    }
}