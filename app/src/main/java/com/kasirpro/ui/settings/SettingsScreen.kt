package com.kasirpro.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kasirpro.utils.BackupHelper
import com.kasirpro.utils.DeviceIdHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // File picker for restore
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        uri?.let {
            val result = BackupHelper.restoreDatabase(context, it)
            if (result) {
                // Show restart prompt
                android.widget.Toast.makeText(context, "Restore berhasil! Restart aplikasi.", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Pengaturan") }) },
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Profil Toko ──
            SettingsSection("Profil Toko") {
                OutlinedTextField(value = state.storeName, onValueChange = { viewModel.onStoreName(it) }, label = { Text("Nama Toko") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state.storeAddress, onValueChange = { viewModel.onStoreAddress(it) }, label = { Text("Alamat") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state.storePhone, onValueChange = { viewModel.onStorePhone(it) }, label = { Text("Telepon") }, modifier = Modifier.fillMaxWidth())
            }

            // ── Pajak & Diskon ──
            SettingsSection("Pajak & Diskon") {
                OutlinedTextField(value = state.taxRate, onValueChange = { viewModel.onTaxRate(it) }, label = { Text("Pajak Default (%)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state.defaultDiscount, onValueChange = { viewModel.onDefaultDiscount(it) }, label = { Text("Diskon Default (%)") }, modifier = Modifier.fillMaxWidth())
            }

            // ── Tema ──
            SettingsSection("Tema") {
                Text("Mode Tampilan", style = MaterialTheme.typography.bodyMedium)
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.themeMode == "light",
                        onClick = { viewModel.onThemeMode("light") },
                        label = { Text("Terang") },
                    )
                    FilterChip(
                        selected = state.themeMode == "dark",
                        onClick = { viewModel.onThemeMode("dark") },
                        label = { Text("Gelap") },
                    )
                    FilterChip(
                        selected = state.themeMode == "system",
                        onClick = { viewModel.onThemeMode("system") },
                        label = { Text("Sistem") },
                    )
                }
            }

            // ── Printer ──
            SettingsSection("Printer Bluetooth") {
                if (state.isConnectedPrinter) {
                    Text("Terhubung: ${state.printerName}", color = MaterialTheme.colorScheme.secondary)
                } else {
                    Text("Belum ada printer terhubung", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = {
                    val deviceId = DeviceIdHelper.getDeviceId(context)
                    android.widget.Toast.makeText(context, "Scan via Settings sistem", android.widget.Toast.LENGTH_SHORT).show()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Scan Bluetooth Printer")
                }
            }

            // ── Backup & Restore ──
            SettingsSection("Backup & Restore") {
                Button(onClick = {
                    BackupHelper.exportDatabase(context)
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Text("📥 Backup Database")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    restoreLauncher.launch(arrayOf("application/octet-stream"))
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("📤 Restore Database")
                }
            }

            // ── Tentang ──
            SettingsSection("Tentang") {
                Text("KasirPro ${state.version}", fontWeight = FontWeight.Bold)
                Text("© 2026 Chukie99")
            }

            // Save button (sticky at bottom)
            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
            ) {
                Text("Simpan Pengaturan", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(16.dp, 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}
