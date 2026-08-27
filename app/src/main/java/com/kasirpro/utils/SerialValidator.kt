package com.kasirpro.utils

import java.security.MessageDigest

/**
 * SerialValidator — validates the activation serial number that the user
 * types into the Activation screen.
 *
 * Validation algorithm (MUST match admin HTML generator):
 *
 *   1. Serial = hash = SHA256(DeviceID + "KasirProSalt2024")
 *   2. Take 8 chars from the hex digest
 *   3. Convert to uppercase alphanumeric (A–Z, 0–9)
 *      — the HTML generator maps the hex slice through a base-36 alphabet,
 *        so we replicate that exact mapping here.
 *
 * Rules:
 *   - Serial must be exactly 8 uppercase alphanumeric characters.
 *   - If the typed serial ≠ expected → invalid.
 *
 * @return ValidationResult(success=Boolean, message=String)
 */
object SerialValidator {

    private const val SALT = "KasirProSalt2024"
    private val CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray()

    /**
     * Validates a user-typed serial against the expected serial derived
     * from the given deviceAndroidId.
     */
    fun validate(serial: String, deviceAndroidId: String): ValidationResult {
        val trimmed = serial.trim().uppercase()
        if (trimmed.isEmpty()) {
            return ValidationResult(false, "Serial Number tidak boleh kosong")
        }
        if (trimmed.length != 8) {
            return ValidationResult(false, "Serial Number harus 8 karakter")
        }
        if (!trimmed.matches(Regex("^[A-Z0-9]+\$"))) {
            return ValidationResult(false, "Serial hanya boleh huruf A-Z dan angka 0-9")
        }

        val expected = generateExpectedSerial(deviceAndroidId)
        return if (trimmed == expected) {
            ValidationResult(true, "Aktivasi berhasil!")
        } else {
            ValidationResult(false, "Serial Number tidak valid!")
        }
    }

    /**
     * Generates the expected serial for a given raw Android ID,
     * replicating the HTML generator's exact algorithm.
     */
    private fun generateExpectedSerial(deviceAndroidId: String): String {
        val input = deviceAndroidId + SALT
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        val hex = digest.joinToString("") { "%02x".format(it) }

        // Take 8-char slice from hex, then map to uppercase alphanumeric
        val slice = hex.slice(8..15)
        val num = slice.toInt(16)
        val sb = StringBuilder()
        var n = num
        for (i in 0 until 8) {
            sb.insert(0, CHAR_SET[n % CHAR_SET.size])
            n /= CHAR_SET.size
            if (n == 0) break
        }
        var result = sb.toString()
        while (result.length < 8) result = "A" + result
        return result.substring(0, 8)
    }

    data class ValidationResult(val isSuccess: Boolean, val message: String)
}
