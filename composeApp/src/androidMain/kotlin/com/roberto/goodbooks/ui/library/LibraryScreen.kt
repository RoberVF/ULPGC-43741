package com.roberto.goodbooks.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.roberto.goodbooks.db.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(),
    onBookClick: (Book) -> Unit = {} // Para el futuro
) {
    // Esto hace que la lista se recargue cada vez que entras en esta pantalla
    LaunchedEffect(Unit) {
        viewModel.loadLibrary()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Mi Biblioteca") })
        }
    ) { padding ->

        if (viewModel.books.isEmpty()) {
            // Si no hay libros, mostramos mensaje
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Tu biblioteca está vacía.\n¡Ve a buscar libros!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            // Si hay libros, mostramos la lista
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                items(viewModel.books) { book ->
                    SavedBookItem(
                        book = book,
                        onClick = { onBookClick(book) }
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
fun SavedBookItem(book: Book, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Portada pequeña
        if (book.thumbnailUrl != null) {
            AsyncImage(
                model = book.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.width(60.dp).height(90.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.width(60.dp).height(90.dp).padding(4.dp)) {
                Text("Sin img")
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Textos
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            book.authors?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
            // Chip de estado (Pendiente/Leído)
            SuggestionChip(
                onClick = { },
                label = { Text(if (book.status == "PENDING") "Para Leer" else book.status) }
            )
        }
    }
}