package com.kasirpro.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kasirpro.R
import com.kasirpro.data.model.Product
import com.kasirpro.utils.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    modifier: Modifier = Modifier,
    onEditProduct: (Product) -> Unit,
    viewModel: ProductViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Produk") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* navigate to add */ }) {
                Icon(painterResource(R.drawable.ic_launcher), contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
    ) { padding ->
        Column(modifier = modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            // Search bar
            OutlinedTextField(value = state.searchQuery, onValueChange = { if (it.length < 50) viewModel.onSearch(it) }, placeholder = { Text("Cari produk…") }, modifier = Modifier.fillMaxWidth().padding(16.dp))

            // Category filter
            if (state.categories.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 12.dp)) {
                    state.categories.forEach { cat ->
                        FilterChip(selected = state.selectedCategory == cat, onClick = { viewModel.onCategory(cat) }, label = { Text(cat) })
                    }
                }
            }

            // Product list
            if (state.isLoading) {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (state.filtered.isEmpty()) {
                Text("Tidak ada produk.", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                state.filtered.forEach { product ->
                    ProductCard(product = product, onEdit = { onEditProduct(product) }, onDelete = { viewModel.deleteProduct(product) })
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.fillMaxWidth().height(80.dp).padding(8.dp)) {
            // Image
            Box(Modifier.size(64.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))) {
                val uri = product.imageUri
                if (!uri.isNullOrBlank()) {
                    AsyncImage(model = uri, contentDescription = product.name, modifier = Modifier.matchParentSize(), placeholder = painterResource(R.drawable.ic_launcher))
                } else {
                    Icon(painterResource(R.drawable.ic_launcher), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.Center).size(32.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold)
                Text(CurrencyFormatter.formatRupiah(product.price), color = MaterialTheme.colorScheme.primary)
                Text("Stok: ${product.stock}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.wrapContentHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(painterResource(id = R.drawable.ic_launcher), contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable(onClick = onEdit).size(24.dp))
                Icon(painterResource(id = R.drawable.ic_launcher), contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.clickable(onClick = onDelete).size(24.dp))
            }
        }
    }
}
