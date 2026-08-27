package com.kasirpro.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Format / parse date & time in Indonesian locale.
 */
object DateFormatter {

    private val locale = Locale("id", "ID")

    fun nowTimestamp(): Long = System.currentTimeMillis()

    fun formatDate(timestamp: Long, pattern: String = "dd MMMM yyyy"): String {
        val sdf = SimpleDateFormat(pattern, locale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String =
        formatDate(timestamp, "dd MMMM yyyy HH:mm")

    fun formatTime(timestamp: Long): String =
        formatDate(timestamp, "HH:mm")

    fun formatForCSV(timestamp: Long): String =
        formatDate(timestamp, "yyyy-MM-dd HH:mm:ss")
}
