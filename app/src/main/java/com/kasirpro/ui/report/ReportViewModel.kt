package com.kasirpro.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasirpro.data.model.TransactionEntity
import com.kasirpro.data.model.Setting
import com.kasirpro.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ReportUiState(
    val isLoading: Boolean = false,
    val dailySales: Long = 0L,
    val transactionCount: Int = 0,
    val topProducts: List<Pair<String, Int>> = emptyList(),
    val topTables: List<Pair<String, Int>> = emptyList(),
    val chartEntries: List<BarEntry> = emptyList(),
    val period: ReportPeriod = ReportPeriod.DAILY,
    val errorMessage: String? = null,
)

enum class ReportPeriod { DAILY, WEEKLY, MONTHLY }

data class BarEntry(val label: String, val value: Float)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repo: Repository,
) : ViewModel() {

    private val _state = MutableStateFlow(ReportUiState())
    val state: StateFlow<ReportUiState> = _state

    init { loadReport(ReportPeriod.DAILY) }

    fun setPeriod(period: ReportPeriod) {
        _state.value = _state.value.copy(period = period)
        loadReport(period)
    }

    private fun loadReport(period: ReportPeriod) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val calendar = Calendar.getInstance()
                val end = calendar.timeInMillis
                calendar.add(when (period) {
                    ReportPeriod.DAILY -> Calendar.DAY_OF_MONTH
                    ReportPeriod.WEEKLY -> Calendar.WEEK_OF_YEAR
                    ReportPeriod.MONTHLY -> Calendar.MONTH
                }, -1)
                val start = calendar.timeInMillis

                val txs = repo.getTransactionsBetween(start, end)
                val totalSales = txs.sumOf { it.total }
                val txCount = txs.size

                // Top products (count appearances in items)
                val productMap = mutableMapOf<String, Int>()
                val gson = com.google.gson.Gson()
                for (tx in txs) {
                    try {
                        val items = gson.fromJson(tx.itemsJson, Array<com.kasirpro.data.model.TransactionItem>::class.java)
                        items.forEach { item ->
                            val key = item.productName
                            productMap[key] = productMap.getOrDefault(key, 0) + item.quantity
                        }
                    } catch (_: Exception) { }
                }
                val topProducts = productMap.entries.sortedByDescending { it.value }
                    .take(5).map { Pair(it.key, it.value) }

                // Top tables
                val tableMap = mutableMapOf<String, Int>()
                txs.forEach { tx ->
                    tableMap[tx.tableNumber] = tableMap.getOrDefault(tx.tableNumber, 0) + 1
                }
                val topTables = tableMap.entries.sortedByDescending { it.value }
                    .take(5).map { Pair(it.key, it.value) }

                // Chart: daily bars (group by day)
                val dayMap = mutableMapOf<Long, Long>()
                txs.forEach { tx ->
                    val day = tx.createdAt / (24 * 3600 * 1000) * (24 * 3600 * 1000)
                    dayMap[day] = dayMap.getOrDefault(day, 0L) + tx.total
                }
                val chartEntries = dayMap.entries.sortedBy { it.key }.take(7)
                    .map { BarEntry(formatDayLabel(it.key), it.value.toFloat() / 1000f) }

                _state.value = ReportUiState(
                    dailySales = totalSales,
                    transactionCount = txCount,
                    topProducts = topProducts,
                    topTables = topTables,
                    chartEntries = chartEntries,
                    period = period,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = e.message, isLoading = false)
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    private fun formatDayLabel(ts: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM", Locale("id"))
        return sdf.format(Date(ts))
    }
}
