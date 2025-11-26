package com.roberto.goodbooks.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.roberto.goodbooks.db.GoodBooksDatabase

/**
 * Esta es la implementación "real" (`actual`) para la plataforma Android.
 * Cumple la "promesa" (`expect`) que hicimos en commonMain.
 *
 * Fíjate como esta clase SÍ necesita un 'Context' de Android.
 * Esto es algo que SOLO Android tiene, por eso este código
 * vive en 'androidMain'.
 */
actual class DatabaseDriverFactory(private val context: Context) {
    /**
     * Esta es la función que prometimos implementar.
     * En Android, usamos 'AndroidSqliteDriver'.
     */
    actual fun createDriver(): SqlDriver {
        // 'GoodBooksDatabase.Schema' es la clase que SQLDelight generó
        // automáticamente a partir de tu archivo GoodBooks.sq.
        return AndroidSqliteDriver(GoodBooksDatabase.Schema, context, "GoodBooks.db")
    }
}