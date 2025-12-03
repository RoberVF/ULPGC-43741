package com.roberto.goodbooks.ui.library

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.goodbooks.GoodBooksApp
import com.roberto.goodbooks.db.Book
import com.roberto.goodbooks.db.Category
import com.roberto.goodbooks.domain.toBookItem
import com.roberto.goodbooks.network.models.BookItem
import kotlinx.coroutines.launch

class ShelfDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = getApplication<GoodBooksApp>().bookRepository

    // La estantería que estamos viendo
    var shelf by mutableStateOf<Category?>(null)
        private set

    // Los libros que hay DENTRO
    var booksInShelf by mutableStateOf<List<Book>>(emptyList())
        private set

    // Los libros DISPONIBLES para añadir (Todos - Los que ya están)
    var availableBooks by mutableStateOf<List<Book>>(emptyList())
        private set

    fun loadShelf(shelfId: Long) {
        viewModelScope.launch {
            // 1. Buscamos la info de la estantería
            val allShelves = repository.getAllShelves()
            shelf = allShelves.find { it.id == shelfId }

            // 2. Cargamos los libros que tiene dentro
            refreshBooks(shelfId)
        }
    }

    private suspend fun refreshBooks(shelfId: Long) {
        booksInShelf = repository.getBooksInShelf(shelfId)
    }

    // Prepara la lista de libros que se pueden añadir
    fun loadAvailableBooks() {
        viewModelScope.launch {
            val all = repository.getAllBooks()
            // Filtramos: Solo mostramos los que NO están ya en la estantería
            val existingIds = booksInShelf.map { it.gid }.toSet()
            availableBooks = all.filter { it.gid !in existingIds }
        }
    }

    fun addBookToShelf(book: Book) {
        val s = shelf ?: return
        viewModelScope.launch {
            repository.addBookToShelf(book.gid, s.id)
            refreshBooks(s.id)
            loadAvailableBooks() // Actualizamos disponibles
        }
    }

    fun removeBookFromShelf(book: Book) {
        val s = shelf ?: return
        viewModelScope.launch {
            repository.removeBookFromShelf(book.gid, s.id)
            refreshBooks(s.id)
        }
    }
}