package com.kasirpro.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kasirpro.R
import com.kasirpro.data.model.Product
import com.kasirpro.data.model.Table
import com.kasirpro.data.model.TransactionItem
import com.kasirpro.printer.ReceiptPrinter
import com.kasirpro.utils.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    modifier: Modifier = Modifier,
    viewModel: TransactionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            SmallTopAppBar(title = { Text("Transaksi Kasir") })
        },
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Table selector
            TableSelector(
                tables = state.availableTables,
                selected = state.selectedTableNumber,
                onSelect = { viewModel.selectTable(it) },
            )

            Spacer(Modifier.height(12.dp))

            // Cart items
            if (state.cart.isEmpty()) {
                Text(
                    "Pilih produk dari dashboard untuk menambah ke keranjang",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                state.cart.forEach { item ->
                    CartItemRow(item = item, onQtyChange = { newQty ->
                        viewModel.updateQty(item.productId, newQty)
                    }) {
                        viewModel.removeItem(item.productId)
                    }
                }
            }

            // Discount + Tax inputs
            OutlinedTextField(
                value = state.discountInput,
                onValueChange = { if (it.length <= 5) viewModel.onDiscountChange(it) },
                label = { Text("Diskon (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
            )

            // Payment method
            Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RadioButtonRow("Cash", state.paymentMethod == "Cash") { viewModel.onPaymentChange("Cash") }
                RadioButtonRow("QRIS", state.paymentMethod == "QRIS") { viewModel.onPaymentChange("QRIS") }
                RadioButtonRow("Transfer", state.paymentMethod == "Transfer") { viewModel.onPaymentChange("Transfer") }
            }

            // Totals summary
            Column(Modifier.padding(16.dp)) {
                SummaryRow("Subtotal", CurrencyFormatter.formatRupiah(state.subtotal))
                SummaryRow("Pajak ${state.taxPercent.toInt()}%", CurrencyFormatter.formatRupiah(state.taxAmount))
                SummaryRow("Diskon", "-${CurrencyFormatter.formatRupiah(state.discountAmount)}")
                Divider(Modifier.padding(vertical = 8.dp))
                SummaryRow("TOTAL", CurrencyFormatter.formatRupiah(state.grandTotal), isBold = true, Color = MaterialTheme.colorScheme.primary)
            }

            // Pay button
            Button(
                onClick = {
                    viewModel.completeTransaction()
                    // Try print receipt
                    ReceiptPrinter.printReceipt(
                        context = context,
                        storeName = "KasirPro Store",  // fetch from prefs later
                        items = state.cart,
                        subtotal = state.subtotal,
                        tax = state.taxAmount,
                        discount = state.discountAmount,
                        total = state.grandTotal,
                        paymentMethod = state.paymentMethod,
                    )
                    android.widget.Toast.makeText(context, "Transaksi berhasil!", android.widget.Toast.LENGTH_LONG).show()
                },
                enabled = state.cart.isNotEmpty() && state.selectedTableNumber.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("BAYAR", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun TableSelector(tables: List<Table>, selected: String, onSelect: (String) -> Unit) {
    Text("Pilih Meja", fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
        tables.take(4).forEach { t ->
            FilterChip(
                selected = selected == t.number,
                onClick = { onSelect(t.number) },
                label = { Text(t.number) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.secondary),
            )
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, onQtyChange: (Int) -> Unit, onDelete: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(item.product.name, fontWeight = FontWeight.SemiBold)
            Text(CurrencyFormatter.formatRupiah(item.product.price) + " x" + item.quantity, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        // Qty controls
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (item.quantity > 1) onQtyChange(item.quantity - 1) }) { Text("-", fontWeight = FontWeight.Bold) }
            Text(item.quantity.toString(), fontWeight = FontWeight.Bold)
            IconButton(onClick = { onQtyChange(item.quantity + 1) }) { Text("+", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.width(8.dp))
            Text(CurrencyFormatter.formatRupiah(item.subtotal), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.clickable(onClick = onDelete),
        )
    }
    Divider()
}

@Composable
fun RadioButtonRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(Modifier.clickable(onSelect).padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onSelect, colors = RadioButtonDefaults.radioButtonColors(selected = MaterialTheme.colorScheme.primary))
        Text(label)
    }
}

@Composable
fun SummaryRow(label: String, value: String, isBold: Boolean = false, Color: Color = LocalTextColor.current) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = Color)
        Text(value, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = Color)
    }
}
