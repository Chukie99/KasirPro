package com.kasirpro.data.database

import androidx.room.*
import com.kasirpro.data.model.Product
import com.kasirpro.data.model.Table
import com.kasirpro.data.model.TransactionEntity
import com.kasirpro.data.model.Setting

/**
 * ===== Product DAO =====
 */
@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllProducts(): List<Product>

    @Query("SELECT * FROM products ORDER BY created_at DESC")
    suspend fun getAllProductsByNewest(): List<Product>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProduct(id: Long): Product?

    @Query("SELECT * FROM products WHERE name LIKE :query || '%' ESCAPE '\\' ORDER BY name ASC")
    suspend fun searchProducts(query: String): List<Product>

    @Query("SELECT DISTINCT category FROM products ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
    suspend fun getProductsByCategory(category: String): List<Product>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("UPDATE products SET stock = :newStock WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Int)
}


/**
 * ===== Table DAO =====
 */
@Dao
interface TableDao {

    @Query("SELECT * FROM tables ORDER BY number ASC")
    suspend fun getAllTables(): List<Table>

    @Query("SELECT * FROM tables WHERE status = 'Terisi'")
    suspend fun getOccupiedTables(): List<Table>

    @Query("SELECT * FROM tables WHERE id = :id")
    suspend fun getTable(id: Long): Table?

    @Query("SELECT * FROM tables WHERE number = :number")
    suspend fun getTableByNumber(number: String): Table?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(table: Table): Long

    @Update
    suspend fun update(table: Table)

    /**
     * Deletes a table only if it's not occupied.
     * Returns the number of rows deleted (0 if blocked).
     */
    @Transaction
    suspend fun deleteIfEmpty(table: Table): Int {
        if (table.status == "Terisi") return 0
        delete(table)
        return 1
    }

    @Delete
    suspend fun delete(table: Table)
}


/**
 * ===== Transaction DAO =====
 */
@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY created_at DESC")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransaction(id: Long): TransactionEntity?

    /** All transactions within a time range (epoch millis) */
    @Query("SELECT * FROM transactions WHERE created_at >= :start AND created_at <= :end ORDER BY created_at DESC")
    suspend fun getTransactionsBetween(start: Long, end: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE created_at >= :start AND created_at < :end ORDER BY created_at DESC")
    suspend fun getTransactionsInDay(start: Long, end: Long): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    /** Sum of total column grouped by day, within range */
    @Query("""
        SELECT CAST(created_at / 86400000 AS TEXT) as day, SUM(total) as total_sales, COUNT(*) as tx_count
        FROM transactions
        WHERE created_at >= :start AND created_at <= :end
        GROUP BY day
        ORDER BY day DESC
    """)
    suspend fun dailySummary(start: Long, end: Long): List<DailySummary>

    data class DailySummary(
        val day: String,
        val total_sales: Long,
        val tx_count: Int,
    )
}


/**
 * ===== Settings DAO =====
 */
@Dao
interface SettingDao {

    @Query("SELECT * FROM settings WHERE [key] = :key LIMIT 1")
    suspend fun get(key: String): Setting?

    @Query("SELECT value FROM settings WHERE [key] = :key LIMIT 1")
    suspend fun getValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: Setting)

    @Update
    suspend fun update(setting: Setting)

    /** Convenience: store or replace a key/value pair */
    @Transaction
    suspend fun put(key: String, value: String) {
        val existing = get(key)
        if (existing != null) {
            update(Setting(id = existing.id, key = key, value = value))
        } else {
            insert(Setting(key = key, value = value))
        }
    }

    /** Get an Int setting (returns default if missing) */
    @Transaction
    suspend fun getInt(key: String, defaultVal: Int): Int {
        val v = getValue(key) ?: return defaultVal
        return v.toIntOrNull() ?: defaultVal
    }

    suspend fun getStoreName(): String? = getValue(Setting.KEY_STORE_NAME)
}


/**
 * ===== Database =====
 */
@Database(
    entities = [Product::class, Table::class, TransactionEntity::class, Setting::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun tableDao(): TableDao
    abstract fun transactionDao(): TransactionDao
    abstract fun settingDao(): SettingDao

    companion object {
        fun buildDatabase(context: android.content.Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "kasirpro.db"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
