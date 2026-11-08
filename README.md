<div align="center">

<img src="Logo.png" alt="BitChord app icon" width="128" height="128">

# BitChord for Apple

**An open-source, native music client for Mac, iPhone and iPad.**<br>
YouTube Music, your own library, DJ-style Automix and Hi-Res playback.

<br>

[![Release](https://img.shields.io/github/v/release/fumakill4/BitChord?style=for-the-badge&label=release&color=2F81F7)](https://github.com/fumakill4/BitChord/releases/latest)
[![License](https://img.shields.io/badge/license-GPL--3.0-E8743B?style=for-the-badge)](LICENSE)
[![Platforms](https://img.shields.io/badge/macOS_14+_·_iOS_17+-F5263F?style=for-the-badge)](#download)

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin_Multiplatform-7F52FF?logo=kotlin&logoColor=white)](shared)
[![Compose Multiplatform](https://img.shields.io/badge/Compose_Multiplatform-4285F4?logo=jetpackcompose&logoColor=white)](desktopApp)
[![SwiftUI](https://img.shields.io/badge/SwiftUI-F05138?logo=swift&logoColor=white)](#download)
[![Apple Silicon](https://img.shields.io/badge/Apple_Silicon-555555?logo=apple&logoColor=white)](#download)

<br>

**[Download](#download)** · **[Features](#features)** · **[Install](#install)** · **[Original Repo](https://github.com/kushagrasinghx/BitChord)** · **[Disclaimer](#disclaimer)**

<br>

<img src="Banner.png" alt="BitChord on Mac and iOS" width="100%">

</div>

> [!IMPORTANT]
> This BitChord is a dedicated Apple platform (macOS Apple Silicon & iOS)  and Windows,  Fumakill4's own fork of BitChord for Android, rebuilt for macOS, iPhone, iPad, and Windows so the Android team doesn't make or support it.

---

## <a id="download"></a>Download

<div align="center">

### 💻 macOS (Apple Silicon)
<a href="https://github.com/fumakill4/BitChord/releases/download/macos-v1.9.1.E4/BitChord-1.9.1.E4-macos-arm64.dmg">
  <img src="https://cdn.jsdelivr.net/gh/JMcrafter26/badges@main/src/assets/supported/macos/cozy.svg" alt="Download for macOS" height="60">
</a>

<br/>

**[Download BitChord-1.9.1.E4-macos-arm64.dmg (Latest Apple Silicon Release)](https://github.com/fumakill4/BitChord/releases/download/macos-v1.9.1.E4/BitChord-1.9.1.E4-macos-arm64.dmg)**<br>
<sub>macOS Apple Silicon · Build 1.9.1.E4 · [Release notes](https://github.com/fumakill4/BitChord/releases/tag/macos-v1.9.1.E4)</sub>

<br/>

### 📱 iPhone & iPad (iOS 17+)
<a href="https://github.com/fumakill4/BitChord/releases/tag/v1.9.1-ios">
  <img src="https://cdn.jsdelivr.net/gh/JMcrafter26/badges@main/src/assets/supported/ios/cozy.svg" alt="Download for iOS" height="60">
</a>

<br/>

**[Download BitChord-iOS-v1.9.1.ipa (Dedicated iOS Release)](https://github.com/fumakill4/BitChord/releases/download/v1.9.1-ios/BitChord-iOS-v1.9.1.ipa)**  
<sub>View the dedicated **[iOS v1.9.1 Release Page](https://github.com/fumakill4/BitChord/releases/tag/v1.9.1-ios)**</sub>

<br/>

### 🪟 Windows 10 & 11 (64-bit)
<a href="https://github.com/fumakill4/BitChord/releases/tag/v1.9.1-windows">
  <img src="https://cdn.jsdelivr.net/gh/JMcrafter26/badges@main/src/assets/supported/windows/cozy.svg" alt="Download for Windows" height="60">
</a>

<br/>

**[Download BitChord-1.9.1-windows-x64-setup.exe (Windows Installer)](https://github.com/fumakill4/BitChord/releases/download/v1.9.1-windows/BitChord-1.9.1-windows-x64-setup.exe)**  
<sub>Or get the portable version: **[BitChord-1.9.1-windows-x64-portable.zip](https://github.com/fumakill4/BitChord/releases/download/v1.9.1-windows/BitChord-1.9.1-windows-x64-portable.zip)**</sub>

</div>

---

## <a id="install"></a>Install

### 💻 Mac (Apple Silicon)

1. Download **`BitChord-1.9.1.E4-macos-arm64.dmg`** from the [macOS release](https://github.com/fumakill4/BitChord/releases/tag/macos-v1.9.1.E4) page.
2. Open the `.dmg` file and drag **BitChord.app** into your **Applications** folder.
3. **Gatekeeper Notice:** Because this is an open-source build without an Apple Developer ID signature, Gatekeeper may display a prompt on the first launch (*"BitChord is from an unidentified developer"*).  
   You can easily open it by running this command in Terminal:
   ```sh
   xattr -cr /Applications/BitChord.app
   ```
   Or right-click (Control-click) `BitChord.app` in Finder and select **Open**.

### 📱 iPhone & iPad (Sideloading)

1. Download **`BitChord-iOS-v1.9.1.ipa`** from the [iOS Release](https://github.com/fumakill4/BitChord/releases/tag/v1.9.1-ios) page.
2. Sideload it using your preferred sideloading tool:
   - **AltStore / SideStore**: Open the IPA in the app and sign with your Apple ID.
   - **Sideloadly**: Connect your device, drag the IPA into Sideloadly, and click Start.
   - **TrollStore** (if supported on your iOS version): Install directly without revokes.
   - **Scarlet**: Direct on-device installation.
3. If prompted by iOS, go to **Settings → General → VPN & Device Management** and trust your developer certificate.

### 🪟 Windows (10 & 11)

**Option A — Setup Installer (Recommended):**
1. Download **`BitChord-1.9.1-windows-x64-setup.exe`** from the [Windows Release](https://github.com/fumakill4/BitChord/releases/tag/v1.9.1-windows) page.
2. Run the `.exe` installer. It installs for the current user (no administrator permission required) and creates shortcuts in your Start Menu and Desktop.
3. **SmartScreen Notice:** If Windows SmartScreen displays a warning (*"Windows protected your PC"*), click **More info** and select **Run anyway**.

**Option B — Portable (.zip):**
1. Download **`BitChord-1.9.1-windows-x64-portable.zip`**.
2. Extract the archive to any folder or external drive.
3. Launch **`BitChord.exe`** directly without installation.

---

<div align="center">

<img src="Banner.png" alt="BitChord banner" width="100%" />

<h1><a id="features"></a>Features</h1>

<table>
  <tr>
    <td width="50%" valign="top">

#### Playback
- **Search, browse and play** anything available on YouTube Music.
- **Hi-Res lossless audio** — FLAC/ALAC from a configured module source, with YouTube Music as fallback.
- **Gapless playback with true crossfade**, adjustable 0–12s.
- **Automix [Beta]** — DJ-style transitions with beat-matching and tempo-stretching.
- **Offline downloads** — save tracks with embedded metadata.
- **Local music library** integration.
- **Background playback** via a proper foreground media session.
- **Apple-like lyrics animation** — credit to [binimum](https://github.com/binimum/am-lyrics).

#### Connectivity & Accounts
- **Sign in with your Google account** for personalized content.
- **Spotify integration** — connect your account to play your playlists and Liked Songs.
- **Discord Rich Presence** — in-app login, live track/artist/album and progress.
- **Scrobbling** to Last.fm and ListenBrainz.
- **Pluggable sources** — add, edit, test and health-check module sources.

    </td>
    <td width="50%" valign="top">

#### Experience
- **Animated album canvas** — motion artwork on the now-playing screen.
- **Word-synced lyrics** — word/syllable-level highlighting from multiple sources.
- **Lyrics providers** — credit to [lrc.red](https://lrc.red), [BiniLyrics](https://github.com/binimum), [BetterLyrics](https://github.com/better-lyrics/better-lyrics), [PaxSenix](https://lyrics.paxsenix.org), [LyricsPlus](https://github.com/ibratabian17/YouLyPlus), [SimpMusic](https://github.com/maxrave-dev/SimpMusic), [Unison](https://unison.boidu.dev), [Megalobiz](https://www.megalobiz.com), [KuGou](https://www.kugou.com), [LRCLIB](https://lrclib.net), [Musixmatch](https://www.musixmatch.com) and [Genius](https://genius.com).
- **Dynamic, artwork-driven theming** — Material palette extracted from album art.
- **Frosted-glass UI** — Telegram-style translucent bars via Haze, Material 3 theming.

#### Controls & Tweaks
- **Per-network audio quality** — separate quality ceilings for Wi-Fi and mobile data.
- **Playback speed control** (0.5×–2.0×) and **skip silence**.
- **Sleep timer** — fixed presets or "stop after this track".
- **System equalizer** integration.
- **Bit-perfect USB exclusive output** — sends lossless tracks to a USB DAC unchanged, at each track's own rate and bit depth. Thanks to [Mornye](https://github.com/mornyeapp/Mornye) for the reference.
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
