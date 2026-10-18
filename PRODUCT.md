# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

Android users who want stickers made from their own photos — a pet, a friend, a
screenshot — without a computer, an account, or a paid cut-out tool. Single
confirmed audience until testing says otherwise.

## Product Purpose

stikr turns a photo into a sticker on the phone itself: the subject is cut out
on device, given a white sticker outline and an optional caption, saved into
packs, and imported into WhatsApp as a real sticker pack or shared as an image.
Success means a finished sticker in under a minute, with nothing leaving the
phone.

## Positioning

No account, no sign-in, no watermark, no upload: the only dependency is the
phone. Neighbouring tools are web apps that upload photos or apps that stop at
saving a PNG; stikr finishes the job by delivering an importable WhatsApp pack.

## Operating Context

Native Android app, Kotlin + Jetpack Compose (Material 3), Android 8.0 and
newer. Photos come from the system photo picker, so no storage permission is
asked. Packs live as plain files in the app's private storage. Export to
WhatsApp (and WhatsApp Business) goes through the standard sticker-pack
ContentProvider contract, which stikr implements. Distribution is currently a
sideloaded debug build; no releases exist yet.

## Capabilities and Constraints

Confirmed in v0: photo picker; on-device auto cut-out using ML Kit subject
segmentation (model fetched by Google Play services on first use; falls back to
the full photo when unavailable, with the toggle exposed); white outline with
adjustable thickness; optional caption with four colours, top or bottom;
packs with create, rename, delete; add and delete stickers; export a pack to
WhatsApp (3–30 stickers per pack); share a single sticker as a PNG through the
share sheet; light and dark theme.

Constraints: static stickers only, no animation; cut-out requires Google Play
services, everything else works fully offline; no cloud, no accounts, no
analytics, no telemetry; nothing is ever uploaded.

Not shipped yet: releasing a signed APK through GitHub releases, tray-icon
picker, multiple subjects, caption fonts, emoji suggestions per sticker.

## Brand Commitments

Name stikr (lowercase). Voice line: "Photos in, stickers out." Visual anchor:
violet (primary #6C5CE7) with mint and peach accents. Source of truth:
github.com/mrduhlol/stikr.

## Evidence on Hand

- README.md (features, how to build and run)
- app/src/main (the whole app: renderer, cut-out, repository, export)
- app/src/test renders visual samples to app/build/sticker-renders/ for review
  without a device

Absence future work must not fabricate: no releases, no installs, no reviews,
no user counts, no real app-store listing, no photos of real people.

## Product Principles

1. One photo to one finished sticker in under a minute.
2. Nothing leaves the phone — no uploads, no accounts, no tracking.
3. Fail soft: whatever is unavailable (cut-out, WhatsApp) degrades to something
   that still works (the full photo, a share).
4. A pack that says it will import into WhatsApp does import into WhatsApp.
