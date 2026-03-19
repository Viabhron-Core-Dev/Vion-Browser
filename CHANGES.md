# Vion Browser — Changes from Upstream

This file documents every modification from the upstream Privacy Browser (Soren Stoutner)
and the monocles browser fork, and the reason for each change.

## Fork Base
- Upstream: Privacy Browser by Soren Stoutner (https://www.stoutner.com/privacy-browser)
- License: GPL v3 — maintained throughout all modifications
- All original copyright notices retained. Vion Browser copyright added on top.

---

## Phase 1 — Repo Cleanup (2026-03)
- Updated `.gitignore` to exclude `.gradle/`, `build/`, `local.properties`, `*.iml`,
  `.idea/caches`, `.idea/workspace.xml` and other generated files.
- Added `.woodpecker.yml` CI configuration placeholder.
- No feature changes.

## Phase 2 — Package Rename and Branding (2026-03)
- Renamed package `com.stoutner.privacybrowser` → `com.vion.browser` throughout all
  Java/Kotlin sources and AndroidManifest.xml.
- Updated `applicationId` in `app/build.gradle`.
- Replaced all "Privacy Browser" UI strings and branding with "Vion Browser".
- Removed product flavors (`standard` / `free`); simplified to single build variant.
- Removed ad-related code (`BannerAd.java`, Google Play Services ads dependency,
  `adView` layouts in free flavor).
- Updated app name, about screen, and all string resources.

## Phase 3 — Build Modernisation (2026-03)
- Upgraded `compileSdkVersion` to 36, `minSdkVersion` to 26, `targetSdkVersion` to 36.
- Migrated all `android.support.*` imports to `androidx.*` (AndroidX).
- Upgraded Gradle plugin and wrapper.
- Added dependencies: Room, EncryptedSharedPreferences, OkHttp, Kotlin Coroutines.
- Converted all Java source files to Kotlin.

## Phase 4 — Bottom Toolbar UI (2026-03)
- Replaced navigation drawer entirely with a 5-button bottom toolbar (Back, Forward,
  Home, Tabs, Menu) — Via Browser style, no labels.
- Added floating 2×5 grid menu (multiple pages, swipe between pages, drag-to-rearrange).
- Added gesture support: horizontal swipe for back/forward, pull-to-refresh only at top.
- Long-press actions per toolbar button are user-configurable.
- WebView now occupies full screen between URL bar (top) and bottom toolbar.
- Security layer (WebView settings, JS default-off, cookie handling) untouched.
