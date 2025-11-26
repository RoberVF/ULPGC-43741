package com.roberto.goodbooks.network

import com.roberto.goodbooks.network.models.GoogleBooksApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class GoogleBooksApiClient {

    // 1. Configuramos el cliente HTTP (el "navegador")
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                // Importante: Ignorar campos que no usemos para que no falle
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    private val BASE_URL = "https://www.googleapis.com/books/v1/volumes"

    /**
     * Busca libros por texto (título, autor, etc.)
     */
    suspend fun searchBooks(query: String): GoogleBooksApiResponse {
        // Ejemplo de llamada: https://www.googleapis.com/books/v1/volumes?q=Harry+Potter
        // Usamos try-catch por si falla la conexión
        return try {
            val response = httpClient.get(BASE_URL) {
                url {
                    parameters.append("q", query)
                    parameters.append("maxResults", "20") // Traer 20 resultados
                    parameters.append("printType", "books")
                }
            }
            response.body()
        } catch (e: Exception) {
            e.printStackTrace()
            // Si falla, devolvemos un objeto vacío para no romper la app
            GoogleBooksApiResponse(items = emptyList())
        }
    }
}