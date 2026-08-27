package com.kasirpro.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasirpro.data.model.Product
import com.kasirpro.data.model.Setting
import com.kasirpro.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductUiState(
    val products: List<Product> = emptyList(),
    val filtered: List<Product> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "Semua",
    val categories: List<String> = listOf("Semua", "Makanan", "Minuman", "Snack"),
    val isLoading: Boolean = false,
)

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repo: Repository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProductUiState())
    val state: StateFlow<ProductUiState> = _state

    init { loadProducts() }

    private fun loadProducts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val products = repo.getAllProducts()
            _state.value = _state.value.copy(
                products = products,
                filtered = products,
                isLoading = false,
                categories = listOf("Semua") + products.map { it.category }.distinct(),
            )
        }
    }

    fun onSearch(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        filter()
    }

    fun onCategory(cat: String) {
        _state.value = _state.value.copy(selectedCategory = cat)
        filter()
    }

    private fun filter() {
        val q = _state.value.searchQuery
        val cat = _state.value.selectedCategory
        val result = _state.value.products.filter { p ->
            (cat == "Semua" || p.category == cat) &&
            (q.isEmpty() || p.name.contains(q, ignoreCase = true))
        }
        _state.value = _state.value.copy(filtered = result)
    }

    fun deleteProduct(p: Product) {
        viewModelScope.launch {
            // Also delete image file
            com.kasirpro.utils.ImageHelper.deleteImage(p.imageUri)
            repo.deleteProduct(p)
            loadProducts()
        }
    }

    fun addProduct(name: String, price: Long, stock: Int, category: String, imageUri: String?) {
        viewModelScope.launch {
            repo.addProduct(Product(
                name = name,
                price = price,
                stock = stock,
                category = category,
                imageUri = imageUri,
            ))
            loadProducts()
        }
    }

    fun updateProduct(p: Product) {
        viewModelScope.launch {
            repo.updateProduct(p.copy(updatedAt = System.currentTimeMillis()))
            loadProducts()
        }
    }
}
