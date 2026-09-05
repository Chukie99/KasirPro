package com.kasirpro.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasirpro.data.model.Product
import com.kasirpro.data.model.Setting
import com.kasirpro.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val storeName: String = "KasirPro",
    val products: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "Semua",
    val isLoading: Boolean = false,
    val searchQuery: String = "",
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repo: Repository,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state

    init {
        loadStoreName()
        loadCategories()
        loadProducts()
    }

    private fun loadStoreName() {
        viewModelScope.launch {
            val name = repo.getString(Setting.KEY_STORE_NAME, "KasirPro Store") ?: "KasirPro Store"
            _state.update { it.copy(storeName = name) }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val cats = repo.getAllCategories()
            _state.update { s -> s.copy(categories = cats) }
        }
    }

    fun loadProducts() {
        val cat = _state.value.selectedCategory
        val query = _state.value.searchQuery
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // Kombinasi: search + kategori = AND (bukan OR) biar filter akurat
            val base = if (cat != "Semua") repo.getProductsByCategory(cat) else repo.getAllProducts()
            val products = if (query.isNotBlank()) base.filter { it.name.contains(query, ignoreCase = true) } else base
            _state.update { it.copy(products = products, isLoading = false) }
        }
    }

    fun onCategorySelected(cat: String) {
        _state.update { it.copy(selectedCategory = cat) }
        loadProducts()
    }

    fun onSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        loadProducts()
    }
}
