package com.kasirpro.ui.activation

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasirpro.utils.DeviceIdHelper
import com.kasirpro.utils.SerialValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ActivationViewModel — holds the device ID + validates the typed serial.
 *
 * Reads/writes activation status to SharedPreferences so it survives
 * process restarts and survives app upgrades.
 */
@HiltViewModel
class ActivationViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(ActivationUiState())
    val state: StateFlow<ActivationUiState> = _state

    init {
        checkActivationStatus()
    }

    fun refreshDeviceId(context: android.content.Context) {
        val id = DeviceIdHelper.getDeviceId(context)
        _state.value = _state.value.copy(deviceId = id)
    }

    fun onSerialChange(newSerial: String) {
        val cleaned = newSerial.trim().uppercase()
        val isValid = cleaned.length == 8
        _state.value = _state.value.copy(
            serialInput = cleaned,
            isInputValid = isValid,
            errorMessage = if (!isValid && cleaned.isNotBlank())
                "Serial harus 8 karakter"
            else
                null,
        )
    }

    /**
     * Called when user presses "Aktifkan". Validates the serial against
     * the device ID. On success, persists the activation flag.
     */
    fun activate(context: android.content.Context) {
        val currentState = _state.value
        val serial = currentState.serialInput
        val deviceId = currentState.deviceId

        if (serial.isBlank()) {
            _state.value = currentState.copy(
                errorMessage = "Masukkan Serial Number dulu"
            )
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isChecking = true, errorMessage = null)

            // Get the raw Android ID for serial checking
            val rawAndroidId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ) ?: ""

            val result = SerialValidator.validate(serial, rawAndroidId)

            if (result.isSuccess) {
                // Persist activation
                sharedPreferences.edit()
                    .putBoolean("kasirpro_activated", true)
                    .putString("kasirpro_serial", serial)
                    .putLong("kasirpro_activated_at", System.currentTimeMillis())
                    .apply()

                _state.value = _state.value.copy(
                    isChecking = false,
                    isActivated = true,
                    errorMessage = null,
                )
            } else {
                _state.value = _state.value.copy(
                    isChecking = false,
                    errorMessage = result.message,
                )
            }
        }
    }

    fun copyDeviceId(callback: (String) -> Unit) {
        val id = _state.value.deviceId
        callback(id)
    }

    private fun checkActivationStatus() {
        val already = sharedPreferences.getBoolean("kasirpro_activated", false)
        if (already) {
            _state.value = _state.value.copy(isActivated = true)
        }
    }
}
