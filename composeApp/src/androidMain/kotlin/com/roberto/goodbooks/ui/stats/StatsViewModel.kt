package com.roberto.goodbooks.ui.stats

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.goodbooks.GoodBooksApp
import com.roberto.goodbooks.db.Book
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = getApplication<GoodBooksApp>().bookRepository

    // --- VARIABLES DE ESTADO ---

    // Básicos
    var totalBooks by mutableStateOf(0); private set
    var booksRead by mutableStateOf(0); private set
    var booksReading by mutableStateOf(0); private set
    var booksPending by mutableStateOf(0); private set
    var totalPagesRead by mutableStateOf(0); private set
    var averageRating by mutableStateOf(0.0); private set
    var booksReadThisYear by mutableStateOf(0); private set

    // Avanzados - Récords
    var topAuthor by mutableStateOf<Pair<String, Int>?>(null); private set
    var longestBook by mutableStateOf<Book?>(null); private set
    var shortestBook by mutableStateOf<Book?>(null); private set

    // Avanzados - Hábitos
    var averageDaysPerBook by mutableStateOf(0); private set

    // Reto Anual (Esta es la variable que te faltaba)
    var yearlyGoal by mutableStateOf(12)
        private set

    // --- CÁLCULOS ---

    fun loadStats() {
        viewModelScope.launch {
            val allBooks = repository.getAllBooks()
            val currentYear = LocalDate.now().year.toString()

            // 1. Básicos
            totalBooks = allBooks.size
            val completed = allBooks.filter { it.status == "COMPLETED" }

            booksRead = completed.size
            booksReading = allBooks.count { it.status == "IN_PROGRESS" }
            booksPending = allBooks.count { it.status == "PENDING" || it.status == null }
            totalPagesRead = completed.sumOf { it.pageCount?.toInt() ?: 0 }
            booksReadThisYear = completed.count { it.endDate?.startsWith(currentYear) == true }

            // Nota media
            val ratedBooks = completed.filter { it.rating != null && it.rating!! > 0 }
            averageRating = if (ratedBooks.isNotEmpty()) {
                val avg = ratedBooks.map { it.rating!!.toDouble() }.average()
                (avg * 10.0).roundToInt() / 10.0
            } else 0.0

            // 2. Calcular Récords
            calculateRecords(completed)

            // 3. Calcular Hábitos (Velocidad)
            calculateReadingHabits(completed)
        }
    }

    fun updateYearlyGoal(newGoal: Int) {
        if (newGoal > 0) {
            yearlyGoal = newGoal
        }
    }
    private fun calculateRecords(completed: List<Book>) {
        if (completed.isEmpty()) {
            topAuthor = null
            longestBook = null
            shortestBook = null
            return
        }

        // Autor más leído: Separamos por comas, agrupamos y contamos
        val authorCounts = completed
            .flatMap { it.authors?.split(", ") ?: emptyList() }
            .groupingBy { it }
            .eachCount()

        // Cogemos el máximo
        topAuthor = authorCounts.maxByOrNull { it.value }?.toPair()

        // Libros extremos
        longestBook = completed.maxByOrNull { it.pageCount ?: 0 }
        shortestBook = completed.filter { (it.pageCount ?: 0) > 0 }.minByOrNull { it.pageCount ?: 0 }
    }

    private fun calculateReadingHabits(completed: List<Book>) {
        // Filtramos libros que tengan fecha de inicio Y fin
        val timedBooks = completed.filter { it.startDate != null && it.endDate != null }

        if (timedBooks.isNotEmpty()) {
            var totalDays = 0L
            var count = 0

            // Formateador para leer las fechas "YYYY-MM-DD"
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE

            for (book in timedBooks) {
                try {
                    val start = LocalDate.parse(book.startDate, formatter)
                    val end = LocalDate.parse(book.endDate, formatter)
                    val days = ChronoUnit.DAYS.between(start, end)
                    if (days >= 0) {
                        totalDays += days
                        count++
                    }
                } catch (e: Exception) {
                    // Ignorar fechas mal formadas
                }
            }

            // Calculamos la media (mínimo 1 día para no dividir por 0)
            averageDaysPerBook = if (count > 0) (totalDays / count).toInt().coerceAtLeast(1) else 0
        } else {
            averageDaysPerBook = 0
        }
    }
}