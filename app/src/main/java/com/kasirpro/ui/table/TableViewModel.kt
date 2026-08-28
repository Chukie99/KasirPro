package com.kasirpro.ui.table

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasirpro.data.model.Table
import com.kasirpro.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TableUiState(val tables: List<Table> = emptyList())

@HiltViewModel
class TableViewModel @Inject constructor(private val repo: Repository) : ViewModel() {
    private val _state = MutableStateFlow(TableUiState())
    val state: StateFlow<TableUiState> = _state
    init { load() }

    private fun load() {
        viewModelScope.launch {
            _state.value = TableUiState(repo.getAllTables())
        }
    }

    fun addTable(number: String, capacity: Int) {
        viewModelScope.launch {
            try {
                repo.addTable(Table(number = number, capacity = capacity))
                load()
            } catch (e: Exception) { /* duplicate number -> handle */ }
        }
    }

    fun updateTable(t: Table) {
        viewModelScope.launch { repo.updateTable(t); load() }
    }

    fun deleteTable(t: Table) {
        viewModelScope.launch { repo.deleteTable(t); load() }
    }
}
