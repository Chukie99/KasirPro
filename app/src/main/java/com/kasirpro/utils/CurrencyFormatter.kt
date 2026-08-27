package com.kasirpro.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Format Long rupiah → "Rp 1.500.000"
 */
object CurrencyFormatter {

    private val locale = Locale("id", "ID")

    fun formatRupiah(amount: Long): String {
        val fmt = NumberFormat.getCurrencyInstance(locale)
        return fmt.format(amount)
    }

    fun formatRupiah(amount: Double): String = formatRupiah(amount.toLong())

    fun parseRupiah(text: String): Long {
        val clean = text.replace("Rp", "").replace(".", "").replace(",", "")
            .replace(" ", "").trim()
        return clean.toLongOrNull() ?: 0L
    }
}
