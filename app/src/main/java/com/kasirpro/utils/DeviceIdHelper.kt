package com.kasirpro.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import java.security.MessageDigest

/**
 * DeviceIdHelper — generates a stable, anonymized device ID.
 *
 * Algorithm (MUST match the admin HTML generator):
 *   DeviceID = SHA256(ANDROID_ID + "KasirProSalt2024")
 *   Take the first 8 hex characters → uppercase
 *
 * The ANDROID_ID is a 64-bit hex string unique per device+user. We never
 * expose the raw value — only the salted hash, so device fingerprinting
 * is irreversible.
 */
object DeviceIdHelper {

    private const val SALT = "KasirProSalt2024"

    /**
     * Returns an 8-character uppercase Device ID derived from the device's
     * Android ID and a fixed salt.
     *
     * @param context Application context (to access Settings.Secure)
     * @return 8-char uppercase alphanumeric string, e.g. "A7K3P9Q2"
     */
    @SuppressLint("HardwareIdentifiers")
    fun getDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "00000000"

        return hashDeviceId(androidId)
    }

    /**
     * Hashes a raw device ID string and returns 8 uppercase chars.
     */
    fun hashDeviceId(rawId: String): String {
        val input = rawId + SALT
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        // Convert to hex
        val hex = digest.joinToString("") { "%02x".format(it) }
        // Take first 8 hex chars → uppercase
        return hex.substring(0, 8).uppercase()
    }

    /**
     * Full SHA-256 of the salted device id — returns the complete hex digest.
     * Used by SerialValidator.
     */
    fun fullHash(rawAndroidId: String): String {
        val input = rawAndroidId + SALT
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
