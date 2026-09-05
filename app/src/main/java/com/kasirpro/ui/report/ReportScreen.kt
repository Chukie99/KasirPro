package com.kasirpro.ui.report

import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.kasirpro.utils.CurrencyFormatter
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileWriter
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
        topBar = { TopAppBar(title = { Text("Laporan") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { exportCSV(state, context) }) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
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

                // Native Compose bar chart
                if (state.chartEntries.isNotEmpty()) {
                    Text("Penjualan", fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 8.dp))
                    BarChart(entries = state.chartEntries, modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(8.dp)
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
fun BarChart(entries: List<BarEntry>, modifier: Modifier = Modifier) {
    val max = (entries.maxOfOrNull { it.value } ?: 1f).takeIf { it > 0 } ?: 1f
    val barColor = Color(0xFF1A73E8)
    val axisColor = Color(0xFFD1D5DB)
    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = size.width / entries.size * 0.6f
                val spacing = size.width / entries.size
                entries.forEachIndexed { idx, e ->
                    val barHeight = (e.value / max) * size.height * 0.85f
                    val x = spacing * idx + spacing * 0.2f
                    val yTop = size.height - barHeight
                    // bar
                    drawRect(
                        color = barColor,
                        topLeft = Offset(x, yTop),
                        size = Size(barWidth, barHeight)
                    )
                }
                // x-axis line
                drawLine(axisColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 2f)
            }
        }
        // x labels
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            entries.forEach { e ->
                Text(e.label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun PeriodSelector(period: ReportPeriod, onSelect: (ReportPeriod) -> Unit) {
    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ReportPeriod.entries.forEach { p ->
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
fun ListItemRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
    }
    HorizontalDivider()
}

fun exportCSV(state: ReportUiState, context: android.content.Context) {
    try {
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloads.exists()) downloads.mkdirs()
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(downloads, "KasirPro_Report_$ts.csv")
        FileWriter(file).use {
            CSVPrinter(it, CSVFormat.DEFAULT).use { printer ->
                // Summary
                printer.printRecord("METRIK", "NILAI")
                printer.printRecord("Total Penjualan", state.dailySales.toString())
                printer.printRecord("Jumlah Transaksi", state.transactionCount.toString())
                printer.printRecord("Periode", state.period.name)
                printer.printRecord("")

                // Top products
                if (state.topProducts.isNotEmpty()) {
                    printer.printRecord("PRODUK TERLARIS", "JUMLAH TERJUAL")
                    state.topProducts.forEach { (name, qty) ->
                        printer.printRecord(name, qty.toString())
                    }
                    printer.printRecord("")
                }

                // Top tables
                if (state.topTables.isNotEmpty()) {
                    printer.printRecord("MEJA TERLARIS", "JUMLAH ORDER")
                    state.topTables.forEach { (number, count) ->
                        printer.printRecord("Meja $number", count.toString())
                    }
                }
            }
        }
        Toast.makeText(context, "Export berhasil! (${file.name})", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Export gagal: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
