package com.kasirpro.ui.settings

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasirpro.data.model.Setting
import com.kasirpro.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val storeName: String = "KasirPro Store",
    val storeAddress: String = "",
    val storePhone: String = "",
    val taxRate: String = "11",
    val defaultDiscount: String = "0",
    val isConnectedPrinter: Boolean = false,
    val printerName: String = "",
    val isLoading: Boolean = false,
    val version: String = "v1.1.5",
    val themeMode: String = "system",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: Repository,
    private val prefs: SharedPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state

    init { loadSettings() }

    private fun loadSettings() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repo.getString(Setting.KEY_STORE_NAME)?.let { name -> _state.value = _state.value.copy(storeName = name) }
            repo.getString(Setting.KEY_STORE_ADDRESS)?.let { addr -> _state.value = _state.value.copy(storeAddress = addr) }
            repo.getString(Setting.KEY_STORE_PHONE)?.let { phone -> _state.value = _state.value.copy(storePhone = phone) }
            repo.getString(Setting.KEY_THEME)?.let { theme -> _state.value = _state.value.copy(themeMode = theme) }
            _state.value = _state.value.copy(
                taxRate = repo.getInt(Setting.KEY_TAX_RATE, 11).toString(),
                defaultDiscount = repo.getInt(Setting.KEY_DEFAULT_DISCOUNT, 0).toString(),
                version = "v1.1.5",
            )
        }
    }

    fun onStoreName(name: String) { _state.value = _state.value.copy(storeName = name) }
    fun onStoreAddress(addr: String) { _state.value = _state.value.copy(storeAddress = addr) }
    fun onStorePhone(phone: String) { _state.value = _state.value.copy(storePhone = phone) }
    fun onTaxRate(rate: String) { _state.value = _state.value.copy(taxRate = rate) }
    fun onDefaultDiscount(disc: String) { _state.value = _state.value.copy(defaultDiscount = disc) }
    fun onThemeMode(mode: String) { _state.value = _state.value.copy(themeMode = mode) }

    fun saveSettings() {
        val s = _state.value
        viewModelScope.launch {
            repo.putString(Setting.KEY_STORE_NAME, s.storeName)
            repo.putString(Setting.KEY_STORE_ADDRESS, s.storeAddress)
            repo.putString(Setting.KEY_STORE_PHONE, s.storePhone)
            repo.putString(Setting.KEY_THEME, s.themeMode)
            // Also save to SharedPreferences for MainActivity to read
            prefs.edit().putString("kasirpro_theme_mode", s.themeMode).apply()
            s.taxRate.toIntOrNull()?.let { repo.putInt(Setting.KEY_TAX_RATE, it) }
            s.defaultDiscount.toIntOrNull()?.let { repo.putInt(Setting.KEY_DEFAULT_DISCOUNT, it) }
        }
    }

    fun saveBluetoothPrinter(name: String, address: String) {
        prefs.edit()
            .putString("kasirpro_printer_name", name)
            .putString("kasirpro_printer_address", address)
            .apply()
        _state.value = _state.value.copy(isConnectedPrinter = true, printerName = name)
    }
}
