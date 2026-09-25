package com.kjbilling.app.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.kjbilling.app.util.ImageSizing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Stores the single business logo as a normalized PNG in app-private storage:
 *
 *   picked Uri ──decode (sampled) → EXIF rotate → scale ≤ MAX_DIMENSION──▶ files/branding/logo.png
 *
 * Kept as a file (not a DB column) so no Room migration is needed. PNG keeps transparency.
 */
class LogoStorage(context: Context) {

    private val appContext = context.applicationContext
    private val dir = File(appContext.filesDir, DIR_NAME)
    private val file = File(dir, FILE_NAME)

    // Bumped on every save/remove so UI can reload the preview (check logoFile() for presence).
    private val _version = MutableStateFlow(0L)
    val version: StateFlow<Long> = _version.asStateFlow()

    /** Returns the logo file, or null when none is set. */
    fun logoFile(): File? = file.takeIf { it.exists() && it.length() > 0 }

    /** Decodes the stored logo. Returns null when missing or unreadable. */
    suspend fun loadBitmap(): Bitmap? = withContext(Dispatchers.IO) {
        val f = logoFile() ?: return@withContext null
        runCatching { BitmapFactory.decodeFile(f.absolutePath) }.getOrNull()
    }

    /** Decodes, normalizes and saves the image at [uri]. Replaces any existing logo atomically. */
    suspend fun saveLogo(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = decodeNormalized(uri)
            try {
                writeAtomically(bitmap)
            } finally {
                bitmap.recycle()
            }
            _version.update { it + 1 }
        }
    }

    /** Replaces the logo with an already-normalized PNG (used by restore). */
    suspend fun replaceLogo(source: File): Unit = withContext(Dispatchers.IO) {
        if (!dir.exists() && !dir.mkdirs()) {
            throw IOException("Cannot create logo directory")
        }
        source.copyTo(file, overwrite = true)
        _version.update { it + 1 }
    }

    suspend fun removeLogo(): Unit = withContext(Dispatchers.IO) {
        file.delete()
        _version.update { it + 1 }
    }

    private fun decodeNormalized(uri: Uri): Bitmap {
        val resolver = appContext.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        // decodeStream returns null in bounds-only mode, so check the stream itself, not the result.
        val boundsStream = resolver.openInputStream(uri) ?: throw IOException("Cannot open selected image")
        boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IOException("Selected file is not a supported image")
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = ImageSizing.sampleSize(bounds.outWidth, bounds.outHeight, MAX_DIMENSION)
        }
        val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw IOException("Selected file is not a supported image")

        val rotated = applyExifRotation(decoded, readExifRotation(uri))
        val (w, h) = ImageSizing.scaleToMax(rotated.width, rotated.height, MAX_DIMENSION)
        if (w == rotated.width && h == rotated.height) {
            return rotated
        }

        val scaled = Bitmap.createScaledBitmap(rotated, w, h, true)
        if (scaled !== rotated) {
            rotated.recycle()
        }
        return scaled
    }

    private fun readExifRotation(uri: Uri): Int {
        return runCatching {
            appContext.contentResolver.openInputStream(uri)?.use { stream ->
                when (ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        }.getOrDefault(0)
    }

    private fun applyExifRotation(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) {
            return bitmap
        }

        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated !== bitmap) {
            bitmap.recycle()
        }
        return rotated
    }

    private fun writeAtomically(bitmap: Bitmap) {
        if (!dir.exists() && !dir.mkdirs()) {
            throw IOException("Cannot create logo directory")
        }

        val temp = File(dir, "$FILE_NAME.tmp")
        try {
            FileOutputStream(temp).use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, out)) {
                    throw IOException("Failed to encode logo")
                }
            }
            if (!temp.renameTo(file)) {
                throw IOException("Failed to save logo")
            }
        } finally {
            temp.delete()
        }
    }

    companion object {
        private const val DIR_NAME = "branding"
        private const val FILE_NAME = "logo.png"
        private const val MAX_DIMENSION = 512
        private const val PNG_QUALITY = 100 // Ignored by PNG (lossless); required by API.
    }
}
