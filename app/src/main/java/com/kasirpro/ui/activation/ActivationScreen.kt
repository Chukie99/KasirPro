package com.kasirpro.ui.activation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kasirpro.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivationScreen(
    onActivated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ActivationViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refreshDeviceId(context)
    }

    LaunchedEffect(state.isActivated) {
        if (state.isActivated) {
            val sp = context.getSharedPreferences("kasirpro_prefs", Context.MODE_PRIVATE)
            sp.edit().putBoolean("kasirpro_activated", true).apply()
            onActivated()
        }
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))

            Icon(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "KasirPro Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text("Aktivasi Aplikasi", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(
                "Kirim Device ID ke Admin untuk mendapatkan Serial Number",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(Modifier.height(32.dp))

            // Device ID box (clickable → copy to clipboard)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f), RoundedCornerShape(12.dp))
                    .clickable {
                        viewModel.copyDeviceId { id ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Device ID", id))
                            android.widget.Toast.makeText(context, "Device ID disalin: $id", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
            ) {
                Text(
                    text = state.deviceId.ifEmpty { "..." },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 3.sp,
                )
            }

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = state.serialInput,
                onValueChange = { if (it.length <= 8) viewModel.onSerialChange(it) },
                label = { Text("Masukkan Serial Number (8 karakter)") },
                singleLine = true,
                isError = state.errorMessage?.isNotEmpty() == true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                supportingText = {
                    state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.activate(context) },
                enabled = state.isInputValid && !state.isChecking,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                if (state.isChecking) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text("Aktifkan", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
