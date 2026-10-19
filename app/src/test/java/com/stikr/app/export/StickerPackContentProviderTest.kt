package com.stikr.app.export

import android.content.Context
import android.content.pm.ProviderInfo
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.stikr.app.data.PackRepository
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.FileNotFoundException

@RunWith(RobolectricTestRunner::class)
class StickerPackContentProviderTest {

    private data class Fixture(
        val context: Context,
        val provider: StickerPackContentProvider,
        val repo: PackRepository,
        val authority: String,
    )

    private fun fixture(): Fixture {
        val context = ApplicationProvider.getApplicationContext<Context>()
        File(context.filesDir, "packs").deleteRecursively()
        val repo = PackRepository(File(context.filesDir, "packs"))
        val authority = "${context.packageName}.stickercontentprovider"
        val provider = StickerPackContentProvider()
        provider.attachInfo(
            context,
            ProviderInfo().apply {
                this.authority = authority
                packageName = context.packageName
            },
        )
        return Fixture(context, provider, repo, authority)
    }

    @Test
    fun metadataListsPacksWithStickerJson() {
        val (_, provider, repo, authority) = fixture()
        val pack = repo.createPack("Test pack")
        repo.addSticker(pack.id, byteArrayOf(1, 2, 3), fileName = "stk_one.webp")

        val cursor = provider.query(Uri.parse("content://$authority/metadata"), null, null, null, null)
        assertEquals(1, cursor.count)
        cursor.moveToFirst()
        assertEquals(pack.id, cursor.getString(cursor.getColumnIndexOrThrow("identifier")))
        assertEquals("Test pack", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        assertEquals("tray.webp", cursor.getString(cursor.getColumnIndexOrThrow("tray_image_file")))

        val stickers = JSONArray(cursor.getString(cursor.getColumnIndexOrThrow("stickers")))
        assertEquals(1, stickers.length())
        assertEquals("stk_one.webp", stickers.getJSONObject(0).getString("image_file"))
    }

    @Test
    fun singlePackMetadataQueryWorks() {
        val (_, provider, repo, authority) = fixture()
        val pack = repo.createPack("Solo")

        val cursor = provider.query(Uri.parse("content://$authority/metadata/${pack.id}"), null, null, null, null)
        assertEquals(1, cursor.count)

        val missing = provider.query(Uri.parse("content://$authority/metadata/pack_other"), null, null, null, null)
        assertEquals(0, missing.count)
    }

    @Test
    fun cachedProviderObservesMetadataAndAssetChanges() {
        val (_, provider, repo, authority) = fixture()
        val pack = repo.createPack("Before")
        repo.addSticker(pack.id, byteArrayOf(1, 2, 3), fileName = "stk_one.webp")
        val uri = Uri.parse("content://$authority/metadata/${pack.id}")
        provider.query(uri, null, null, null, null).close()
        repo.renamePack(pack.id, "After!")
        provider.query(uri, null, null, null, null).use { cursor ->
            cursor.moveToFirst()
            assertEquals("After!", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        }
        val asset = Uri.parse("content://$authority/stickers_asset/${pack.id}/stk_one.webp")
        provider.openFile(asset, "r").close()
        // File loss must not serve a cached asset, even when pack.json is unchanged.
        repo.stickerFile(pack.id, "stk_one.webp")!!.delete()
        try {
            provider.openFile(asset, "r")
            fail("a deleted file must not be served from cached metadata")
        } catch (_: FileNotFoundException) {
            // expected
        }
    }

    @Test
    fun openFileServesListedStickersOnly() {
        val (_, provider, repo, authority) = fixture()
        val pack = repo.createPack("Files")
        repo.addSticker(pack.id, byteArrayOf(1, 3, 3, 7), fileName = "stk_two.webp")

        val descriptor = provider.openFile(
            Uri.parse("content://$authority/stickers_asset/${pack.id}/stk_two.webp"),
            "r",
        )
        assertNotNull(descriptor)
        descriptor.close()

        try {
            provider.openFile(Uri.parse("content://$authority/stickers_asset/${pack.id}/missing.webp"), "r")
            fail("an unlisted file must not be served")
        } catch (_: FileNotFoundException) {
            // expected
        }
    }
}
