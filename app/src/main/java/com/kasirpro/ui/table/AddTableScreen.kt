package com.kasirpro.ui.table

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTableScreen(
    modifier: Modifier = Modifier,
    editTableId: Long? = null,
    onTableSaved: () -> Unit,
    viewModel: TableViewModel = hiltViewModel(),
) {
    var number by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("4") }
    val isEditing = editTableId != null

    // Load existing table if editing
    LaunchedEffect(editTableId) {
        if (editTableId != null) {
            val table = viewModel.getTable(editTableId)
            if (table != null) {
                number = table.number
                capacity = table.capacity.toString()
            }
        }
    }

    Scaffold(topBar = { SmallTopAppBar(title = { Text(if (isEditing) "Edit Meja" else "Tambah Meja") }) }) { padding ->
        Column(modifier = modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            OutlinedTextField(value = number, onValueChange = { if (it.length <= 4) number = it }, label = { Text("Nomor Meja") }, placeholder = { Text("Misal: 1, A3") })
            OutlinedTextField(value = capacity, onValueChange = { if (it.length <= 2) capacity = it }, label = { Text("Kapasitas (1-10)") }, placeholder = { Text("4") })
            Button(onClick = {
                val cap = capacity.toIntOrNull()?.coerceIn(1, 10) ?: 4
                if (number.isNotBlank()) {
                    if (isEditing && editTableId != null) {
                        viewModel.updateTableById(editTableId, number, cap)
                    } else {
                        viewModel.addTable(number, cap)
                    }
                    onTableSaved()
                }
            }, modifier = Modifier.fillMaxWidth().height(52.dp).padding(top = 16.dp)) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        }
    }
}
