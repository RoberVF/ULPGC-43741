package com.roberto.goodbooks.network.models

import kotlinx.serialization.Serializable

// 1. La respuesta general de Google (una lista de items)
@Serializable
data class GoogleBooksApiResponse(
    val items: List<BookItem>? = null,
    val totalItems: Int? = 0
)

// 2. Cada libro individual en la lista
@Serializable
data class BookItem(
    val id: String, // El ID único de Google (ej. "zyTCAlFPjgYC")
    val volumeInfo: VolumeInfo
)

// 3. Los detalles del libro (Título, autor, etc.)
@Serializable
data class VolumeInfo(
    val title: String,
    val subtitle: String? = null,
    val authors: List<String>? = null,
    val description: String? = null,
    val pageCount: Int? = null,
    val imageLinks: ImageLinks? = null,

    // Identificadores de industria (ISBN)
    val industryIdentifiers: List<IndustryIdentifier>? = null
)

// 4. Los enlaces a las imágenes de portada
@Serializable
data class ImageLinks(
    val smallThumbnail: String? = null,
    val thumbnail: String? = null
)

// 5. Para leer el ISBN-10 y ISBN-13
@Serializable
data class IndustryIdentifier(
    val type: String, // "ISBN_10" o "ISBN_13"
    val identifier: String
)