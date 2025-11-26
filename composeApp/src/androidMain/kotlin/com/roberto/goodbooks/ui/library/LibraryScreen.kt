package com.roberto.goodbooks.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.roberto.goodbooks.db.Book
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(),
    onBookClick: (Book) -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.loadLibrary()
    }

    var showSearch by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // Barra superior dinámica: Puede ser el título o un campo de búsqueda
            if (showSearch) {
                SearchBarTop(
                    query = viewModel.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onClose = {
                        showSearch = false
                        viewModel.onSearchQueryChanged("") // Limpiar filtro al cerrar
                    }
                )
            } else {
                CenterAlignedTopAppBar(
                    title = { Text("Mi Biblioteca") },
                    actions = {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar en biblioteca")
                        }
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filtros",
                                tint = if (viewModel.minPagesFilter.isNotEmpty() || viewModel.maxPagesFilter.isNotEmpty())
                                    MaterialTheme.colorScheme.primary
                                else
                                    LocalContentColor.current
                            )
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // 1. FILTROS DE ESTADO (CHIPS)
            // Usamos LazyRow para que se pueda hacer scroll horizontal si no caben
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = viewModel.statusFilter == null,
                        onClick = { viewModel.onStatusFilterChanged(null) },
                        label = { Text("Todos") },
                        leadingIcon = if (viewModel.statusFilter == null) {
                            { Icon(Icons.Default.Done, null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
                item {
                    StatusFilterChip("PENDING", "Pendientes", viewModel)
                }
                item {
                    StatusFilterChip("IN_PROGRESS", "Leyendo", viewModel)
                }
                item {
                    StatusFilterChip("COMPLETED", "Terminados", viewModel)
                }
            }

            Divider()

            // 2. LISTA DE LIBROS
            if (viewModel.books.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (viewModel.statusFilter != null || viewModel.searchQuery.isNotEmpty())
                            "No hay libros con estos filtros."
                        else
                            "Tu biblioteca está vacía.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(viewModel.books) { book ->
                        SavedBookItem(book, onClick = { onBookClick(book) })
                        Divider()
                    }
                }
            }
        }
        if (showFilterDialog) {
            FilterDialog(
                currentMinPages = viewModel.minPagesFilter,
                currentMaxPages = viewModel.maxPagesFilter,
                currentStartDate = viewModel.startDateFilter,
                currentEndDate = viewModel.endDateFilter,

                onApply = { min, max, start, end ->
                    viewModel.onAdvancedFiltersChanged(min, max, start, end)
                },

                onDismiss = { showFilterDialog = false },
                onClear = { viewModel.clearAllFilters() }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun StatusFilterChip(statusKey: String, label: String, viewModel: LibraryViewModel) {
    val isSelected = viewModel.statusFilter == statusKey
    FilterChip(
        selected = isSelected,
        onClick = { viewModel.onStatusFilterChanged(statusKey) },
        label = { Text(label) },
        leadingIcon = if (isSelected) {
            { Icon(Icons.Default.Done, null, modifier = Modifier.size(18.dp)) }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarTop(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Filtrar por título o autor...") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Clear, contentDescription = "Cerrar búsqueda")
            }
        }
    )
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
        // Portada
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

            // Estado visual
            Spacer(modifier = Modifier.height(4.dp))
            val (label, color) = when(book.status) {
                "IN_PROGRESS" -> "Leyendo" to MaterialTheme.colorScheme.tertiaryContainer
                "COMPLETED" -> "Terminado" to MaterialTheme.colorScheme.secondaryContainer
                else -> "Pendiente" to MaterialTheme.colorScheme.surfaceVariant
            }

            Surface(
                color = color,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}