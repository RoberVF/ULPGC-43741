package com.roberto.goodbooks

import android.app.Application
import com.roberto.goodbooks.db.DatabaseDriverFactory
import com.roberto.goodbooks.repository.BookRepository

class GoodBooksApp : Application() {

    // Esta variable guardará nuestro repositorio
    lateinit var bookRepository: BookRepository

    override fun onCreate() {
        super.onCreate()

        // 1. Creamos el Driver de Base de Datos
        val driverFactory = DatabaseDriverFactory(context = this)

        // 2. Iniciamos el Repositorio
        bookRepository = BookRepository(driverFactory)
    }
}