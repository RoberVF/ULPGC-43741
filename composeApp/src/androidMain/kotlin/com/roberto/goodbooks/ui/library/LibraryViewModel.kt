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
}