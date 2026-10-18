package com.stikr.app.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.stikr.app.data.StickerPack
import com.stikr.app.render.StickerCodec
import com.stikr.app.render.StickerRenderer
import com.stikr.app.ui.theme.StikrTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Renders whole screens off-device into build/ui-renders/ so the theme can be
 * reviewed without installing the app. A contact sheet, not an assertion suite.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h880dp-xxhdpi")
class ScreenRenderTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val out = File("build/ui-renders").apply { mkdirs() }
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun homeEmptyLight() {
        compose.setContent {
            StikrTheme(darkTheme = false) {
                HomeScreen(packs = emptyList(), onOpenPack = {}, onCreatePack = {}, onMakeSticker = {})
            }
        }
        settle()
        capture("home-empty-light")
    }

    @Test
    fun homePacksLight() {
        val packs = listOf(packFixture("pack-2", "Faces"), packFixture("pack-1", "My stickers"))
        compose.setContent {
            StikrTheme(darkTheme = false) {
                HomeScreen(packs = packs, onOpenPack = {}, onCreatePack = {}, onMakeSticker = {})
            }
        }
        settle()
        capture("home-packs-light")
    }

    @Test
    fun homePacksDark() {
        val packs = listOf(packFixture("pack-2", "Faces"), packFixture("pack-1", "My stickers"))
        compose.setContent {
            StikrTheme(darkTheme = true) {
                HomeScreen(packs = packs, onOpenPack = {}, onCreatePack = {}, onMakeSticker = {})
            }
        }
        settle()
        capture("home-packs-dark")
    }

    @Test
    fun packScreenLight() {
        val pack = packFixture("pack-1", "My stickers")
        compose.setContent {
            StikrTheme(darkTheme = false) {
                PackScreen(
                    pack = pack,
                    onBack = {},
                    onRename = {},
                    onDelete = {},
                    onAddSticker = {},
                    onDeleteSticker = {},
                    onShareSticker = {},
                    onExport = {},
                )
            }
        }
        settle()
        capture("pack-light")
    }

    @Test
    fun packScreenDark() {
        val pack = packFixture("pack-1", "My stickers")
        compose.setContent {
            StikrTheme(darkTheme = true) {
                PackScreen(
                    pack = pack,
                    onBack = {},
                    onRename = {},
                    onDelete = {},
                    onAddSticker = {},
                    onDeleteSticker = {},
                    onShareSticker = {},
                    onExport = {},
                )
            }
        }
        settle()
        capture("pack-dark")
    }

    @Test
    fun editorScreenLight() {
        editorScreen(dark = false)
        capture("editor-light")
    }

    @Test
    fun editorBottomLight() {
        editorScreen(dark = false)
        capture("editor-light-bottom", scrollTo = "Position")
    }

    @Test
    fun editorScreenDark() {
        editorScreen(dark = true)
        capture("editor-dark")
    }

    @Test
    fun editorBottomDark() {
        editorScreen(dark = true)
        capture("editor-dark-bottom", scrollTo = "Position")
    }

    private fun editorScreen(dark: Boolean) {
        val sticker = sampleSticker(0xFFFFAB5E.toInt())
        val state = EditorState(
            packId = "pack-1",
            source = sticker,
            subject = sticker,
            preview = sticker,
            style = StickerRenderer.Style(outline = 10, caption = "hello", captionColor = StickerRenderer.CaptionColor.CHERRY),
        )
        compose.setContent {
            StikrTheme(darkTheme = dark) {
                EditorScreen(
                    state = state,
                    packName = "My stickers",
                    onPick = {},
                    onCutout = {},
                    onStyle = {},
                    onSave = {},
                    onBack = {},
                )
            }
        }
        settle()
    }

    @Test
    fun packScreenWhatsAppLight() {
        val packageManager = context.packageManager
        val whatsApp = android.content.pm.PackageInfo().apply { packageName = "com.whatsapp" }
        org.robolectric.Shadows.shadowOf(packageManager).installPackage(whatsApp)

        val pack = packFixture("pack-1", "My stickers")
        compose.setContent {
            StikrTheme(darkTheme = false) {
                PackScreen(
                    pack = pack,
                    onBack = {},
                    onRename = {},
                    onDelete = {},
                    onAddSticker = {},
                    onDeleteSticker = {},
                    onShareSticker = {},
                    onExport = {},
                )
            }
        }
        settle()
        capture("pack-whatsapp-light")
    }

    @Test
    fun newPackDialogLight() {
        compose.setContent {
            StikrTheme(darkTheme = false) {
                NameDialog(
                    title = "New pack",
                    confirmLabel = "Create",
                    initial = "",
                    onDismiss = {},
                    onConfirm = {},
                )
            }
        }
        settle()

        val dialog = org.robolectric.shadows.ShadowDialog.getLatestDialog()
        val view = dialog.window?.decorView
        if (view != null && view.width > 0 && view.height > 0) {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            File(out, "dialog-light.png").outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }

    // ------------------------------------------------------------- fixtures

    private fun sampleSticker(circle: Int): Bitmap {
        val photo = Bitmap.createBitmap(600, 800, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(photo)
        canvas.drawColor(Color.rgb(242, 240, 248))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = circle
        canvas.drawCircle(300f, 260f, 170f, paint)
        paint.color = Color.rgb(66, 62, 58)
        canvas.drawRoundRect(150f, 480f, 450f, 700f, 60f, 60f, paint)
        return StickerRenderer.render(
            photo,
            StickerRenderer.Style(outline = 10, caption = "hi"),
        )
    }

    private fun packFixture(id: String = "pack-1", name: String = "My stickers"): StickerPack {
        val dir = File(context.filesDir, "packs/$id").apply { mkdirs() }
        val circles = listOf(
            0xFFFFAB5E.toInt(),
            0xFF56C09E.toInt(),
            0xFF7EA6F0.toInt(),
            0xFFF07E9C.toInt(),
            0xFFD6C05C.toInt(),
        )
        val names = circles.mapIndexed { index, circle ->
            val fileName = "s${index + 1}.webp"
            File(dir, fileName).writeBytes(StickerCodec.encodeWebp(sampleSticker(circle)))
            fileName
        }
        return StickerPack(
            id = id,
            name = name,
            author = "stikr",
            createdAt = System.currentTimeMillis(),
            stickers = names,
        )
    }

    // ------------------------------------------------------------- plumbing

    /** Lets async work (sticker files, produceState) land before capturing. */
    private fun settle() {
        repeat(3) {
            Thread.sleep(200)
            compose.waitForIdle()
            compose.mainClock.advanceTimeBy(32)
            compose.waitForIdle()
        }
    }

    /**
     * captureToImage() needs PixelCopy, which Robolectric doesn't implement, so
     * the content view is drawn straight onto a bitmap instead.
     */
    private fun capture(name: String, scrollTo: String? = null) {
        if (scrollTo != null) {
            compose.onNodeWithText(scrollTo).performScrollTo()
            settle()
        }
        val view: View = compose.activity.findViewById(android.R.id.content)
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        File(out, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
