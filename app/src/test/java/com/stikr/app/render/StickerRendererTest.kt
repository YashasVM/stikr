package com.stikr.app.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Renders stickers off-device. Images are written to build/sticker-renders/
 * so the look can be reviewed without installing the app.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "xxhdpi")
class StickerRendererTest {

    private val out = File("build/sticker-renders").apply { mkdirs() }

    /** A stand-in photo: warm head, green body, on a light background. */
    private fun samplePhoto(width: Int = 640, height: Int = 900): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.rgb(238, 240, 250))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.rgb(255, 171, 94)
        canvas.drawCircle(width / 2f, height * 0.34f, width * 0.30f, paint)
        paint.color = Color.rgb(86, 192, 158)
        canvas.drawRoundRect(width * 0.18f, height * 0.55f, width * 0.82f, height * 0.96f, 48f, 48f, paint)
        return bitmap
    }

    @Test
    fun rendersFullStickerAt512() {
        val sticker = StickerRenderer.render(
            samplePhoto(),
            StickerRenderer.Style(
                outline = 10,
                caption = "hello!",
                captionColor = StickerRenderer.CaptionColor.CREAM,
            ),
        )

        assertEquals(StickerRenderer.SIZE, sticker.width)
        assertEquals(StickerRenderer.SIZE, sticker.height)
        // corners stay transparent: a sticker is a cut-out, not a tile
        assertEquals(0, Color.alpha(sticker.getPixel(2, 2)))
        assertEquals(0, Color.alpha(sticker.getPixel(StickerRenderer.SIZE - 3, 2)))
        save("sticker_full.png", sticker)
    }

    @Test
    fun inkOutlinePaintsTheSilhouetteInInk() {
        val sticker = StickerRenderer.render(
            samplePhoto(),
            StickerRenderer.Style(outline = 14, outlineColor = StickerRenderer.OutlineColor.INK),
        )
        // straight down from centre: image bottom -> ink outline ring -> transparent
        var outlinePixels = 0
        for (y in StickerRenderer.SIZE / 2 until StickerRenderer.SIZE) {
            val pixel = sticker.getPixel(StickerRenderer.SIZE / 2, y)
            if (pixel == StickerRenderer.OutlineColor.INK.color) outlinePixels++
        }
        assertTrue("expected an ink outline band, found $outlinePixels ink pixels", outlinePixels > 6)
        save("sticker_ink_outline.png", sticker)
    }

    @Test
    fun keepsUnderWhatsAppSizeLimit() {
        val sticker = StickerRenderer.render(
            samplePhoto(),
            StickerRenderer.Style(outline = 4, caption = "hi"),
        )
        val bytes = StickerCodec.encodeWebp(sticker)
        assertTrue(
            "sticker must stay under ${StickerCodec.MAX_STICKER_BYTES} bytes, was ${bytes.size}",
            bytes.size <= StickerCodec.MAX_STICKER_BYTES,
        )
    }

    @Test
    fun rendersStyleVariants() {
        val photo = samplePhoto()
        val variants = mapOf(
            "sticker_no_outline" to StickerRenderer.Style(outline = 0, caption = "no outline"),
            "sticker_top_caption" to StickerRenderer.Style(
                outline = 12,
                caption = "top",
                captionPosition = StickerRenderer.CaptionPosition.TOP,
                captionColor = StickerRenderer.CaptionColor.BUTTER,
            ),
            "sticker_cherry_caption" to StickerRenderer.Style(
                outline = 6,
                caption = "cherry",
                captionColor = StickerRenderer.CaptionColor.CHERRY,
            ),
            "sticker_ink_caption" to StickerRenderer.Style(
                outline = 8,
                caption = "ink",
                captionColor = StickerRenderer.CaptionColor.INK,
            ),
            "sticker_long_caption" to StickerRenderer.Style(
                outline = 6,
                caption = "a rather long caption that has to shrink",
            ),
        )
        for ((name, style) in variants) {
            val sticker = StickerRenderer.render(photo, style)
            assertEquals(StickerRenderer.SIZE, sticker.width)
            save("$name.png", sticker)
        }
    }

    @Test
    fun trayIconIs96AndUnderLimit() {
        val sticker = StickerRenderer.render(samplePhoto(), StickerRenderer.Style())
        val tray = StickerRenderer.renderTray(sticker)
        assertEquals(96, tray.width)
        assertEquals(96, tray.height)
        val bytes = StickerCodec.encodeWebp(tray, StickerCodec.MAX_ICON_BYTES)
        assertTrue(
            "tray icon must stay under ${StickerCodec.MAX_ICON_BYTES} bytes, was ${bytes.size}",
            bytes.size <= StickerCodec.MAX_ICON_BYTES,
        )
        save("tray.png", tray)
    }

    @Test
    fun trimCropsTransparentBorders() {
        val source = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(source)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.RED }
        canvas.drawCircle(200f, 200f, 60f, paint)

        val trimmed = StickerRenderer.trimTransparent(source)
        assertTrue("trimmed should be smaller: ${trimmed.width}", trimmed.width < source.width)
        assertTrue(trimmed.width >= 120)

        val empty = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        assertEquals(100, StickerRenderer.trimTransparent(empty).width)
    }

    private fun save(name: String, bitmap: Bitmap) {
        File(out, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
