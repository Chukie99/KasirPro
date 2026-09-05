package com.kasirpro.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

/**
 * BackupHelper — exports the Room database file (.db) to Downloads
 * Works on Android 7..14: MediaStore on API 29+, fallback file API below.
 * Restore: closes Room via Hilt-provided AppDatabase before overwriting.
 */
object BackupHelper {

    fun exportDatabase(context: Context): String? {
        return try {
            val dbFile = File(context.getDatabasePath("kasirpro.db").path)
            if (!dbFile.exists()) {
                Toast.makeText(context, "Database tidak ditemukan.", Toast.LENGTH_SHORT).show()
                return null
            }
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
            val backupName = "KasirPro_Backup_${timestamp}.db"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, backupName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/x-sqlite3")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: throw IllegalStateException("MediaStore insert failed")
                resolver.openOutputStream(uri)?.use { out ->
                    dbFile.inputStream().use { inp -> inp.copyTo(out) }
                }
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                Toast.makeText(context, "Backup berhasil! ($backupName)", Toast.LENGTH_LONG).show()
                uri.toString()
            } else {
                @Suppress("DEPRECATION")
                val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloads.exists()) downloads.mkdirs()
                val backupFile = File(downloads, backupName)
                dbFile.copyTo(backupFile, overwrite = true)
                Toast.makeText(context, "Backup berhasil! (${backupName})", Toast.LENGTH_LONG).show()
                backupFile.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Backup gagal: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    /**
     * Restores DB from picked .db Uri. Caller must restart app after success.
     * Tries to close Room database instance held by Hilt before overwriting file.
     */
    fun restoreDatabase(context: Context, uri: Uri, closeDb: (() -> Unit)? = null): Boolean {
        return try {
            val dbFile = File(context.getDatabasePath("kasirpro.db").path)
            val tmp = File(context.cacheDir, "restore_temp.db")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tmp).use { output -> input.copyTo(output) }
            } ?: throw IllegalStateException("Gagal baca file backup")

            // Validate header: SQLite format
            tmp.inputStream().use { inp ->
                val header = ByteArray(16)
                inp.read(header)
                val magic = String(header, Charsets.UTF_8)
                if (!magic.startsWith("SQLite format")) {
                    throw IllegalStateException("File bukan database SQLite valid")
                }
            }

            // Close Room if caller provided hook (Hilt singleton)
            try { closeDb?.invoke() } catch (_: Exception) {}

            // Also try reflective close via application holder
            try {
                val app = context.applicationContext
                val field = app.javaClass.getDeclaredField("database") // best-effort
                field.isAccessible = true
                (field.get(app) as? androidx.room.RoomDatabase)?.close()
            } catch (_: Exception) {}

            tmp.copyTo(dbFile, overwrite = true)
            tmp.delete()
            Toast.makeText(context, "Restore berhasil! Restart aplikasi.", Toast.LENGTH_LONG).show()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Restore gagal: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }

    val restorePicker = androidx.activity.result.contract.ActivityResultContracts.GetContent()
}
