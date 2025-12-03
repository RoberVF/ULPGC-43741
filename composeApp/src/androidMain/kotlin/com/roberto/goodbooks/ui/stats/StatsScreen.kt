package com.roberto.goodbooks.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pages
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadStats()
    }

    var showGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Mis Estadísticas") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // 1. RETO ANUAL (Ahora clicable)
            ReadingChallengeCard(
                read = viewModel.booksReadThisYear,
                goal = viewModel.yearlyGoal,
                onClick = { showGoalDialog = true } // Al pulsar, abrimos diálogo
            )

            // 2. RESUMEN NUMÉRICO
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    title = "Total Leídos",
                    value = "${viewModel.booksRead}",
                    icon = Icons.Default.MenuBook,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Páginas",
                    value = "${viewModel.totalPagesRead}",
                    icon = Icons.Default.Pages,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            // 3. TARJETA DE RÉCORDS
            Text("Salón de la Fama", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    HighlightRow(
                        icon = Icons.Default.Person,
                        label = "Autor más leído",
                        value = viewModel.topAuthor?.first ?: "-",
                        subValue = if (viewModel.topAuthor != null) "${viewModel.topAuthor!!.second} libros" else ""
                    )
                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                    HighlightRow(
                        icon = Icons.Default.Star,
                        label = "Nota Media",
                        value = "${viewModel.averageRating}/10",
                        subValue = "Basado en libros puntuados"
                    )
                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                    HighlightRow(
                        icon = Icons.Default.VerticalAlignTop,
                        label = "El más largo",
                        value = viewModel.longestBook?.title ?: "-",
                        subValue = "${viewModel.longestBook?.pageCount ?: 0} págs"
                    )

                    HighlightRow(
                        icon = Icons.Default.VerticalAlignBottom,
                        label = "El más corto",
                        value = viewModel.shortestBook?.title ?: "-",
                        subValue = "${viewModel.shortestBook?.pageCount ?: 0} págs"
                    )
                }
            }

            // 4. HÁBITOS DE LECTURA
            Text("Tus Hábitos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.weight(1f).height(120.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(Icons.Default.Speed, null)
                        Column {
                            Text("Velocidad Media", style = MaterialTheme.typography.labelMedium)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("${viewModel.averageDaysPerBook}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                                Text(" días/libro", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))
                            }
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.weight(1f).height(120.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(Icons.Default.AccessTime, null)
                        Column {
                            Text("Leyendo Ahora", style = MaterialTheme.typography.labelMedium)
                            Text("${viewModel.booksReading}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // --- DIÁLOGO PARA EDITAR META ---
    if (showGoalDialog) {
        var newGoalText by remember { mutableStateOf(viewModel.yearlyGoal.toString()) }

        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            icon = { Icon(Icons.Default.EmojiEvents, null) },
            title = { Text("Editar Reto Anual") },
            text = {
                Column {
                    Text("¿Cuántos libros quieres leer este año?")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newGoalText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) newGoalText = it },
                        label = { Text("Número de libros") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val goal = newGoalText.toIntOrNull()
                    if (goal != null && goal > 0) {
                        viewModel.updateYearlyGoal(goal)
                    }
                    showGoalDialog = false
                }) {
                    Text("GUARDAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("CANCELAR")
                }
            }
        )
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun ReadingChallengeCard(read: Int, goal: Int, onClick: () -> Unit) {
    val progress = (read.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth().clickable { onClick() } // Ahora es clicable
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Reto ${LocalDate.now().year}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Has leído $read de $goal libros", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Icon(Icons.Default.EmojiEvents, null, modifier = Modifier.size(48.dp), tint = Color.White)
        }
    }
}

@Composable
fun HighlightRow(icon: ImageVector, label: String, value: String, subValue: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (subValue.isNotBlank()) {
                Text(subValue, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier.height(110.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, null, modifier = Modifier.size(24.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelMedium)
                Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}