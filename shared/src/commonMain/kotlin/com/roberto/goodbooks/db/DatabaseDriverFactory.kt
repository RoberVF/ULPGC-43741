package com.roberto.goodbooks.db

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    /**
     * Esta funcion es la que Android e iOS deberan implementar.
     * Creara el driver de base de datos especifico para cada plataforma.
     */
    fun createDriver(): SqlDriver
}