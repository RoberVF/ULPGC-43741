package com.roberto.goodbooks.ui.library

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.goodbooks.GoodBooksApp
import com.roberto.goodbooks.db.Book
import kotlinx.coroutines.launch
import com.roberto.goodbooks.network.models.BookItem
import com.roberto.goodbooks.domain.toBookItem
import java.time.LocalDate

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = getApplication<GoodBooksApp>().bookRepository

    // Estado: La lista de libros que mostraremos
    var books by mutableStateOf<List<Book>>(emptyList())
        private set

    // Función para recargar la lista (se llamará al abrir la pantalla)
    fun loadLibrary() {
        viewModelScope.launch {
            // Pedimos los libros a la base de datos local
            books = repository.getAllBooks()
        }
    }

    // Libro seleccionado para ver en detalle
    var selectedBook by mutableStateOf<BookItem?>(null)
        private set
    // Cuando pulsamos un libro en la biblioteca
    fun onBookClick(book: Book) {
        // Lo convertimos al formato que entiende la pantalla de detalle
        selectedBook = book.toBookItem()
    }
    // Función para borrar el libro y actualizar la pantalla
    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            repository.deleteBookById(bookId)
            loadLibrary()
        }
    }

    // LOGICA
    // Empieza libro
    fun startReading(bookId: String) {
        val today = LocalDate.now().toString() // "2025-11-26"
        viewModelScope.launch {
            // Cambiamos estado a IN_PROGRESS y guardamos fecha inicio
            repository.updateBookStatus(bookId, "IN_PROGRESS", startDate = today, endDate = null)

            loadLibrary() // Recargamos lista

            // Truco: Actualizamos también el 'selectedBook' para que la pantalla de detalle se entere del cambio al instante
            selectedBook = selectedBook?.copy(
                volumeInfo = selectedBook!!.volumeInfo.copy(
                    myStatus = "IN_PROGRESS",
                    myStartDate = today
                )
            )
        }
    }

    // Termina libro
    fun finishReading(bookId: String, rating: Int) {
        val today = LocalDate.now().toString()
        viewModelScope.launch {
            // 1. Guardamos estado COMPLETED y fecha fin
            repository.updateBookStatus(bookId, "COMPLETED", startDate = selectedBook?.volumeInfo?.myStartDate, endDate = today)
            // 2. Guardamos el rating
            repository.updateBookRating(bookId, rating.toLong())

            loadLibrary()

            // Actualizamos la vista actual
            selectedBook = selectedBook?.copy(
                volumeInfo = selectedBook!!.volumeInfo.copy(
                    myStatus = "COMPLETED",
                    myEndDate = today,
                    myRating = rating
                )
            )
        }
    }
}