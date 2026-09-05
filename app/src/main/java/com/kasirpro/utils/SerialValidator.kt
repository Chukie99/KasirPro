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
     * from the 8-char Device ID shown to user (NOT raw ANDROID_ID).
     * MUST match admin/serial-generator.html: SHA256(DeviceID + SALT) slice 8..16 base36.
     */
    fun validate(serial: String, deviceId: String): ValidationResult {
        val trimmed = serial.trim().uppercase()
        if (trimmed.isEmpty()) {
            return ValidationResult(false, "Serial Number tidak boleh kosong")
        }
        if (trimmed.length != 8) {
            return ValidationResult(false, "Serial Number harus 8 karakter")
        }
        if (!trimmed.all { it in 'A'..'Z' || it in '0'..'9' }) {
            return ValidationResult(false, "Serial hanya boleh huruf A-Z dan angka 0-9")
        }

        val expected = generateExpectedSerial(deviceId.trim().uppercase())
        return if (trimmed == expected) {
            ValidationResult(true, "Aktivasi berhasil!")
        } else {
            ValidationResult(false, "Serial Number tidak valid!")
        }
    }

    /**
     * Generates the expected serial for a given 8-char Device ID,
     * replicating the HTML generator's exact algorithm.
     * Uses Long to avoid overflow (0xFFFFFFFF > Int.MAX_VALUE).
     */
    fun generateExpectedSerial(deviceId: String): String {
        val input = deviceId + SALT
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        val hex = digest.joinToString("") { "%02x".format(it) }

        // Take 8-char slice from hex, then map to uppercase alphanumeric (base36)
        val slice = hex.slice(8..15)
        var n = slice.toLong(16)
        val sb = StringBuilder()
        for (i in 0 until 8) {
            sb.insert(0, CHAR_SET[(n % CHAR_SET.size).toInt()])
            n /= CHAR_SET.size
            if (n == 0L) break
        }
        var result = sb.toString()
        while (result.length < 8) result = "A" + result
        return result.substring(0, 8)
    }

    /** Public helper for testing — exposed for unit test vector check */
    fun generateForTest(deviceId: String): String = generateExpectedSerial(deviceId)

    data class ValidationResult(val isSuccess: Boolean, val message: String)
}
