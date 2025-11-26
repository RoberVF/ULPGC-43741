package com.roberto.goodbooks.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun FilterDialog(
    currentMinPages: String,
    currentMaxPages: String,
    currentStartDate: String,
    currentEndDate: String,
    onApply: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit,
    onClear: () -> Unit
) {
    var minPages by remember { mutableStateOf(currentMinPages) }
    var maxPages by remember { mutableStateOf(currentMaxPages) }
    var startDate by remember { mutableStateOf(currentStartDate) }
    var endDate by remember { mutableStateOf(currentEndDate) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Filtros Avanzados", style = MaterialTheme.typography.headlineSmall)

                // --- FILTRO PÁGINAS ---
                Text("Por número de páginas:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minPages,
                        onValueChange = { if (it.all { c -> c.isDigit() }) minPages = it },
                        label = { Text("Mín") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = maxPages,
                        onValueChange = { if (it.all { c -> c.isDigit() }) maxPages = it },
                        label = { Text("Máx") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                HorizontalDivider()

                // --- FILTRO FECHAS (NUEVO) ---
                Text("Por fecha de fin (YYYY-MM-DD):", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Desde") },
                        placeholder = { Text("2024-01-01") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("Hasta") },
                        placeholder = { Text("2024-12-31") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // --- BOTONES ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        onClear()
                        onDismiss()
                    }) {
                        Text("Limpiar Todo")
                    }

                    Button(onClick = {
                        onApply(minPages, maxPages, startDate, endDate)
                        onDismiss()
                    }) {
                        Text("Aplicar")
                    }
                }
            }
        }
    }
}