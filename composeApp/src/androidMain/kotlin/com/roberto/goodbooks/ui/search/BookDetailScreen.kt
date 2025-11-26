package com.roberto.goodbooks.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage // Aquí usamos Coil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    viewModel: SearchViewModel,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val book = viewModel.selectedBook

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
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.saveSelectedBook()
                    onSaveClick() // Volver a la librería o mostrar confirmación
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("GUARDAR EN BIBLIOTECA")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Para poder hacer scroll
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

            // Espacio extra al final para que el botón flotante no tape texto
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}