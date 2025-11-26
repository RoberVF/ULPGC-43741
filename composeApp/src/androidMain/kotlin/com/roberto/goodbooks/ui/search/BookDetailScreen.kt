package com.roberto.goodbooks.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.roberto.goodbooks.network.models.BookItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    book: BookItem?,
    isLibraryMode: Boolean,
    onBackClick: () -> Unit,
    onFabClick: () -> Unit
) {

    if (book == null) {
        // Si no hay libro seleccionado, volvemos atrás
        onBackClick()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Libro") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            // El botón cambia según el modo
            ExtendedFloatingActionButton(
                onClick = { onFabClick() },
                containerColor = MaterialTheme.colorScheme.primary,
                icon = {
                    Icon(
                        if (isLibraryMode) Icons.Default.PlayArrow else Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = {
                    Text(if (isLibraryMode) "EMPEZAR LECTURA" else "GUARDAR EN BIBLIOTECA")
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 1. PORTADA
            // Usamos AsyncImage de Coil para cargar la URL
            val imageUrl = book.volumeInfo.imageLinks?.thumbnail?.replace("http:", "https:")

            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Portada del libro",
                    modifier = Modifier
                        .height(250.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            } else {
                // Placeholder si no hay imagen
                Box(
                    modifier = Modifier
                        .height(250.dp)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("Sin Portada", style = MaterialTheme.typography.headlineMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. TÍTULO Y AUTOR
            Text(
                text = book.volumeInfo.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            book.volumeInfo.authors?.let { authors ->
                Text(
                    text = authors.joinToString(", "),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (isLibraryMode) {
                Spacer(modifier = Modifier.height(8.dp))
                SuggestionChip(onClick = {}, label = { Text("Estado: En tu biblioteca") })
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. DESCRIPCIÓN
            Text(
                text = "Sinopsis",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = book.volumeInfo.description ?: "No hay descripción disponible.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // 4. Ficha Tecnica
            Text(
                text = "Ficha Técnica",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Páginas
            book.volumeInfo.pageCount?.let { pages ->
                DetailRow("Páginas:", "$pages")
            }
            // ISBNs
            book.volumeInfo.industryIdentifiers?.forEach { identifier ->
                val label = if (identifier.type == "ISBN_10") "ISBN-10:" else "ISBN-13:"
                DetailRow(label, identifier.identifier)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}