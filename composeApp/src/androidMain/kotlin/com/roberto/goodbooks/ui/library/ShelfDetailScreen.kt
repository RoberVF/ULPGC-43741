package com.roberto.goodbooks.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.roberto.goodbooks.db.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelfDetailScreen(
    shelfId: Long,
    onBackClick: () -> Unit,
    onBookClick: (Book) -> Unit,
    viewModel: ShelfDetailViewModel = viewModel()
) {
    // Cargar datos al entrar
    LaunchedEffect(shelfId) {
        viewModel.loadShelf(shelfId)
    }

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.shelf?.name ?: "Estantería") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = viewModel.shelf?.let { Color(it.colorHex.toInt()).copy(alpha = 0.1f) }
                        ?: MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.loadAvailableBooks()
                    showAddDialog = true
                },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Añadir Libro") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Descripción
            viewModel.shelf?.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Text(
                        text = desc,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Divider()
                }
            }

            if (viewModel.booksInShelf.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Esta estantería está vacía.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn {
                    items(viewModel.booksInShelf) { book ->
                        BookInShelfItem(
                            book = book,
                            onClick = { onBookClick(book) },
                            onRemove = { viewModel.removeBookFromShelf(book) }
                        )
                        Divider()
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddBookToShelfDialog(
            availableBooks = viewModel.availableBooks,
            onDismiss = { showAddDialog = false },
            onBookSelected = { book ->
                viewModel.addBookToShelf(book)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun BookInShelfItem(book: Book, onClick: () -> Unit, onRemove: () -> Unit) {
    ListItem(
        headlineContent = { Text(book.title, maxLines = 1) },
        supportingContent = { book.authors?.let { Text(it, maxLines = 1) } },
        trailingContent = {
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Sacar de estantería")
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}