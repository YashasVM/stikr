package com.stikr.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class PackRepositoryTest {

    @get:Rule
    val temp = TemporaryFolder()

    private fun repo() = PackRepository(File(temp.root, "packs"))

    @Test
    fun createAddRenameDelete() {
        val repository = repo()
        val pack = repository.createPack("Cats")
        assertEquals("Cats", pack.name)
        assertEquals(0, pack.stickerCount)

        val withSticker = repository.addSticker(pack.id, byteArrayOf(1, 2, 3))
        assertNotNull(withSticker)
        assertEquals(1, withSticker!!.stickerCount)

        val renamed = repository.renamePack(pack.id, "Street cats")
        assertEquals("Street cats", renamed?.name)

        val afterDelete = repository.deleteSticker(pack.id, withSticker.stickers.first())
        assertEquals(0, afterDelete?.stickerCount)

        assertTrue(repository.deletePack(pack.id))
        assertTrue(repository.listPacks().isEmpty())
    }

    @Test
    fun packsSurviveReopen() {
        val pack = repo().createPack("Keepers")
        repo().addSticker(pack.id, byteArrayOf(9, 9, 9), fileName = "stk_a.webp")

        val reopened = repo().listPacks()
        assertEquals(1, reopened.size)
        assertEquals("Keepers", reopened.first().name)
        assertEquals(listOf("stk_a.webp"), reopened.first().stickers)
    }

    @Test
    fun stickerFileOnlyServesListedFiles() {
        val repository = repo()
        val pack = repository.createPack("Guarded")
        repository.addSticker(pack.id, byteArrayOf(5), fileName = "stk_ok.webp")
        File(repository.stickerFile(pack.id, "stk_ok.webp")!!.parentFile, "stk_unlisted.webp")
            .writeBytes(byteArrayOf(6))

        assertNotNull(repository.stickerFile(pack.id, "stk_ok.webp"))
        assertNull(repository.stickerFile(pack.id, "stk_unlisted.webp"))
        assertNull(repository.stickerFile(pack.id, "../pack.json"))
    }

    @Test
    fun unreadablePackIsSkipped() {
        val root = File(temp.root, "packs")
        val broken = File(root, "pack_broken").apply { mkdirs() }
        File(broken, PackRepository.PACK_FILE).writeText("{ not json !!")

        assertTrue(repo().listPacks().isEmpty())
    }

    @Test
    fun newestPackComesFirst() {
        val repository = repo()
        val older = repository.createPack("Older")
        Thread.sleep(5)
        repository.createPack("Newer")

        val listed = repository.listPacks()
        assertEquals(2, listed.size)
        assertEquals("Newer", listed.first().name)
        assertEquals("Older", listed.last().name)
        assertNotNull(repository.getPack(older.id))
    }
}
