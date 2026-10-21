package com.stikr.app.export

import android.content.Context
import android.content.pm.ProviderInfo
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.stikr.app.data.PackRepository
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
    fun metadataUsesWhatsAppColumns() {
        val (_, provider, repo, authority) = fixture()
        val pack = repo.createPack("Test pack")
        repo.addSticker(pack.id, byteArrayOf(1, 2, 3), fileName = "stk_one.webp")

        val cursor = provider.query(Uri.parse("content://$authority/metadata"), null, null, null, null)
        assertEquals(1, cursor.count)
        assertArrayEquals(arrayOf(
            "sticker_pack_identifier", "sticker_pack_name", "sticker_pack_publisher",
            "sticker_pack_icon", "android_play_store_link", "ios_app_download_link",
            "sticker_pack_publisher_email", "sticker_pack_publisher_website",
            "sticker_pack_privacy_policy_website", "sticker_pack_license_agreement_website",
            "image_data_version", "whatsapp_will_not_cache_stickers", "animated_sticker_pack",
        ), cursor.columnNames)
        cursor.moveToFirst()
        assertEquals(pack.id, cursor.getString(cursor.getColumnIndexOrThrow("sticker_pack_identifier")))
        assertEquals("Test pack", cursor.getString(cursor.getColumnIndexOrThrow("sticker_pack_name")))
        assertEquals("stikr", cursor.getString(cursor.getColumnIndexOrThrow("sticker_pack_publisher")))
        assertEquals("tray.webp", cursor.getString(cursor.getColumnIndexOrThrow("sticker_pack_icon")))
        assertTrue(cursor.getString(cursor.getColumnIndexOrThrow("image_data_version")).isNotEmpty())
        assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("whatsapp_will_not_cache_stickers")))
        assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("animated_sticker_pack")))
        cursor.close()
    }

    @Test
    fun stickersQueryListsFilesWithEmojis() {
        val (_, provider, repo, authority) = fixture()
        val pack = repo.createPack("Test pack")
        repo.addSticker(pack.id, byteArrayOf(1, 2, 3), fileName = "stk_one.webp")
        repo.addSticker(pack.id, byteArrayOf(4, 5, 6), fileName = "stk_two.webp")

        provider.query(Uri.parse("content://$authority/stickers/${pack.id}"), null, null, null, null).use { cursor ->
            assertArrayEquals(arrayOf("sticker_file_name", "sticker_emoji", "sticker_accessibility_text"), cursor.columnNames)
            assertEquals(2, cursor.count)
            cursor.moveToFirst()
            assertEquals("stk_one.webp", cursor.getString(cursor.getColumnIndexOrThrow("sticker_file_name")))
            assertTrue(cursor.getString(cursor.getColumnIndexOrThrow("sticker_emoji")).isNotEmpty())
        }
        provider.query(Uri.parse("content://$authority/stickers/pack_other"), null, null, null, null).use { cursor ->
            assertEquals(0, cursor.count)
        }
    }

    @Test
    fun imageDataVersionChangesWithStickersAndTray() {
        val (_, provider, repo, authority) = fixture()
        val pack = repo.createPack("Versions")
        repo.addSticker(pack.id, byteArrayOf(1), fileName = "stk_one.webp")
        val uri = Uri.parse("content://$authority/metadata/${pack.id}")
        fun version() = provider.query(uri, null, null, null, null).use { cursor ->
            cursor.moveToFirst()
            cursor.getString(cursor.getColumnIndexOrThrow("image_data_version"))
        }
        val first = version()
        assertEquals(first, version())
        repo.addSticker(pack.id, byteArrayOf(2), fileName = "stk_two.webp")
        val second = version()
        assertNotEquals(first, second)
        repo.setTrayImage(pack.id, byteArrayOf(3))
        val tray = File(File(File(ApplicationProvider.getApplicationContext<Context>().filesDir, "packs"), pack.id), "tray.webp")
        tray.setLastModified(1_000_000L)
        val third = version()
        assertNotEquals(second, third)
        tray.setLastModified(2_000_000L)
        assertNotEquals(third, version())
    }

    @Test
    fun trayIsServedThroughStickersAsset() {
        val (_, provider, repo, authority) = fixture()
        val pack = repo.createPack("Tray")
        repo.addSticker(pack.id, byteArrayOf(1), fileName = "stk_one.webp")
        repo.setTrayImage(pack.id, byteArrayOf(9, 9))
        provider.openFile(Uri.parse("content://$authority/stickers_asset/${pack.id}/tray.webp"), "r").close()
        assertEquals("image/webp", provider.getType(Uri.parse("content://$authority/stickers_asset/${pack.id}/tray.webp")))
        assertEquals(
            "vnd.android.cursor.dir/vnd.$authority.stickers",
            provider.getType(Uri.parse("content://$authority/stickers/${pack.id}")),
        )
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
    fun providerObservesMetadataAndAssetChanges() {
        val (_, provider, repo, authority) = fixture()
        val pack = repo.createPack("Before")
        repo.addSticker(pack.id, byteArrayOf(1, 2, 3), fileName = "stk_one.webp")
        val uri = Uri.parse("content://$authority/metadata/${pack.id}")
        provider.query(uri, null, null, null, null).close()
        repo.renamePack(pack.id, "After!")
        provider.query(uri, null, null, null, null).use { cursor ->
            cursor.moveToFirst()
            assertEquals("After!", cursor.getString(cursor.getColumnIndexOrThrow("sticker_pack_name")))
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

    @Test
    fun manifestRequiresWhatsAppReadPermission() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val info = context.packageManager.resolveContentProvider(
            "${context.packageName}.stickercontentprovider", 0,
        )
        assertNotNull("provider must be registered in the merged manifest", info)
        assertTrue(info!!.exported)
        assertTrue(info.enabled)
        assertEquals("com.whatsapp.sticker.READ", info.readPermission)
        assertFalse("provider must not grant arbitrary URI access", info.grantUriPermissions)
    }

    @Test
    fun singlePackQueriesDoNotLeakOtherPacks() {
        val (_, provider, repo, authority) = fixture()
        val first = repo.createPack("First")
        val second = repo.createPack("Second")
        repo.addSticker(first.id, byteArrayOf(1), "first.webp")
        repo.addSticker(second.id, byteArrayOf(2), "second.webp")
        provider.query(Uri.parse("content://$authority/metadata/${first.id}"), null, null, null, null).use {
            assertEquals(1, it.count)
            it.moveToFirst()
            assertEquals(first.id, it.getString(it.getColumnIndexOrThrow("sticker_pack_identifier")))
        }
        provider.query(Uri.parse("content://$authority/stickers/${first.id}"), null, null, null, null).use {
            assertEquals(1, it.count)
            it.moveToFirst()
            assertEquals("first.webp", it.getString(it.getColumnIndexOrThrow("sticker_file_name")))
        }
        provider.query(Uri.parse("content://$authority/metadata/0"), null, null, null, null).use {
            assertEquals("numeric IDs must not select all remaining packs", 0, it.count)
        }
    }

    @Test
    fun malformedAndUnlistedAssetsAndWriteModesAreRejected() {
        val (_, provider, repo, authority) = fixture()
        val pack = repo.createPack("Boundaries")
        repo.addSticker(pack.id, byteArrayOf(1), "listed.webp")
        File(repo.stickerFile(pack.id, "listed.webp")!!.parentFile, "unlisted.webp").writeBytes(byteArrayOf(2))
        val paths = listOf(
            "stickers_asset/${pack.id}/unlisted.webp",
            "stickers_asset/${pack.id}/pack.json",
            "stickers_asset/${pack.id}/listed.webp/extra",
            "stickers_asset/${pack.id}",
            "sticker_pack_icon/${pack.id}",
            "stickers_asset/${pack.id}/%2E%2E%2Fpack.json",
            "stickers_asset/%2E%2E%2F${pack.id}/listed.webp",
        )
        for (path in paths) {
            try {
                provider.openFile(Uri.parse("content://$authority/$path"), "r").close()
                fail("unexpected access to $path")
            } catch (_: FileNotFoundException) {
                // expected
            }
        }
        for (mode in listOf("w", "rw", "wa", "rwt")) {
            try {
                provider.openFile(Uri.parse("content://$authority/stickers_asset/${pack.id}/listed.webp"), mode).close()
                fail("unexpected writable descriptor for $mode")
            } catch (_: FileNotFoundException) {
                // expected
            }
        }
        for (path in listOf("metadata/${pack.id}/extra", "stickers/${pack.id}/extra", "metadata/%2E%2E%2F${pack.id}")) {
            provider.query(Uri.parse("content://$authority/$path"), null, null, null, null).use {
                assertEquals("malformed query must not expose packs", 0, it.count)
            }
        }
    }

}
