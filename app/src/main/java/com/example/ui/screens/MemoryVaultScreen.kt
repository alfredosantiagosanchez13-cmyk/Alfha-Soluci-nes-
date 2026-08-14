package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.MemoryNodeEntity
import com.example.ui.components.D3MemoryDashboard
import com.example.ui.components.MemoryNodeCard
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekBorderSubtle
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.ui.theme.SleekVioletDark
import com.example.ui.theme.SleekVioletPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryVaultScreen(
    memories: List<MemoryNodeEntity>,
    onAddMemory: (category: String, title: String, detail: String) -> Unit,
    onDeleteMemory: (MemoryNodeEntity) -> Unit,
    onClearAllMemories: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("ALL", "COMMUNITY", "AMENITY", "PREFERENCE", "DIRECTIVE", "SECURITY", "FACT")

    val filteredMemories = remember(memories, searchQuery, selectedCategory) {
        memories.filter { node ->
            val matchesCategory = (selectedCategory == "ALL" || node.category.equals(selectedCategory, ignoreCase = true))
            val matchesSearch = searchQuery.isBlank() ||
                node.title.contains(searchQuery, ignoreCase = true) ||
                node.detail.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SleekSurfaceVariant)
                    .border(1.dp, SleekBorder, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = SleekTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Buscar en nodos de memoria...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SleekTextMuted,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .semantics { testTag = "search_memory_input" },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = SleekTextPrimary,
                            fontSize = 14.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = SleekVioletPrimary
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter category row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    val displayLabel = when (cat) {
                        "ALL" -> "Todos"
                        "PREFERENCE" -> "Preferencias"
                        "DIRECTIVE" -> "Directivas"
                        "SECURITY" -> "Seguridad"
                        else -> "Hechos"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) SleekVioletPrimary else SleekSurface)
                            .border(
                                1.dp,
                                if (isSelected) SleekVioletPrimary else SleekBorderSubtle,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .semantics {
                                testTag = "category_chip_$cat"
                                contentDescription = displayLabel
                            }
                    ) {
                        Text(
                            text = displayLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SleekVioletDark else SleekTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subheader with clear memories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BÓVEDA DE MEMORIA APRENDIDA (${filteredMemories.size})",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = SleekTextSecondary
                )

                if (memories.isNotEmpty()) {
                    TextButton(
                        onClick = onClearAllMemories,
                        modifier = Modifier.semantics { testTag = "clear_all_memories_button" }
                    ) {
                        Text(
                            text = "Resetear Memoria",
                            style = MaterialTheme.typography.labelSmall,
                            color = SleekTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // List of nodes
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    D3MemoryDashboard(memories = memories)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (filteredMemories.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(SleekSurface)
                                    .border(1.dp, SleekBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = SleekTextMuted,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Sin Nodos de Memoria",
                                style = MaterialTheme.typography.titleMedium,
                                color = SleekTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Charla con la IA o presiona '+' para registrar datos manualmente en Room DB.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SleekTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                items(filteredMemories, key = { it.id }) { node ->
                    MemoryNodeCard(
                        node = node,
                        onDelete = { onDeleteMemory(node) }
                    )
                }
            }
        }

        // Floating Action Button to Add Manual Node
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp)
                .semantics { testTag = "add_memory_fab" },
            containerColor = SleekVioletPrimary,
            contentColor = SleekVioletDark,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar Nodo de Memoria Manual",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (showAddDialog) {
        AddMemoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { cat, title, detail ->
                onAddMemory(cat, title, detail)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String, title: String, detail: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("PREFERENCE") }
    var expanded by remember { mutableStateOf(false) }

    val categories = listOf("COMMUNITY", "AMENITY", "PREFERENCE", "DIRECTIVE", "SECURITY", "FACT")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SleekSurface,
        title = {
            Text(
                text = "REGISTRAR NODO DE MEMORIA",
                style = MaterialTheme.typography.labelSmall,
                color = SleekVioletPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = when (category) {
                            "COMMUNITY" -> "🏡 Esencia Condominio"
                            "AMENITY" -> "🌿 Amenidades y Convivencia"
                            "PREFERENCE" -> "✨ Preferencia Residente"
                            "DIRECTIVE" -> "🛡️ Directiva y Reglas"
                            "SECURITY" -> "🔒 Seguridad y Caseta"
                            else -> "👁️ Hecho / Información"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría", color = SleekTextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekVioletPrimary,
                            unfocusedBorderColor = SleekBorder,
                            focusedTextColor = SleekTextPrimary,
                            unfocusedTextColor = SleekTextPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(SleekSurfaceVariant)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = when (cat) {
                                            "COMMUNITY" -> "🏡 Esencia Condominio"
                                            "AMENITY" -> "🌿 Amenidades y Convivencia"
                                            "PREFERENCE" -> "✨ Preferencia Residente"
                                            "DIRECTIVE" -> "🛡️ Directiva y Reglas"
                                            "SECURITY" -> "🔒 Seguridad y Caseta"
                                            else -> "👁️ Hecho / Información"
                                        },
                                        color = SleekTextPrimary
                                    )
                                },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título del Nodo", color = SleekTextSecondary) },
                    placeholder = { Text("Ej. Horario de Trabajo", color = SleekTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "add_memory_title_input" },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekVioletPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedTextColor = SleekTextPrimary,
                        unfocusedTextColor = SleekTextPrimary
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = { Text("Detalle / Regla", color = SleekTextSecondary) },
                    placeholder = { Text("Ej. El usuario trabaja en turno diurno y prefiere reportes breves.", color = SleekTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "add_memory_detail_input" },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekVioletPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedTextColor = SleekTextPrimary,
                        unfocusedTextColor = SleekTextPrimary
                    ),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && detail.isNotBlank()) {
                        onConfirm(category, title, detail)
                    }
                },
                enabled = title.isNotBlank() && detail.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekVioletPrimary,
                    contentColor = SleekVioletDark
                ),
                modifier = Modifier.semantics { testTag = "save_memory_node_button" }
            ) {
                Text("Guardar Nodo", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = SleekTextSecondary)
            }
        }
    )
}
