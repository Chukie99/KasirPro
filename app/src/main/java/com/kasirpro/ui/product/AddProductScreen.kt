package com.kasirpro.ui.product

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kasirpro.R
import com.kasirpro.utils.ImageHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    modifier: Modifier = Modifier,
    onProductSaved: () -> Unit,
    viewModel: ProductViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Makanan") }
    var imagePath by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            // Convert uri to bitmap & save
            try {
                val input = context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(input)
                input?.close()
                val path = ImageHelper.saveImageToInternal(context, bitmap)
                imagePath = path
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Tambah Produk") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val p = price.toLongOrNull() ?: 0L
                val s = stock.toIntOrNull() ?: 0
                if (name.isNotBlank()) {
                    viewModel.addProduct(name, p, s, category, imagePath)
                    onProductSaved()
                }
            }) { Text("💾") }
        },
    ) { padding ->
        Column(modifier = modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            // Image picker
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { picker.launch("image/*") },
                contentAlignment = Alignment.Center,
            ) {
                if (imagePath != null) {
                    AsyncImage(model = imagePath, contentDescription = "Product image", contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
                } else {
                    Icon(painterResource(R.drawable.ic_launcher), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
            }
            Text("Ketuk untuk pilih gambar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Produk") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga (Rp)") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("3000") })
            OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stok") }, modifier = Modifier.fillMaxWidth())

            // Category dropdown
            var expanded by remember { mutableStateOf(false) }
            Box(Modifier.fillMaxWidth()) {
                OutlinedTextField(value = category, onValueChange = {}, readOnly = true, label = { Text("Kategori") }, modifier = Modifier.fillMaxWidth(), trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }, onClick = { expanded = true })
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("Makanan", "Minuman", "Snack").forEach { c ->
                        DropdownMenuItem(onClick = { category = c; expanded = false }) { Text(c) }
                    }
                }
            }
        }
    }
}
