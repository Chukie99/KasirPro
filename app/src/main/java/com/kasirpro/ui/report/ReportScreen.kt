package com.kasirpro.ui.report

import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorUtil
import com.kasirpro.utils.CurrencyFormatter
import kotlinx.coroutines.flow.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Laporan") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // Export CSV
                exportCSV(state, context)
            }) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.FileDownload,
                    contentDescription = "Export CSV",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Period selector
            PeriodSelector(
                period = state.period,
                onSelect = { viewModel.setPeriod(it) },
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                // Totals
                SummaryCard("Total Penjualan", CurrencyFormatter.formatRupiah(state.dailySales))
                SummaryDoubleCard(
                    "Jumlah Transaksi", state.transactionCount.toString(),
                    "Produk Terlaris", state.topProducts.firstOrNull()?.first ?: "-",
                )

                // Chart
                if (state.chartEntries.isNotEmpty()) {
                    Text("Penjualan", fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 8.dp))
                    AndroidView(
                        factory = { ctx ->
                            BarChart(ctx).apply {
                                val entries = state.chartEntries.mapIndexed { idx, e ->
                                    BarEntry(idx.toFloat(), e.value)
                                }
                                val dataSet = BarDataSet(entries, "Total (Rp1000)")
                                dataSet.color = ColorUtil.rgb("#1A73E8", "#34A853", "#FBBC04")
                                dataSet.valueTextSize = 10f
                                data = BarData(dataSet)
                                description = Description().apply { text = "Penjualan" }
                                invalidate()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(8.dp),
                    )
                }

                // Top products
                if (state.topProducts.isNotEmpty()) {
                    Text("Produk Terlaris", fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 4.dp))
                    state.topProducts.forEach { (name, qty) ->
                        ListItemRow(name, "x$qty")
                    }
                }
                // Top tables
                if (state.topTables.isNotEmpty()) {
                    Text("Meja Terlaris", fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 4.dp))
                    state.topTables.forEach { (number, count) ->
                        ListItemRow("Meja $number", "$count order")
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodSelector(period: ReportPeriod, onSelect: (ReportPeriod) -> Unit) {
    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ReportPeriod.values().forEach { p ->
            FilterChip(
                selected = period == p,
                onClick = { onSelect(p) },
                label = { Text(p.name.lowercase().replaceFirstChar { it.uppercase() }) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@Composable
fun SummaryCard(label: String, value: String) {
    Card(
        Modifier.fillMaxWidth().padding(16.dp, 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun SummaryDoubleCard(label1: String, val1: String, label2: String, val2: String) {
    Card(Modifier.fillMaxWidth().padding(16.dp, 8.dp), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column { Text(label1, style = MaterialTheme.typography.bodySmall); Text(val1, fontWeight = FontWeight.Bold) }
            Column(horizontalAlignment = Alignment.End) { Text(label2, style = MaterialTheme.typography.bodySmall); Text(val2, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun ListItemRow(label: String, value: String, Color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
    }
    Divider()
}

fun exportCSV(state: ReportUiState, context: android.content.Context) {
    try {
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloads.exists()) downloads.mkdirs()
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(downloads, "KasirPro_Report_$ts.csv")

        val writer = com.github.doyaaaaaken.kotlincsv.dsl.csvWriter()
        writer.open(file) {
            writer {
                singleRow(listOf("Metric", "Value"))
                singleRow(listOf("Total Penjualan", state.dailySales.toString()))
                singleRow(listOf("Jumlah Transaksi", state.transactionCount.toString()))
            }
        }
        android.widget.Toast.makeText(context, "Export berhasil! (${file.name})", android.widget.Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Export gagal: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}
