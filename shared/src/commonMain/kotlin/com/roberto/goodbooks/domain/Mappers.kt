package com.roberto.goodbooks.domain

import com.roberto.goodbooks.db.Book
import com.roberto.goodbooks.network.models.BookItem
import com.roberto.goodbooks.network.models.VolumeInfo
import com.roberto.goodbooks.network.models.ImageLinks
import com.roberto.goodbooks.network.models.IndustryIdentifier

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

// Convierte de Base de Datos a Formato Visual
fun Book.toBookItem(): BookItem {
    // Reconstruimos la lista de ISBNs si existen en la base de datos
    val identifiers = mutableListOf<IndustryIdentifier>()
    if (this.isbn10 != null) {
        identifiers.add(IndustryIdentifier("ISBN_10", this.isbn10))
    }
    if (this.isbn13 != null) {
        identifiers.add(IndustryIdentifier("ISBN_13", this.isbn13))
    }

    return BookItem(
        id = this.gid,
        volumeInfo = VolumeInfo(
            title = this.title,
            subtitle = this.subtitle,
            // Convertimos el string "Autor1, Autor2" de vuelta a una lista
            authors = this.authors?.split(", "),
            description = this.description,
            pageCount = this.pageCount?.toInt(),
            imageLinks = ImageLinks(thumbnail = this.thumbnailUrl),
            industryIdentifiers = identifiers.ifEmpty { null }, // No necesitamos esto para mostrar detalles
            myStatus = this.status,
            myRating = this.rating?.toInt(),
            myStartDate = this.startDate,
            myEndDate = this.endDate,
        )
    )
}