package com.kasirpro.data.repository

import androidx.room.withTransaction
import com.kasirpro.data.database.AppDatabase
import com.kasirpro.data.database.TransactionDao
import com.kasirpro.data.model.Product
import com.kasirpro.data.model.Table
import com.kasirpro.data.model.TransactionEntity
import com.kasirpro.data.model.TransactionItem
import com.kasirpro.data.model.Setting
import com.google.gson.GsonBuilder

/**
 * Repository — single source of truth for all data.
 *
 * In production you might have a remote + cache; here it's Room-only (offline).
 * All functions are suspend-safe (coroutine).
 */
class Repository(private val db: AppDatabase) {

    private val gson = GsonBuilder().create()

    // ── Product ─────────────────────────────────────────────────────────────
    suspend fun getAllProducts(): List<Product> = db.productDao().getAllProducts()
    suspend fun getProduct(id: Long): Product? = db.productDao().getProduct(id)
    suspend fun searchProducts(query: String): List<Product> =
        if (query.isBlank()) getAllProducts() else db.productDao().searchProducts(query)
    suspend fun getProductsByCategory(cat: String): List<Product> = db.productDao().getProductsByCategory(cat)
    suspend fun getAllCategories(): List<String> =
        (listOf("Semua", "Makanan", "Minuman", "Snack") + db.productDao().getAllCategories()).distinct()
    suspend fun addProduct(p: Product): Long = db.productDao().insert(p)
    suspend fun updateProduct(p: Product) = db.productDao().update(p)
    suspend fun deleteProduct(p: Product) = db.productDao().delete(p)

    // ── Table ───────────────────────────────────────────────────────────────
    suspend fun getAllTables(): List<Table> = db.tableDao().getAllTables()
    suspend fun getTable(id: Long): Table? = db.tableDao().getTable(id)
    suspend fun addTable(t: Table): Long = db.tableDao().insert(t)
    suspend fun updateTable(t: Table) = db.tableDao().update(t)
    suspend fun deleteTable(t: Table): Int = db.tableDao().deleteIfEmpty(t)

    // ── Transaction ─────────────────────────────────────────────────────────
    suspend fun getAllTransactions(): List<TransactionEntity> = db.transactionDao().getAllTransactions()
    suspend fun getTransaction(id: Long): TransactionEntity? = db.transactionDao().getTransaction(id)
    suspend fun getTransactionsBetween(start: Long, end: Long): List<TransactionEntity> =
        db.transactionDao().getTransactionsBetween(start, end)
    suspend fun addTransaction(tx: TransactionEntity): Long = db.transactionDao().insert(tx)

    /**
     * Atomic checkout: insert transaction + decrement stock + mark table Terisi.
     * All inside single Room transaction so no partial state if crash mid-sale.
     */
    suspend fun checkout(
        tx: TransactionEntity,
        items: List<TransactionItem>,
        tableId: Long?,
    ): Long = db.withTransaction {
        val id = db.transactionDao().insert(tx)
        // Decrement stock per product (clamp to 0, never negative)
        for (item in items) {
            val product = db.productDao().getProduct(item.productId) ?: continue
            val newStock = maxOf(0, product.stock - item.quantity)
            db.productDao().updateStock(item.productId, newStock)
        }
        // Mark table as Terisi if a table was selected
        if (tableId != null && tableId != 0L) {
            val table = db.tableDao().getTable(tableId)
            if (table != null && table.status != "Terisi") {
                db.tableDao().update(table.copy(status = "Terisi"))
            }
        }
        id
    }

    fun parseItems(json: String): List<TransactionItem> {
        val arr = gson.fromJson(json, Array<TransactionItem>::class.java)
        return arr.toList()
    }
    fun itemsToJson(items: List<TransactionItem>): String = gson.toJson(items)
    suspend fun dailyReport(start: Long, end: Long): List<TransactionDao.DailySummary> =
        db.transactionDao().dailySummary(start, end)

    // ── Settings ────────────────────────────────────────────────────────────
    suspend fun getString(key: String, default: String? = null): String? =
        db.settingDao().getValue(key) ?: default
    suspend fun putString(key: String, value: String) = db.settingDao().put(key, value)
    suspend fun getInt(key: String, defaultVal: Int): Int = db.settingDao().getInt(key, defaultVal)
    suspend fun putInt(key: String, value: Int) = db.settingDao().put(key, value.toString())

    // ── Convenience initializers ───────────────────────────────────────────
    suspend fun initDefaultsIfNeeded() {
        if (getString(Setting.KEY_TAX_RATE) == null) putInt(Setting.KEY_TAX_RATE, 11)
        if (getString(Setting.KEY_DEFAULT_DISCOUNT) == null) putInt(Setting.KEY_DEFAULT_DISCOUNT, 0)
        if (getString(Setting.KEY_STORE_NAME) == null) putString(Setting.KEY_STORE_NAME, "KasirPro Store")
        if (getString(Setting.KEY_THEME) == null) putString(Setting.KEY_THEME, "system")
        if (getString(Setting.KEY_DEFAULT_PAYMENT) == null) putString(Setting.KEY_DEFAULT_PAYMENT, "Cash")
    }
}
