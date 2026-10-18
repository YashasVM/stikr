package com.stikr.app.data

/** One pack of stickers, as stored on disk. */
data class StickerPack(
    val id: String,
    val name: String,
    val author: String,
    val createdAt: Long,
    val stickers: List<String>,
    val trayImage: String? = null,
) {
    val stickerCount: Int get() = stickers.size
}
