package com.roberto.goodbooks.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.roberto.goodbooks.network.models.BookItem
import androidx.compose.material.icons.filled.Add

@Composable
fun SearchScreen(
    // Inyectamos el ViewModel automáticamente
    viewModel: SearchViewModel,
    onBookClick: (BookItem) -> Unit,
    onManualClick: () -> Unit
) {
    // Scaffold nos da la estructura básica (barra superior, fondo, etc.)
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("GoodBooks: Buscar") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onManualClick) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Manual")
            }
        }
    ) { paddingValues ->

        // Column organiza los elementos verticalmente (uno debajo de otro)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 1. BARRA DE BÚSQUEDA
            SearchBar(
                query = viewModel.searchQuery,
                onQueryChange = viewModel::onQueryChange,
                onSearch = viewModel::onSearchClick,
                isLoading = viewModel.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. MENSAJE DE ERROR (Si lo hay)
            if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // 3. LISTA DE RESULTADOS
            // LazyColumn es la forma eficiente de mostrar listas en Compose
            LazyColumn {
                items(viewModel.searchResults) { book ->
                    // Pasamos el click hacia arriba
                    BookItemRow(book, onClick = { onBookClick(book) })
                    Divider()
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    isLoading: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Título o autor") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        Button(onClick = onSearch, enabled = !isLoading) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Default.Search, contentDescription = "Buscar")
            }
        }
    }
}

@Composable
fun BookItemRow(book: BookItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        // Título del libro
        Text(
            text = book.volumeInfo.title,
            style = MaterialTheme.typography.titleMedium
        )

        // Autor (si existe)
        book.volumeInfo.authors?.let { authors ->
            Text(
                text = authors.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Número de páginas (si existe)
        book.volumeInfo.pageCount?.let { pages ->
            Text(
                text = "$pages páginas",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Esta función es necesaria porque TopAppBar en Material3 es experimental
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(title: @Composable () -> Unit) {
    CenterAlignedTopAppBar(title = title)
}