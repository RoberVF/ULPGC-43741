package com.roberto.goodbooks.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.roberto.goodbooks.network.models.BookItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    book: BookItem?,
    isLibraryMode: Boolean,
    onBackClick: () -> Unit,
    onFabClick: () -> Unit,
    onFinishClick: (Int) -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    if (book == null) {
        onBackClick()
        return
    }

    // Estado para mostrar/ocultar el diálogo de poner nota
    var showRatingDialog by remember { mutableStateOf(false) }

    // Estado actual del libro (PENDING, IN_PROGRESS, COMPLETED)
    val status = book.volumeInfo.myStatus ?: "PENDING"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isLibraryMode) "Mi Libro" else "Detalles del Libro") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Solo mostramos la papelera si el libro ya es nuestro
                    if (isLibraryMode) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Borrar libro",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isLibraryMode) {
                // MODO BÚSQUEDA: Botón Guardar
                ExtendedFloatingActionButton(
                    onClick = onFabClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("GUARDAR") }
                )
            } else {
                // MODO BIBLIOTECA: Botón según estado
                when (status) {
                    "PENDING" -> {
                        ExtendedFloatingActionButton(
                            onClick = onFabClick, // Esto llamará a startReading
                            containerColor = MaterialTheme.colorScheme.primary,
                            icon = { Icon(Icons.Default.PlayArrow, null) },
                            text = { Text("EMPEZAR LECTURA") }
                        )
                    }
                    "IN_PROGRESS" -> {
                        ExtendedFloatingActionButton(
                            onClick = { showRatingDialog = true }, // Abrimos diálogo
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            icon = { Icon(Icons.Default.Check, null) },
                            text = { Text("TERMINAR") }
                        )
                    }
                    "COMPLETED" -> {
                        // Si ya está acabado, no mostramos botón
                    }
                }
            }
        }
    ) { padding ->

        // --- CONTENIDO PRINCIPAL (UNA SOLA COLUMNA) ---
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // 1. TARJETA DE PROGRESO (Solo si es mi libro)
            if (isLibraryMode) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tu Progreso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        DetailRow("Estado:", when(status) {
                            "PENDING" -> "Para Leer"
                            "IN_PROGRESS" -> "Leyendo ahora..."
                            "COMPLETED" -> "¡Terminado!"
                            else -> status
                        })

                        if (status == "IN_PROGRESS") {
                            DetailRow("Empezado el:", book.volumeInfo.myStartDate ?: "-")
                        }

                        if (status == "COMPLETED") {
                            DetailRow("Leído del:", "${book.volumeInfo.myStartDate} al ${book.volumeInfo.myEndDate}")

                            // Mostrar Estrellas
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Tu Valoración:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(100.dp)
                                )
                                val rating = book.volumeInfo.myRating ?: 0
                                Text(
                                    "$rating/10 ",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // 2. PORTADA
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
                // Placeholder
                Box(
                    modifier = Modifier
                        .height(250.dp)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sin Portada", style = MaterialTheme.typography.headlineMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. TÍTULO Y AUTOR
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
                SuggestionChip(onClick = {}, label = { Text("En tu biblioteca") })
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. DESCRIPCIÓN
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

            // 5. FICHA TÉCNICA
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

            // Espacio final para el botón flotante
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // --- DIÁLOGO DE VALORACIÓN ---
    if (showRatingDialog) {
        var currentRating by remember { mutableStateOf(5f) }

        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = { Text("¡Libro Terminado!") },
            text = {
                Column {
                    Text("¿Qué nota le pones? (0-10)")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "${currentRating.toInt()}",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Slider(
                        value = currentRating,
                        onValueChange = { currentRating = it },
                        valueRange = 0f..10f,
                        steps = 9
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showRatingDialog = false
                    onFinishClick(currentRating.toInt())
                }) {
                    Text("GUARDAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) {
                    Text("CANCELAR")
                }
            }
        )
    }
}

// Componente auxiliar para las filas de detalles
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