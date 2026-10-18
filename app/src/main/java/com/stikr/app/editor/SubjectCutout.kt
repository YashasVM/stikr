package com.stikr.app.editor

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Auto cut-out: ML Kit's on-device subject segmentation.
 *
 * The model is fetched by Google Play services on first use, so the first
 * cut-out can take a moment. Everything fails soft: when Play services or the
 * model are unavailable, the caller keeps the original photo instead.
 */
object SubjectCutout {

    private const val TIMEOUT_MS = 90_000L

    @Volatile
    private var segmenter: SubjectSegmenter? = null

    private fun segmenter(): SubjectSegmenter =
        segmenter ?: synchronized(this) {
            segmenter ?: SubjectSegmentation.getClient(
                SubjectSegmenterOptions.Builder()
                    .enableForegroundBitmap()
                    .build(),
            ).also { segmenter = it }
        }

    /** A cut-out with a transparent background, or null when unavailable. */
    suspend fun cut(bitmap: Bitmap): Bitmap? = try {
        withTimeoutOrNull(TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                val image = InputImage.fromBitmap(bitmap, 0)
                segmenter().process(image)
                    .addOnSuccessListener { result -> continuation.resume(result.foregroundBitmap) }
                    .addOnFailureListener { continuation.resume(null) }
            }
        }
    } catch (_: Exception) {
        null
    }
}
