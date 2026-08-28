package com.kasirpro.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasirpro.data.model.Setting
import com.kasirpro.data.model.Table
import com.kasirpro.data.model.TransactionEntity
import com.kasirpro.data.model.TransactionItem
import com.kasirpro.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repo: Repository,
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionUiState())
    val state: StateFlow<TransactionUiState> = _state

    init {
        loadTables()
        loadTaxRate()
    }

    private fun loadTaxRate() {
        viewModelScope.launch {
            val tax = repo.getInt(Setting.KEY_TAX_RATE, 11)
            _state.update { it.copy(taxPercent = tax.toDouble()) }
        }
    }

    private fun loadTables() {
        viewModelScope.launch {
            val tables = repo.getAllTables()
            _state.update { it.copy(availableTables = tables) }
        }
    }

    fun selectTable(number: String) {
        _state.update { it.copy(selectedTableNumber = number) }
    }

    fun onPaymentChange(method: String) {
        _state.update { it.copy(paymentMethod = method) }
    }

    fun onDiscountChange(text: String) {
        val amt = text.toLongOrNull() ?: 0L
        val subtotal = _state.value.subtotal
        val taxRate = _state.value.taxPercent / 100.0
        val afterDiscount = maxOf(0L, subtotal - amt)
        val tax = (afterDiscount * taxRate).toLong()
        val total = afterDiscount + tax
        _state.update {
            it.copy(
                discountInput = text,
                discountAmount = amt,
                taxAmount = tax,
                grandTotal = total,
            )
        }
    }

    fun updateQty(productId: Long, newQty: Int) {
        // Rebuild cart — in a full app we'd have a more granular update.
        val current = _state.value.cart.toMutableList()
        val idx = current.indexOfFirst { it.productId == productId }
        if (idx >= 0) {
            val item = current[idx]
            if (newQty <= 0) {
                current.removeAt(idx)
            } else {
                current[idx] = item.copy(quantity = newQty, subtotal = item.product.price * newQty)
            }
        }
        val subtotal = current.sumOf { it.subtotal }
        val taxRate = _state.value.taxPercent / 100.0
        val tax = (subtotal * taxRate).toLong()
        val discount = _state.value.discountAmount
        val total = subtotal - discount + tax
        _state.update {
            it.copy(cart = current, subtotal = subtotal, taxAmount = tax, grandTotal = total)
        }
    }

    fun removeItem(productId: Long) {
        val current = _state.value.cart.filterNot { it.productId == productId }
        val subtotal = current.sumOf { it.subtotal }
        val taxRate = _state.value.taxPercent / 100.0
        val tax = (subtotal * taxRate).toLong()
        val discount = _state.value.discountAmount
        val total = subtotal - discount + tax
        _state.update {
            it.copy(cart = current, subtotal = subtotal, taxAmount = tax, grandTotal = total)
        }
    }

    fun addToCart(product: com.kasirpro.data.model.Product, qty: Int = 1) {
        val current = _state.value.cart.toMutableList()
        val idx = current.indexOfFirst { it.productId == product.id }
        if (idx >= 0) {
            val item = current[idx]
            current[idx] = item.copy(quantity = item.quantity + qty, subtotal = product.price * (item.quantity + qty))
        } else {
            current.add(
                CartItem(product.id, product, qty, product.price * qty)
            )
        }
        val subtotal = current.sumOf { it.subtotal }
        val taxRate = _state.value.taxPercent / 100.0
        val tax = (subtotal * taxRate).toLong()
        val total = subtotal - _state.value.discountAmount + tax
        _state.update {
            it.copy(cart = current, subtotal = subtotal, taxAmount = tax, grandTotal = total)
        }
    }

    fun completeTransaction() {
        val s = _state.value
        if (s.cart.isEmpty() || s.selectedTableNumber.isBlank()) return

        val itemsJson = com.google.gson.GsonBuilder().create()
            .toJson(s.cart.map {
                TransactionItem(
                    productId = it.productId,
                    productName = it.product.name,
                    quantity = it.quantity,
                    price = it.product.price,
                    subtotal = it.subtotal,
                )
            })

        // Find matching table row id
        val tableObj = s.availableTables.find { it.number == s.selectedTableNumber }
        val tableId = tableObj?.id ?: 0L

        val tx = TransactionEntity(
            tableId = tableId,
            tableNumber = s.selectedTableNumber,
            paymentMethod = s.paymentMethod,
            subtotal = s.subtotal,
            tax = s.taxAmount,
            discount = s.discountAmount,
            total = s.grandTotal,
            itemsJson = itemsJson,
        )

        viewModelScope.launch {
            repo.addTransaction(tx)
            _state.update { st ->
                st.copy(
                    isComplete = true,
                    errorMessage = null,
                    cart = emptyList(),
                    subtotal = 0L,
                    discountInput = "",
                    discountAmount = 0L,
                    grandTotal = 0L,
                )
            }
        }
    }
}
