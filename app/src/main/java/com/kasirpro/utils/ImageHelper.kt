package com.kasirpro.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileOutputStream

/**
 * ImageHelper — utilities for compressing & persisting product photos
 * into internal cache storage so the app stays 100% offline.
 *
 * - saveImageToInternal() compresses as JPEG (quality 85) to
 *   /files/product_images/<timestamp>.jpg and returns the absolute path.
 * - loadBitmap() loads a bitmap from a path, down-sampled to target size.
 */
object ImageHelper {
    private const val QUALITY = 85
    private const val MAX_SIZE_PX = 512

    fun saveImageToInternal(context: Context, bitmap: Bitmap): String {
        val dir = File(context.filesDir, "product_images")
        if (!dir.exists()) dir.mkdirs()
        val name = "img_" + System.currentTimeMillis() + ".jpg"
        val file = File(dir, name)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, out)
        }
        return file.absolutePath
    }

    fun deleteImage(path: String?) {
        if (path.isNullOrBlank()) return
        try { File(path).delete() } catch (_: Exception) {}
    }

    fun loadBitmap(path: String?, maxSizePx: Int = MAX_SIZE_PX): Bitmap? {
        if (path.isNullOrBlank()) return null
        return try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, opts)
            if (opts.outWidth <= 0) return null
            opts.inSampleSize = calculateInSampleSize(opts, maxSizePx, maxSizePx)
            opts.inJustDecodeBounds = false
            BitmapFactory.decodeFile(path, opts)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqW: Int, reqH: Int): Int {
        val h = options.outHeight
        val w = options.outWidth
        var size = 1
        if (h > reqH || w > reqW) {
            val halfH = h / 2
            val halfW = w / 2
            while (halfH / size >= reqH && halfW / size >= reqW) size *= 2
        }
        return size
    }

    /** Launcher to pick an image from gallery (call from an Activity Result API). */
    val galleryPicker = ActivityResultContracts.GetContent()
}
