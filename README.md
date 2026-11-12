<div align="center">

<img src="Logo.png" alt="BitChord app icon" width="128" height="128">

# BitChord for Intel Mac (`x86_64`)

**An open-source, native YouTube Music & Hi-Res audio client rebuilt for Intel-based MacBook Pro.**<br>
YouTube Music, local library, DJ-style Automix, bit-perfect audio, and live synced Touch Bar lyrics.

<br>

[![License](https://img.shields.io/badge/license-GPL--3.0-E8743B?style=for-the-badge)](LICENSE)
[![Platform](https://img.shields.io/badge/macOS_Intel_x86__64-15.0+-F5263F?style=for-the-badge&logo=apple&logoColor=white)](#build-from-source)
[![Touch Bar](https://img.shields.io/badge/Touch_Bar-Synced_Lyrics-2F81F7?style=for-the-badge)](#whats-new-in-this-intel-mac-rebuild)

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin_Multiplatform-7F52FF?logo=kotlin&logoColor=white)](shared)
[![Compose Multiplatform](https://img.shields.io/badge/Compose_Multiplatform-4285F4?logo=jetpackcompose&logoColor=white)](desktopApp)
[![Swift AppKit](https://img.shields.io/badge/Swift_·_AppKit_·_MediaPlayer-F05138?logo=swift&logoColor=white)](desktopApp/src/main/swift/macos/BitChordMacMedia.swift)

<br>

**[What's New](#whats-new-in-this-intel-mac-rebuild)** · **[Install](#install)** · **[Build from Source](#build-from-source)** · **[Features](#features)** · **[Upstream Repo](https://github.com/fumakill4/BitChord)** · **[Disclaimer](#disclaimer)**

<br>

<img src="Banner.png" alt="BitChord banner" width="100%">

</div>

> [!IMPORTANT]
> This repository is an **Intel Mac (`x86_64`) rebuild** based on [fumakill4/BitChord](https://github.com/fumakill4/BitChord) (`v2.0.0`), adapted for Intel MacBook Pro hardware with native **MacBook Pro Touch Bar synced lyrics**, verified `x86_64` native dependencies, and automatic Gradle JDK 21 toolchain provisioning.

---

## <a id="whats-new-in-this-intel-mac-rebuild"></a>What's New in This Intel Mac Rebuild

### 🎹 MacBook Pro Touch Bar Synced Lyrics
- **Word- & line-synced lyrics on the Touch Bar** — renders active lyric lines directly alongside playback transport controls (`Previous`, `Play/Pause`, `Next`) inside the MacBook Pro Touch Bar via a native AppKit `NSTouchBar` bridge (`com.music.bitchord.lyric`).
- **Word-by-word highlight & auto-panning** — tracks word timing (`revealedChars`), highlights sung text in white against muted upcoming words, and smoothly pans long lines in-place without Touch Bar flicker.
- **Tap to toggle** — tapping the lyric strip on the Touch Bar toggles the in-app lyrics view.
- **Graceful fallback** — displays `track — artist` when no synced lyrics are available, and a subtle instrumental label during instrumental passages.
- **Native macOS media integration** — integrates with `MPNowPlayingInfoCenter` and `MPRemoteCommandCenter` for Control Center, Lock Screen, and hardware media keys.

### 💻 Intel Mac (`x86_64`) Native Compatibility
- **ONNX Runtime `1.20.0` (`osx-x64`)** for Automix beat/audio analysis on Intel Macs (later ONNX Runtime releases dropped `x86_64` macOS binaries).
- **Native `x86_64` Desktop Stack** — bundles `compose.desktop.macos_x64`, FFmpeg `7.1.1-1.5.12` (`macosx-x86_64`), and JavaFX `21.0.10` (`mac`) for BotGuard PoToken generation.
- **Swift Native Bridge** — compiles [`BitChordMacMedia.swift`](desktopApp/src/main/swift/macos/BitChordMacMedia.swift) via `xcrun --sdk macosx swiftc -target x86_64-apple-macosx15.0` into `libbitchord_macos_media.dylib`.
- **Gradle Foojay Toolchain Resolver** — [`settings.gradle.kts`](settings.gradle.kts) includes `org.gradle.toolchains.foojay-resolver-convention` (`0.9.0`) to provision a compatible `x86_64` Java 21 JDK automatically, and skips Android/TV/WearOS targets cleanly when no Android SDK is installed.

---

## <a id="install"></a>Install

Prebuilt macOS disk images (when published under **[GitHub Releases](https://github.com/ahdiru/BitchordIntelMac/releases)**) bundle their own Java 21 runtime, FFmpeg natives, ONNX Runtime, Automix models, and `libbitchord_macos_media.dylib` — no external JDK installation required.

1. Download `BitChord-2.0.0.dmg` from the [Releases](https://github.com/ahdiru/BitchordIntelMac/releases) page (or build it locally using the steps below).
2. Open the `.dmg` and drag **BitChord.app** into your **Applications** folder.
3. **Gatekeeper Notice:** Because local/open-source builds are not notarized with a paid Apple Developer ID, macOS Gatekeeper may warn on first launch (*"BitChord is from an unidentified developer"* or *"damaged"*). Clear the quarantine attribute in Terminal:
   ```sh
   xattr -cr /Applications/BitChord.app
   ```
   Or Control-click `BitChord.app` in Finder and choose **Open**.

---

## <a id="build-from-source"></a>Build from Source (Intel Mac)

### Prerequisites
- macOS (`x86_64` Intel Mac, targeted against macOS 15 SDK)
- **Xcode Command Line Tools** (`xcrun` and `swiftc`) for building `libbitchord_macos_media.dylib`
- **CMake** (optional but required to compile the C++ Automix analyser `libbitchord_analysis.dylib`)
- Java 21 is resolved automatically via Gradle Toolchains (`foojay-resolver-convention` `0.9.0`)

### Commands

```sh
# Clone the repository
git clone https://github.com/ahdiru/BitchordIntelMac.git
cd BitchordIntelMac

# Run desktop tests (including Touch Bar lyrics formatting tests)
./gradlew :desktopApp:test

# Run BitChord directly from Gradle
./gradlew :desktopApp:run

# Build standalone BitChord.app bundle
./gradlew :desktopApp:createDistributable
# Output: desktopApp/build/compose/binaries/main/app/BitChord.app

# Package macOS .dmg installer
./gradlew :desktopApp:packageDmg -Pbitchord.version=2.0.0
# Output: desktopApp/build/compose/binaries/main/dmg/BitChord-2.0.0.dmg
```

> See [DESKTOP.md](DESKTOP.md) for cross-platform desktop notes (Linux and Windows).

---

<div align="center">

<h1><a id="features"></a>Features</h1>

<table>
  <tr>
    <td width="50%" valign="top">

#### Playback
- **Search, browse and play** anything available on YouTube Music.
- **Hi-Res lossless audio** — FLAC/ALAC from a configured module source, with YouTube Music as fallback.
- **Gapless playback with true crossfade**, adjustable 0–12s.
- **Automix [Beta]** — DJ-style transitions with ONNX beat-matching and tempo-stretching.
- **Offline downloads** — save tracks with embedded metadata.
- **Local music library** integration.
- **Native macOS media controls** — `MPNowPlayingInfoCenter` and hardware media keys.
- **Apple-like lyrics animation** — credit to [binimum](https://github.com/binimum/am-lyrics).

#### Connectivity & Accounts
- **Sign in with your Google account** for personalized content and PoToken support.
- **Spotify integration** — connect your account to play your playlists and Liked Songs.
- **Discord Rich Presence** — in-app login, live track/artist/album and progress.
- **Scrobbling** to Last.fm and ListenBrainz.
- **Pluggable sources** — add, edit, test and health-check module sources.

    </td>
    <td width="50%" valign="top">

#### Experience
- **MacBook Pro Touch Bar lyrics** — live word-synced lyrics with auto-panning and tap-to-toggle.
- **Animated album canvas** — motion artwork on the now-playing screen.
- **Word-synced lyrics** — word/syllable-level highlighting from multiple sources.
- **Lyrics providers** — credit to [lrc.red](https://lrc.red), [BiniLyrics](https://github.com/binimum), [BetterLyrics](https://github.com/better-lyrics/better-lyrics), [PaxSenix](https://lyrics.paxsenix.org), [LyricsPlus](https://github.com/ibratabian17/YouLyPlus), [SimpMusic](https://github.com/maxrave-dev/SimpMusic), [Unison](https://unison.boidu.dev), [Megalobiz](https://www.megalobiz.com), [KuGou](https://www.kugou.com), [LRCLIB](https://lrclib.net), [Musixmatch](https://www.musixmatch.com) and [Genius](https://genius.com).
- **Dynamic, artwork-driven theming** — Material palette extracted from album art.
- **Frosted-glass UI** — translucent bars via Haze, Material 3 theming, and macOS vibrancy.

#### Controls & Tweaks
- **Audio quality controls** and **playback speed** (0.5×–2.0×) with **skip silence**.
- **Sleep timer** — fixed presets or "stop after this track".
- **Equalizer & DSP** controls.
- **Bit-perfect output** support for lossless playback.
- **Stats for nerds** — codec, bit depth, sample rate, and more on the now-playing screen.

    </td>
  </tr>
</table>

</div>

---

<div align="center">

<h1><a id="disclaimer"></a>Disclaimer & Legal Notice</h1>

BitChord is an independent, community-driven third-party audio player and client. It is **not** associated with Google LLC, YouTube Music, Deezer, Telegram, or any of their parent companies.

* **No Media Hosting:** BitChord does not host, upload, or store copyrighted music files. It operates strictly as an interface to scan local device storage or stream media directly from public, public-facing, or user-authenticated APIs.
* **Fair Use & API Usage:** This software is created solely for personal research, educational, and fair-use purposes. The user is entirely responsible for ensuring their usage aligns with their local copyright laws and YouTube Terms of Service.
* **No Ad-Blocking Guarantee:** While BitChord focuses on providing a clean listening environment, it does not guarantee permanent bypasses or modifications to commercial third-party platform conditions.
* **Copyleft:** BitChord is free software under the GPLv3. The license does not let anyone forbid others from selling or redistributing copies, but any distribution must come with the Corresponding Source under the same license.

</div>

---

<div align="center">

<h1><a id="license"></a>License</h1>

This project is licensed under the **GNU General Public License v3.0 (GPLv3)**. See the [LICENSE](LICENSE) file for details.

</div>
