package com.roberto.goodbooks.ui.search

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.roberto.goodbooks.GoodBooksApp
import com.roberto.goodbooks.network.models.BookItem
import kotlinx.coroutines.launch
import com.roberto.goodbooks.domain.toDatabaseEntity

// Heredamos de AndroidViewModel para poder acceder a la clase "Application"
class SearchViewModel(application: Application) : AndroidViewModel(application) {

    // 1. Obtenemos el repositorio desde nuestra clase GoodBooksApp
    // Esto conecta la pantalla con la base de datos y la API
    private val repository = getApplication<GoodBooksApp>().bookRepository

    // 2. Estado de la UI (Variables que la pantalla "observa")
    // Texto que el usuario escribe en el buscador
    var searchQuery by mutableStateOf("")
        private set

    // Lista de libros encontrados (empieza vacía)
    var searchResults by mutableStateOf<List<BookItem>>(emptyList())
        private set

    // ¿Estamos cargando? (para mostrar una ruedita de carga)
    var isLoading by mutableStateOf(false)
        private set

    // Mensaje de error (si falla internet)
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // 3. Funciones (Lo que la pantalla puede "ordenar" hacer)

    // Cuando el usuario escribe una letra
    fun onQueryChange(newQuery: String) {
        searchQuery = newQuery
    }

    // Cuando el usuario pulsa "Buscar"
    fun onSearchClick() {
        if (searchQuery.isBlank()) return

        // Lanzamos una corrutina (hilo secundario) para no congelar la app
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                // Llamamos al repositorio (que llama a la API de Google)
                val results = repository.searchRemoteBooks(searchQuery)
                searchResults = results
            } catch (e: Exception) {
                errorMessage = "Error al buscar: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    var selectedBook by mutableStateOf<BookItem?>(null)
        private set

    fun onBookClick(book: BookItem) {
        selectedBook = book
    }

    // Función para guardar el libro en la Base de Datos
    fun saveSelectedBook() {
        val bookToSave = selectedBook ?: return
        viewModelScope.launch {
            try {
                // Mapeo manual rápido si el impo   rt falla por ahora:
                val info = bookToSave.volumeInfo
                val entity = com.roberto.goodbooks.db.Book(
                    gid = bookToSave.id,
                    title = info.title,
                    subtitle = info.subtitle,
                    authors = info.authors?.joinToString(", "),
                    description = info.description,
                    pageCount = info.pageCount?.toLong(),
                    thumbnailUrl = info.imageLinks?.thumbnail?.replace("http:", "https:"),
                    isbn10 = null,
                    isbn13 = null,
                    status = "PENDING",
                    startDate = null,
                    endDate = null,
                    rating = null,
                    notes = null
                )
                repository.insertBook(entity)
                // Opcional: Mostrar mensaje de éxito
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}