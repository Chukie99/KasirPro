package com.kasirpro.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Product entity — one row per menu item.
 * price is stored as Long (rupiah, no decimals needed for kopir prices)
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "price") val price: Long,         // in rupiah
    @ColumnInfo(name = "stock") val stock: Int = 0,
    @ColumnInfo(name = "category") val category: String = "Makanan",
    @ColumnInfo(name = "image_uri") val imageUri: String? = null,   // local file:// path
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)

/**
 * Table entity — represents a physical dining table.
 */
@Entity(tableName = "tables")
data class Table(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "number") val number: String,     // e.g. "1", "A3"
    @ColumnInfo(name = "capacity") val capacity: Int = 4, // 1-10
    @ColumnInfo(name = "status") val status: String = "Kosong", // Kosong / Terisi / Reservasi
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)

/**
 * TransactionItem — individual line item on a receipt.
 * Stored as JSON inside Transaction.items OR as separate rows in a join table.
 * Here we embed it as a JSON string field for simplicity & atomic saves.
 */
data class TransactionItem(
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val price: Long,        // unit price (rupiah)
    val subtotal: Long,     // quantity × price
)

/**
 * Transaction entity — a completed sale.
 * The full list of line items is stored as a JSON string in the `items` column
 * so the transaction is saved atomically (single DB write).
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "table_id") val tableId: Long,
    @ColumnInfo(name = "table_number") val tableNumber: String,
    @ColumnInfo(name = "payment_method") val paymentMethod: String, // Cash / QRIS / Transfer
    @ColumnInfo(name = "subtotal") val subtotal: Long,
    @ColumnInfo(name = "tax") val tax: Long,
    @ColumnInfo(name = "discount") val discount: Long,
    @ColumnInfo(name = "total") val total: Long,
    @ColumnInfo(name = "items_json") val itemsJson: String,   // JSON array of TransactionItem
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "synced") val synced: Boolean = false,
)

/**
 * App-wide setting row (key/value).
 * e.g. "tax_rate" → "11", "store_name" → "Kopi Bos"
 */
@Entity(tableName = "settings")
data class Setting(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "value") val value: String,
) {
    companion object {
        const val KEY_STORE_NAME = "store_name"
        const val KEY_STORE_ADDRESS = "store_address"
        const val KEY_STORE_PHONE = "store_phone"
        const val KEY_STORE_LOGO = "store_logo"
        const val KEY_TAX_RATE = "tax_rate"
        const val KEY_DEFAULT_DISCOUNT = "default_discount"
        const val KEY_DEFAULT_PAYMENT = "default_payment"
        const val KEY_BLUETOOTH_PRINTER = "bluetooth_printer"
        const val KEY_THEME = "theme"   // "light" | "dark" | "system"
        const val KEY_LANGUAGE = "language"  // "id" | "en"
    }
}
