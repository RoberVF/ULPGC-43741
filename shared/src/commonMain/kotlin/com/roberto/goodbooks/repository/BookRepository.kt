package com.roberto.goodbooks.repository

import com.roberto.goodbooks.db.Book
import com.roberto.goodbooks.db.DatabaseDriverFactory
import com.roberto.goodbooks.db.GoodBooksDatabase
import com.roberto.goodbooks.network.GoogleBooksApiClient
import com.roberto.goodbooks.network.models.BookItem

class BookRepository(driverFactory: DatabaseDriverFactory) {

    // 1. Inicializamos la base de datos local
    private val database = GoodBooksDatabase(driverFactory.createDriver())
    private val dbQueries = database.goodBooksQueries

    // 2. Inicializamos el cliente de API (NUEVO)
    // Esto nos permite conectarnos a Internet desde aquí
    private val apiClient = GoogleBooksApiClient()

    /**
     * --- FUNCIONES DE BASE DE DATOS LOCAL (OFFLINE) ---
     */

    // Obtener todos los libros guardados en el móvil
    fun getAllBooks(): List<Book> {
        return dbQueries.getAllBooks().executeAsList()
    }

    // Guardar un libro en el móvil
    fun insertBook(book: Book) {
        dbQueries.insertBook(
            gid = book.gid,
            title = book.title,
            subtitle = book.subtitle,
            authors = book.authors,
            description = book.description,
            pageCount = book.pageCount,
            thumbnailUrl = book.thumbnailUrl,
            isbn10 = book.isbn10,
            isbn13 = book.isbn13,
            status = book.status,
            startDate = book.startDate,
            endDate = book.endDate,
            rating = book.rating,
            notes = book.notes
        )
    }

    /**
     * --- FUNCIONES DE API REMOTA (ONLINE) ---
     * Esta función llama a internet para buscar libros nuevos.
     * Es 'suspend' porque la red puede tardar y no queremos congelar la app.
     */
    suspend fun searchRemoteBooks(query: String): List<BookItem> {
        val response = apiClient.searchBooks(query)
        // Devolvemos la lista de items, o una lista vacía si es null
        return response.items ?: emptyList()
    }
}