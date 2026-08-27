package com.kasirpro.ui.transaction

/**
 * CartItem represents the items in the active cash register cart.
 */
data class CartItem(
    val productId: Long,
    val product: com.kasirpro.data.model.Product,
    val quantity: Int,
    val subtotal: Long,
)

data class TransactionUiState(
    val cart: List<CartItem> = emptyList(),
    val subtotal: Long = 0,
    val taxAmount: Long = 0,
    val discountInput: String = "",
    val discountAmount: Long = 0,
    val grandTotal: Long = 0,
    val paymentMethod: String = "Cash",
    val selectedTableNumber: String = "",
    val taxPercent: Double = 11.0,
    val availableTables: List<com.kasirpro.data.model.Table> = emptyList(),
    val isLoading: Boolean = false,
    val isComplete: Boolean = false,
    val errorMessage: String? = null,
)
