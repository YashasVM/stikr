package com.stikr.app.data

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream
import kotlin.math.max

/**
 * Loads a photo from the photo picker as a bitmap that fits [maxDimension],
 * applying EXIF rotation so portrait shots do not arrive sideways.
 * Returns null for anything unreadable rather than throwing.
 */
object ImageLoader {

    fun load(context: Context, uri: Uri, maxDimension: Int = 1024): Bitmap? = try {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { stream -> BitmapFactory.decodeStream(stream, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            null
        } else {
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
            decoded?.let { rotate(resolver, uri, it) }
        }
    } catch (_: Exception) {
        null
    }

    private fun sampleSize(width: Int, height: Int, maxDimension: Int): Int {
        var sample = 1
        var longest = max(width, height)
        while (longest / 2 >= maxDimension) {
            sample *= 2
            longest /= 2
        }
        return sample
    }

    private fun rotate(resolver: ContentResolver, uri: Uri, bitmap: Bitmap): Bitmap {
        val degrees = readRotation(resolver.openInputStream(uri))
        if (degrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated !== bitmap) bitmap.recycle()
        return rotated
    }

    private fun readRotation(stream: InputStream?): Float = try {
        stream?.use { input ->
            when (ExifInterface(input).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
    } catch (_: Exception) {
        0f
    }
}
