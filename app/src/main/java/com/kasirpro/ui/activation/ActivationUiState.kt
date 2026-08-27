package com.kasirpro.ui.activation

/**
 * Thin data-holder for the activation screen's UI state.
 * The ViewModel owns the real logic; this just mirrors what Compose needs.
 */
data class ActivationUiState(
    val deviceId: String = "",
    val serialInput: String = "",
    val isChecking: Boolean = false,
    val isActivated: Boolean = false,
    val errorMessage: String? = null,
    val isInputValid: Boolean = false,
)
