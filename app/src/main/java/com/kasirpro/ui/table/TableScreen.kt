package com.kasirpro.ui.table

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kasirpro.data.model.Table

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableScreen(
    modifier: Modifier = Modifier,
    onAddTable: () -> Unit,
    onEditTable: (Table) -> Unit,
    viewModel: TableViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Meja") }) },
        floatingActionButton = { FloatingActionButton(onClick = onAddTable) { Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Add table", tint = MaterialTheme.colorScheme.onPrimary) } },
    ) { padding ->
        if (state.tables.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Belum ada meja.") }
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(3), contentPadding = PaddingValues(8.dp)) {
                items(state.tables, key = { it.id }) { t ->
                    TableCard(t, onEdit = { onEditTable(t) }) { viewModel.deleteTable(t) }
                }
            }
        }
    }
}

@Composable
fun TableCard(table: Table, onEdit: () -> Unit, onDelete: () -> Unit) {
    val color = when (table.status) {
        "Terisi" -> Color(0xFFD32F2F)
        "Reservasi" -> Color(0xFFFBBC04)
        else -> Color(0xFF34A853)
    }
    Card(Modifier.size(100.dp, 110.dp).padding(4.dp), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(table.number, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("kapasitas ${table.capacity}", style = MaterialTheme.typography.bodySmall)
            Box(Modifier.padding(4.dp).clip(RoundedCornerShape(8.dp)).background(color), contentAlignment = Alignment.Center) {
                Text(table.status, color = Color.White, fontSize = 11.sp)
            }
            Row(Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(androidx.compose.material.icons.Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.clickable(onClick = onEdit).size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Icon(androidx.compose.material.icons.Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.clickable(onClick = onDelete).size(16.dp), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
