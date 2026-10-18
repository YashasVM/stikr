# stikr

**Photos in, stickers out.** Turn a photo into a sticker on your phone — cut out
the subject, add an outline and a caption, then import the pack straight into
WhatsApp or share a sticker anywhere. No account, no upload, no watermark.

## Features

- **Auto cut-out, on device** — the subject is cut from the background by ML Kit
  subject segmentation; the model is fetched once by Google Play services and
  everything runs locally
- **A real sticker look** — a white outline (adjustable thickness) and an
  optional caption in four colours, top or bottom
- **Packs** — create, rename, and delete packs; add and delete stickers
- **Add to WhatsApp** — export a pack (3–30 stickers) through the standard
  sticker-pack contract; stikr works with WhatsApp and WhatsApp Business
- **Share a sticker** — send any sticker as a PNG through the normal share sheet
- **Works offline** — no account, no sign-in, nothing is uploaded; cut-out needs
  Google Play services, and everything else works without it
- **Light and dark** — follows your phone's theme

## Install

There are no releases yet. To build and install:

```bash
./gradlew :app:assembleDebug
# then, with a phone connected:
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or open the project in Android Studio and press Run.

## Use

1. Open **stikr** and tap **Make a sticker** (or create a pack first)
2. Pick a photo — stikr cuts out the subject automatically
3. Adjust the **outline**, add a **caption** if you like, then tap **Add to pack**
4. Once a pack has 3 or more stickers, tap **Add to WhatsApp** to import it;
   or use a sticker's menu to **Share** it as an image

## Development

- Kotlin + Jetpack Compose (Material 3), minSdk 26, one module
- `render/StickerRenderer.kt` draws the final 512x512 sticker: dilated white
  silhouette for the outline, subject on top, caption last
- `render/StickerCodec.kt` encodes WebP and walks the quality down until the
  file fits WhatsApp's limits (100 KB per sticker, 50 KB per tray icon)
- `editor/SubjectCutout.kt` wraps ML Kit subject segmentation in a suspend
  function that returns null whenever cut-out is unavailable
- `data/PackRepository.kt` stores packs as files + `pack.json` under the app's
  private storage — no database
- `export/StickerPackContentProvider.kt` implements WhatsApp's pack contract
  (metadata + `stickers_asset` + `sticker_pack_icon`)
- Visual review without a device: `./gradlew testDebugUnitTest` renders sample
  stickers to `app/build/sticker-renders/` (Robolectric)
