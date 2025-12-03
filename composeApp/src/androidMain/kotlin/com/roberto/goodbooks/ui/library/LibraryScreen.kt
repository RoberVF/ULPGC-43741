package com.roberto.goodbooks.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.roberto.goodbooks.db.Book
import com.roberto.goodbooks.db.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(),
    onBookClick: (Book) -> Unit = {},
    onShelfClick: (Long) -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.loadLibrary()
    }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Mis Libros", "Estanterías")

    var showCreateShelfDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var shelfToDelete by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            Column {
                if (showSearch) {
                    SearchBarTop(
                        query = viewModel.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged,
                        onClose = {
                            showSearch = false
                            viewModel.onSearchQueryChanged("")
                        }
                    )
                } else {
                    CenterAlignedTopAppBar(
                        title = { Text("Mi Biblioteca") },
                        actions = {
                            if (selectedTabIndex == 0) { // Solo mostrar búsqueda/filtros en "Mis Libros"
                                IconButton(onClick = { showSearch = true }) {
                                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                                }
                                IconButton(onClick = { showFilterDialog = true }) {
                                    Icon(Icons.Default.FilterList, contentDescription = "Filtros")
                                }
                            }
                        }
                    )
                }

                // --- PESTAÑAS (TABS) ---
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            // FAB para crear estantería (Solo en tab Estanterías)
            if (selectedTabIndex == 1) {
                FloatingActionButton(onClick = { showCreateShelfDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Estantería")
                }
            }
        }
    ) { padding ->

        if (selectedTabIndex == 0) {
            // --- TAB 1: LISTA DE LIBROS (Lo que ya tenías) ---
            BooksListContent(
                padding = padding,
                viewModel = viewModel,
                onBookClick = onBookClick
            )
        } else {
            // --- TAB 2: GRID DE ESTANTERÍAS (Nuevo) ---
            ShelvesGridContent(
                padding = padding,
                shelves = viewModel.shelves,
                onShelfClick = { shelf -> onShelfClick(shelf.id) },
                onDeleteShelf = { shelfId ->
                    val shelf = viewModel.shelves.find { it.first.id == shelfId }?.first
                    shelfToDelete = shelf
                }
            )
        }
    }

    // --- DIÁLOGOS ---
    if (showCreateShelfDialog) {
        CreateShelfDialog(
            onDismiss = { showCreateShelfDialog = false },
            onCreate = { name, desc, color -> viewModel.createShelf(name, desc, color) }
        )
    }

    if (showFilterDialog) {
        FilterDialog(
            currentMinPages = viewModel.minPagesFilter,
            currentMaxPages = viewModel.maxPagesFilter,
            currentStartDate = viewModel.startDateFilter,
            currentEndDate = viewModel.endDateFilter,
            onApply = { min, max, start, end -> viewModel.onAdvancedFiltersChanged(min, max, start, end) },
            onDismiss = { showFilterDialog = false },
            onClear = { viewModel.clearAllFilters() }
        )
    }

    if (shelfToDelete != null) {
        DeleteConfirmationDialog(
            title = "Eliminar Estantería",
            message = "¿Seguro que quieres borrar '${shelfToDelete?.name}'?\nLos libros no se borrarán, solo la colección.",
            onConfirm = {
                // Borramos de verdad
                shelfToDelete?.let { viewModel.deleteShelf(it.id) }
                shelfToDelete = null // Cerramos diálogo
            },
            onDismiss = {
                shelfToDelete = null // Cerramos sin borrar
            }
        )
    }
}

// --- SUB-COMPONENTES PARA ORGANIZAR ---

@Composable
fun BooksListContent(padding: PaddingValues, viewModel: LibraryViewModel, onBookClick: (Book) -> Unit) {
    Column(modifier = Modifier.padding(padding).fillMaxSize()) {
        // Filtros Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = viewModel.statusFilter == null,
                    onClick = { viewModel.onStatusFilterChanged(null) },
                    label = { Text("Todos") },
                    leadingIcon = if (viewModel.statusFilter == null) { { Icon(Icons.Default.Done, null, modifier = Modifier.size(18.dp)) } } else null
                )
            }
            item { StatusFilterChip("PENDING", "Pendientes", viewModel) }
            item { StatusFilterChip("IN_PROGRESS", "Leyendo", viewModel) }
            item { StatusFilterChip("COMPLETED", "Terminados", viewModel) }
        }
        HorizontalDivider()

        if (viewModel.books.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay libros aquí.", color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(viewModel.books) { book ->
                    SavedBookItem(book = book, shelves = viewModel.bookShelves[book.gid] ?: emptyList(), onClick = { onBookClick(book) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun ShelvesGridContent(
    padding: PaddingValues,
    shelves: List<Pair<Category, Long>>, // Recibimos (Categoria, Conteo)
    onShelfClick: (Category) -> Unit,
    onDeleteShelf: (Long) -> Unit
) {
    if (shelves.isEmpty()) {
        Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No tienes estanterías.\n¡Crea una con el botón +!", style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2 columnas
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            items(shelves) { (shelf, count) ->
                ShelfCard(shelf, count, onClick = { onShelfClick(shelf) }, onDelete = { onDeleteShelf(shelf.id) })
            }
        }
    }
}

@Composable
fun ShelfCard(shelf: Category, count: Long, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.aspectRatio(1f).clickable { onClick() }, // Tarjeta cuadrada
        colors = CardDefaults.cardColors(containerColor = Color(shelf.colorHex.toInt()).copy(alpha = 0.2f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Icono Borrar (Esquina superior derecha)
            IconButton(onClick = onDelete, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(shelf.colorHex.toInt())
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = shelf.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$count libros",
                    style = MaterialTheme.typography.bodySmall
                )
            }
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
fun SavedBookItem(book: Book, shelves: List<Category>, onClick: () -> Unit) {
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

            Spacer(modifier = Modifier.height(4.dp))


            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Estado de lectura
                item {
                    val (label, color) = when (book.status) {
                        "IN_PROGRESS" -> "Leyendo" to MaterialTheme.colorScheme.tertiaryContainer
                        "COMPLETED" -> "Terminado" to MaterialTheme.colorScheme.secondaryContainer
                        else -> "Pendiente" to MaterialTheme.colorScheme.surfaceVariant
                    }

                    Surface(
                        color = color,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Estanteria al que pertenece
                items(shelves) { shelf ->
                    Surface(
                        color = Color(shelf.colorHex.toInt()).copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.extraSmall,

                    ) {
                        Text(
                            text = shelf.name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}