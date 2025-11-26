package com.roberto.goodbooks.domain

import com.roberto.goodbooks.db.Book
import com.roberto.goodbooks.network.models.BookItem

// Función de extensión para convertir un BookItem (API) a un Book (Base de Datos)
fun BookItem.toDatabaseEntity(): Book {
    val info = this.volumeInfo
    return Book(
        gid = this.id,
        title = info.title,
        subtitle = info.subtitle,
        // Convertimos la lista de autores a un solo String separado por comas
        authors = info.authors?.joinToString(", "),
        description = info.description,
        pageCount = info.pageCount?.toLong(),
        thumbnailUrl = info.imageLinks?.thumbnail?.replace("http:", "https:"), // Aseguramos HTTPS
        isbn10 = info.industryIdentifiers?.find { it.type == "ISBN_10" }?.identifier,
        isbn13 = info.industryIdentifiers?.find { it.type == "ISBN_13" }?.identifier,
        status = "PENDING", // Por defecto, lo guardamos como "Para Leer"
        startDate = null,
        endDate = null,
        rating = null,
        notes = null
    )
}