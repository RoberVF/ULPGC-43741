package com.roberto.goodbooks.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.roberto.goodbooks.ui.library.LibraryScreen
import com.roberto.goodbooks.ui.library.LibraryViewModel
import com.roberto.goodbooks.ui.recommendations.RecommendationsScreen
import com.roberto.goodbooks.ui.search.BookDetailScreen
import com.roberto.goodbooks.ui.search.SearchScreen
import com.roberto.goodbooks.ui.search.SearchViewModel
import com.roberto.goodbooks.ui.stats.StatsScreen

// 1. Definimos las rutas de nuestras pantallas
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Library : Screen("library", "Biblioteca", Icons.Filled.Home)
    object Search : Screen("search", "Buscar", Icons.Filled.Search)
    object Stats : Screen("stats", "Estadísticas", Icons.AutoMirrored.Filled.List)
    object Recommendations : Screen("recommendations", "Para ti", Icons.Filled.Star)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val searchViewModel: SearchViewModel = viewModel()
    val libraryViewModel: LibraryViewModel = viewModel()

    // Lista de pantallas para la barra inferior
    val items = listOf(
        Screen.Library,
        Screen.Search,
        Screen.Stats,
        Screen.Recommendations
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Esto evita que se acumulen pantallas si pulsas muchas veces
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Aquí es donde se intercambian las pantallas
        NavHost(
            navController = navController,
            startDestination = Screen.Library.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Library.route) {
                LibraryScreen(
                    viewModel = libraryViewModel,
                    // Cuando pulsamos un libro...
                    onBookClick = { book ->
                        libraryViewModel.onBookClick(book) // 1. Guardamos cuál es
                        navController.navigate("library_detail") // 2. Navegamos
                    }
                )
            }

            // Detalle desde biblioteca
            composable("library_detail") {
                BookDetailScreen(
                    book = libraryViewModel.selectedBook, // Le pasamos el libro de la biblioteca
                    isLibraryMode = true,
                    onBackClick = { navController.popBackStack() },
                    onFabClick = { /* Logica para empezar a leer */ },
                )
            }

            // Pantalla de Búsqueda
            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = searchViewModel,
                    onBookClick = { book ->
                        searchViewModel.onBookClick(book)
                        navController.navigate("search_detail") // Cambiamos el nombre de la ruta para diferenciar
                    }
                )
            }

            // Detalle desde busqueda
            composable("search_detail") {
                BookDetailScreen(
                    book = searchViewModel.selectedBook, // Le pasamos el libro de la búsqueda
                    isLibraryMode = false,
                    onBackClick = { navController.popBackStack() },
                    onFabClick = {
                        searchViewModel.saveSelectedBook()
                        navController.popBackStack()
                        // Opcional: Ir a la biblioteca
                        // navController.navigate(Screen.Library.route)
                    }
                )
            }

            composable(Screen.Stats.route) { StatsScreen() }
            composable(Screen.Recommendations.route) { RecommendationsScreen() }
        }

    }
}