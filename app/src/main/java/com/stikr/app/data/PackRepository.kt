package com.stikr.app.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.random.Random

/**
 * Packs live as plain files in the app's private storage:
 *
 *   packs/
 *     pack_<id>/
 *       pack.json      — name, author, created, sticker file list
 *       stk_<id>.webp  — 512x512 sticker images
 *       tray.webp      — 96x96 pack icon, written when exporting
 *
 * Plain files and JSON keep the app dependency-free and the data inspectable.
 */
class PackRepository(private val root: File) {

    private val lock = Any()

    fun listPacks(): List<StickerPack> = synchronized(lock) {
        root.listFiles { file -> file.isDirectory }
            ?.mapNotNull { readPack(it) }
            ?.sortedByDescending { it.createdAt }
            ?: emptyList()
    }

    fun getPack(id: String): StickerPack? = synchronized(lock) { readPack(File(root, id)) }

    fun createPack(name: String, author: String = DEFAULT_AUTHOR): StickerPack = synchronized(lock) {
        val pack = StickerPack(
            id = newPackId(),
            name = name.trim().ifEmpty { "Untitled pack" },
            author = author,
            createdAt = System.currentTimeMillis(),
            stickers = emptyList(),
        )
        writePack(pack)
        pack
    }

    fun renamePack(id: String, name: String): StickerPack? = synchronized(lock) {
        val pack = readPack(File(root, id)) ?: return null
        if (name.isBlank()) return pack
        pack.copy(name = name.trim()).also { writePack(it) }
    }

    fun deletePack(id: String): Boolean = synchronized(lock) {
        File(root, id).deleteRecursively()
    }

    fun addSticker(packId: String, bytes: ByteArray, fileName: String = newStickerName()): StickerPack? =
        synchronized(lock) {
            val pack = readPack(File(root, packId)) ?: return null
            val file = File(root, packId).resolve(fileName)
            file.parentFile?.mkdirs()
            file.writeBytes(bytes)
            pack.copy(stickers = pack.stickers + fileName).also { writePack(it) }
        }

    fun deleteSticker(packId: String, fileName: String): StickerPack? = synchronized(lock) {
        val pack = readPack(File(root, packId)) ?: return null
        if (!pack.stickers.contains(fileName)) return pack
        File(root, packId).resolve(fileName).delete()
        pack.copy(
            stickers = pack.stickers - fileName,
            trayImage = pack.trayImage?.takeUnless { it == fileName },
        ).also { writePack(it) }
    }

    fun setTrayImage(packId: String, bytes: ByteArray, fileName: String = TRAY_FILE): StickerPack? =
        synchronized(lock) {
            val pack = readPack(File(root, packId)) ?: return null
            File(root, packId).resolve(fileName).writeBytes(bytes)
            pack.copy(trayImage = fileName).also { writePack(it) }
        }

    /**
     * Resolves a sticker file for serving, but only when the pack still lists it.
     * This keeps the exported content provider from reading arbitrary paths.
     */
    fun stickerFile(packId: String, fileName: String): File? = synchronized(lock) {
        if (fileName.isBlank() || fileName.contains('/') || fileName.contains("..")) return null
        val pack = readPack(File(root, packId)) ?: return null
        if (!pack.stickers.contains(fileName) && pack.trayImage != fileName) return null
        File(root, packId).resolve(fileName).takeIf { it.isFile }
    }

    // ------------------------------------------------------------- internals

    private fun readPack(dir: File): StickerPack? {
        val meta = File(dir, PACK_FILE)
        if (!meta.isFile) return null
        return try {
            val json = JSONObject(meta.readText())
            val stickerNames = json.optJSONArray("stickers")?.let { array ->
                (0 until array.length())
                    .mapNotNull { index -> array.optString(index).takeIf { it.isNotEmpty() } }
                    .filter { File(dir, it).isFile }
            } ?: emptyList()
            StickerPack(
                id = json.getString("id"),
                name = json.optString("name", "Untitled pack"),
                author = json.optString("author", DEFAULT_AUTHOR),
                createdAt = json.optLong("createdAt", 0L),
                stickers = stickerNames,
                trayImage = json.optString("trayImage")
                    .takeIf { it.isNotEmpty() && File(dir, it).isFile },
            )
        } catch (_: Exception) {
            null // an unreadable pack.json is skipped rather than crashing the list
        }
    }

    private fun writePack(pack: StickerPack) {
        val dir = File(root, pack.id).apply { mkdirs() }
        val json = JSONObject()
            .put("id", pack.id)
            .put("name", pack.name)
            .put("author", pack.author)
            .put("createdAt", pack.createdAt)
            .put("stickers", JSONArray(pack.stickers))
            .put("trayImage", pack.trayImage ?: "")
        val target = File(dir, PACK_FILE)
        val temp = File(dir, "$PACK_FILE.tmp")
        temp.writeText(json.toString())
        if (!temp.renameTo(target)) {
            target.writeText(json.toString())
            temp.delete()
        }
    }

    private fun newPackId(): String =
        "pack_" + System.currentTimeMillis().toString(36) + "_" + Random.nextInt(0xFFFF).toString(16)

    private fun newStickerName(): String =
        "stk_" + System.currentTimeMillis().toString(36) + "_" + Random.nextInt(0xFFFF).toString(16) + ".webp"

    companion object {
        const val PACK_FILE = "pack.json"
        const val TRAY_FILE = "tray.webp"
        const val DEFAULT_AUTHOR = "stikr"
        const val MIN_WHATSAPP_STICKERS = 3
        const val MAX_WHATSAPP_STICKERS = 30
    }
}
