package com.stikr.app.render

import android.graphics.Bitmap
import android.os.Build
import java.io.ByteArrayOutputStream

/**
 * Encodes stickers the way WhatsApp wants them: small WebP files.
 * Stickers must stay under 100 KB and pack icons under 50 KB,
 * so the quality walks down until the payload fits.
 */
object StickerCodec {

    const val MAX_STICKER_BYTES = 100 * 1024
    const val MAX_ICON_BYTES = 50 * 1024

    fun encodeWebp(bitmap: Bitmap, maxBytes: Int = MAX_STICKER_BYTES): ByteArray {
        var quality = 90
        var bytes = compress(bitmap, quality)
        while (bytes.size > maxBytes && quality > 30) {
            quality -= 15
            bytes = compress(bitmap, quality)
        }
        return bytes
    }

    fun encodePng(bitmap: Bitmap): ByteArray {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        return out.toByteArray()
    }

    private fun compress(bitmap: Bitmap, quality: Int): ByteArray {
        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }
        val out = ByteArrayOutputStream()
        bitmap.compress(format, quality, out)
        return out.toByteArray()
    }
}
