package com.example.wardeobe.util

import android.content.Context
import kotlinx.coroutines.CancellationException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

object ImageCompressor {
    /**
     * Compresses and downscales an image from the given [uri].
     * Returns a ByteArray of the compressed JPEG image, or null on failure.
     *
     * @param maxDimension Maximum width or height in pixels. Maintains aspect ratio.
     * @param quality JPEG compression quality (0-100). Defaults to 85.
     */
    fun compressImage(
        context: Context,
        uri: Uri,
        maxDimension: Int = 1536,
        quality: Int = 85
    ): ByteArray? {
        return try {
            // First, decode bounds only to get original dimensions
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri).use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
            var (width, height) = options.outWidth to options.outHeight
            if (width <= 0 || height <= 0) return null
            // Determine scaling factor
            val scale = if (width > height) {
                maxDimension.toFloat() / width
            } else {
                maxDimension.toFloat() / height
            }.coerceAtMost(1f)
            val targetWidth = (width * scale).toInt()
            val targetHeight = (height * scale).toInt()
            // Decode the actual bitmap with sampling
            val sampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = context.contentResolver.openInputStream(uri).use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null
            // Scale to exact target size
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
            if (scaledBitmap != bitmap) {
                bitmap.recycle()
            }
            // Compress to JPEG
            val output = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
            scaledBitmap.recycle()
            output.toByteArray()
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        val (height, width) = options.outHeight to options.outWidth
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
