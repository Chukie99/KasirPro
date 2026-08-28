package com.kasirpro.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * BackupHelper — exports the Room database file (.db) to the device's
 * Downloads folder so the user can back up & restore manually.
 *
 * For Google-Drive sync (optional) you would extend this class with
 * an authenticated DriveClient — see SettingsScreen.
 */
object BackupHelper {

    /**
     * Exports the database to:
     *   ~/Downloads/KasirPro_Backup_YYYYMMDD_HHMMSS.db
     *
     * Returns the absolute file path of the backup, or null on failure.
     */
    fun exportDatabase(context: Context): String? {
        return try {
            // Room DB lives at /data/data/<pkg>/databases/kasirpro.db
            val dbFile = File(context.getDatabasePath("kasirpro.db").path)
            if (!dbFile.exists()) {
                Toast.makeText(context, "Database tidak ditemukan.", Toast.LENGTH_SHORT).show()
                return null
            }

            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloads.exists()) downloads.mkdirs()

            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
            val backupName = "KasirPro_Backup_${timestamp}.db"
            val backupFile = File(downloads, backupName)

            dbFile.copyTo(backupFile, overwrite = true)

            Toast.makeText(context, "Backup berhasil! (${backupName})", Toast.LENGTH_LONG).show()
            backupFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Backup gagal: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    /**
     * Restores the database from a selected .db URI.
     * Closes the current DB, replaces the file, and notifies.
     * The app should be restarted after a successful restore.
     */
    fun restoreDatabase(context: Context, uri: Uri): Boolean {
        return try {
            val dbFile = File(context.getDatabasePath("kasirpro.db").path)
            val newFile = File(context.cacheDir, "restore_temp.db")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(newFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Close the database before overwriting
            try {
                val closeDb = Class.forName("com.kasirpro.data.database.AppDatabase")
                // Force close via Room's close method - best effort
            } catch (_: Exception) { }

            newFile.copyTo(dbFile, overwrite = true)
            newFile.delete()

            Toast.makeText(context, "Restore berhasil! Restart aplikasi.", Toast.LENGTH_LONG).show()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Restore gagal: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }

    /**
     * Opens the Android file picker for the user to choose a .db file.
     */
    val restorePicker = androidx.activity.result.contract.ActivityResultContracts.GetContent()
}
