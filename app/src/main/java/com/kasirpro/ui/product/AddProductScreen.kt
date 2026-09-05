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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kasirpro.utils.ImageHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    modifier: Modifier = Modifier,
    editProductId: Long? = null,
    onProductSaved: () -> Unit,
    viewModel: ProductViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Makanan") }
    var imagePath by remember { mutableStateOf<String?>(null) }
    var showNameError by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val isEditing = editProductId != null

    LaunchedEffect(editProductId) {
        if (editProductId != null) {
            val product = viewModel.getProduct(editProductId)
            if (product != null) {
                name = product.name
                price = product.price.toString()
                stock = product.stock.toString()
                category = product.category
                imagePath = product.imageUri
            }
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val bitmap = android.graphics.BitmapFactory.decodeStream(input)
                    if (bitmap != null) {
                        val path = ImageHelper.saveImageToInternal(context, bitmap)
                        imagePath = path
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (isEditing) "Edit Produk" else "Tambah Produk") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (name.isBlank()) { showNameError = true; return@FloatingActionButton }
                val p = price.toLongOrNull() ?: 0L
                val s = stock.toIntOrNull() ?: 0
                val safeP = if (p < 0) 0L else p
                val safeS = if (s < 0) 0 else s
                if (isEditing && editProductId != null) {
                    viewModel.updateProductById(editProductId, name.trim(), safeP, safeS, category, imagePath)
                } else {
                    viewModel.addProduct(name.trim(), safeP, safeS, category, imagePath)
                }
                onProductSaved()
            }) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "Simpan")
            }
        },
    ) { padding ->
        Column(modifier = modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
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
                    Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
            }
            Text("Ketuk untuk pilih gambar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; if (it.isNotBlank()) showNameError = false },
                label = { Text("Nama Produk *") },
                modifier = Modifier.fillMaxWidth(),
                isError = showNameError,
                supportingText = { if (showNameError) Text("Nama tidak boleh kosong") },
                singleLine = true
            )
            OutlinedTextField(
                value = price,
                onValueChange = { if (it.all { c -> c.isDigit() }) price = it },
                label = { Text("Harga (Rp)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("3000") },
                singleLine = true
            )
            OutlinedTextField(
                value = stock,
                onValueChange = { if (it.all { c -> c.isDigit() }) stock = it },
                label = { Text("Stok") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    listOf("Makanan", "Minuman", "Snack").forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c) },
                            onClick = { category = c; expanded = false },
                        )
                    }
                }
            }
        }
    }
}
