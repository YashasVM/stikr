package com.stikr.app.export

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.stikr.app.data.PackRepository
import com.stikr.app.data.StickerPack
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException

/**
 * Serves packs to WhatsApp. When the user taps "Add to WhatsApp", WhatsApp
 * queries this provider for pack metadata and then reads the sticker files
 * through it. The path layout is fixed by WhatsApp's sticker pack contract.
 */
class StickerPackContentProvider : ContentProvider() {

    private val repo: PackRepository by lazy {
        PackRepository(File(requireNotNull(context).filesDir, "packs"))
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        val cursor = MatrixCursor(COLUMNS)
        val segments = uri.pathSegments
        if (segments.firstOrNull() != METADATA) return cursor

        val packs = repo.listPacks()
        val target = segments.getOrNull(1)
        val selected: List<StickerPack> = when {
            target == null -> packs
            // WhatsApp can ask for "the rest" as metadata/<startIndex>.
            target.isNotEmpty() && target.all { it.isDigit() } -> packs.drop(target.toInt())
            else -> packs.filter { it.id == target }
        }
        selected.forEach { cursor.addRow(packRow(it)) }
        return cursor
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r") throw FileNotFoundException("stikr only serves read-only stickers")
        val segments = uri.pathSegments
        val file: File? = when (segments.firstOrNull()) {
            STICKERS_ASSET -> repo.stickerFile(segments.getOrNull(1).orEmpty(), segments.getOrNull(2).orEmpty())
            STICKER_PACK_ICON -> repo.stickerFile(segments.getOrNull(1).orEmpty(), PackRepository.TRAY_FILE)
            else -> null
        }
        if (file == null || !file.isFile) throw FileNotFoundException("No such sticker: $uri")
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun getType(uri: Uri): String =
        "vnd.android.cursor.dir/vnd.${requireNotNull(context).packageName}.stickercontentprovider." +
            uri.pathSegments.joinToString(".")

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0

    // ------------------------------------------------------------- internals

    /** One metadata row, in the column order WhatsApp expects. */
    private fun packRow(pack: StickerPack): Array<Any?> = arrayOf(
        pack.id,
        pack.name,
        pack.author,
        pack.trayImage ?: PackRepository.TRAY_FILE,
        "", // publisher_email
        "", // publisher_website
        "", // privacy_policy_website
        "", // license_agreement_website
        IMAGE_DATA_VERSION,
        // sent over the wire as booleans rather than words
        0,  // avoid_cache
        0,  // animated_sticker_pack
        stickersJson(pack),
    )

    private fun stickersJson(pack: StickerPack): String {
        val array = JSONArray()
        pack.stickers.take(PackRepository.MAX_WHATSAPP_STICKERS).forEach { fileName ->
            array.put(
                JSONObject()
                    .put("image_file", fileName)
                    .put("emojis", JSONArray(DEFAULT_EMOJIS)),
            )
        }
        return array.toString()
    }

    companion object {
        private const val METADATA = "metadata"
        private const val STICKERS_ASSET = "stickers_asset"
        private const val STICKER_PACK_ICON = "sticker_pack_icon"
        private const val IMAGE_DATA_VERSION = "1"
        private val DEFAULT_EMOJIS = listOf("\uD83D\uDE42") // 🙂
        private val COLUMNS = arrayOf(
            "identifier",
            "name",
            "publisher",
            "tray_image_file",
            "publisher_email",
            "publisher_website",
            "privacy_policy_website",
            "license_agreement_website",
            "image_data_version",
            "avoid_cache",
            "animated_sticker_pack",
            "stickers",
        )
    }
}
