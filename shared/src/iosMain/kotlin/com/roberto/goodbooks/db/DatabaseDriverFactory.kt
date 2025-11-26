package com.roberto.goodbooks.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.roberto.goodbooks.db.GoodBooksDatabase

actual class DatabaseDriverFactory {

    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(GoodBooksDatabase.Schema, "GoodBooks.db")
    }
}