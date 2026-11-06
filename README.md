<div align="center">

<br/>
<br/>

<img src="Logo.png" alt="BitChord app icon" width="200" />

# BitChord for macOS (Apple Silicon)

### Aesthetic YouTube Music Client • Native Apple Silicon Build (M1/M2/M3/M4)

<br/>

[![macOS Apple Silicon](https://img.shields.io/badge/Platform-macOS%20Apple%20Silicon%20(arm64)-000000?style=for-the-badge&logo=apple&logoColor=white)](https://github.com/fumakill4/BitChord/releases)
[![Version](https://img.shields.io/badge/Release-v1.9.1-blue?style=for-the-badge)](https://github.com/fumakill4/BitChord/releases/tag/v1.9.1)
[![Original Repo](https://img.shields.io/badge/Original%20Repository-kushagrasinghx%2FBitChord-181717?style=for-the-badge&logo=github)](https://github.com/kushagrasinghx/BitChord)
[![License](https://img.shields.io/github/license/kushagrasinghx/BitChord?style=for-the-badge&labelColor=0d1117)](https://github.com/kushagrasinghx/BitChord/blob/main/LICENSE)

<br/>

[**Download .DMG**](#download) · [**Installation Guide**](#installation-on-macos) · [**Features**](#features) · [**Original Repository**](https://github.com/kushagrasinghx/BitChord)

</div>

> [!NOTE]
> **About this Repository:**  
> This repository provides native **macOS Apple Silicon (`arm64`)** builds and packages (`.dmg` installer & standalone `.app`) of **BitChord v1.9.1**, complete with high-resolution retina app icons and bundled Java 21 runtime.  
> **Original project & source by Kushagra Singh:** [https://github.com/kushagrasinghx/BitChord](https://github.com/kushagrasinghx/BitChord).

> [!IMPORTANT]
> BitChord is not affiliated with, endorsed by, or connected to YouTube or Google in any way. Use it at your own discretion.

---

<div align="center">

<h1><a id="download"></a>Download for macOS</h1>

<a href="https://github.com/fumakill4/BitChord/releases/download/v1.9.1/BitChord-1.9.1.dmg" target="_blank" rel="noopener noreferrer">
  <img src="https://cdn.jsdelivr.net/gh/JMcrafter26/badges@main/src/assets/supported/apple/cozy.svg" alt="Download BitChord for macOS Apple Silicon" height="55"/>
</a>

<br/>

**[Download BitChord-1.9.1.dmg (Apple Silicon arm64)](https://github.com/fumakill4/BitChord/releases/download/v1.9.1/BitChord-1.9.1.dmg)**

</div>

### <a id="installation-on-macos"></a>🍎 Installation on macOS

1. Download **`BitChord-1.9.1.dmg`** from the [Releases](https://github.com/fumakill4/BitChord/releases) page.
2. Double-click the `.dmg` file to open it.
3. Drag **BitChord.app** into your **Applications** folder.
4. **Gatekeeper Notice:** Because this is an open-source build without an expensive Apple Developer ID certificate, macOS may show a prompt (*"BitChord is from an unidentified developer"*).  
   To allow it to open smoothly, run this single command in your Terminal:
   ```bash
   xattr -cr /Applications/BitChord.app
   ```
   Or right-click (Control-click) `BitChord.app` in Finder and select **Open**.

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

<h1><a id="download"></a>Download</h1>

<a href="https://github.com/kushagrasinghx/BitChord/releases" target="_blank" rel="noopener noreferrer"><img src="https://cdn.jsdelivr.net/gh/JMcrafter26/badges@main/src/assets/supported/android/cozy.svg" alt="Download for Android" height="55"/></a>
<a href="https://github.com/kushagrasinghx/BitChord/releases" target="_blank" rel="noopener noreferrer"><img src="https://cdn.jsdelivr.net/gh/JMcrafter26/badges@main/src/assets/supported/windows/cozy.svg" alt="Download for Windows" height="55"/></a>

Grab the latest signed APK from the [Releases](https://github.com/kushagrasinghx/BitChord/releases) page. Sideloading requires enabling "Install unknown apps" for whichever app you download it with.

For the Windows and Linux desktop app, see [DESKTOP.md](DESKTOP.md).

</div>

---

<div align="center">

<h1><a id="contributing"></a>Contributing</h1>

We welcome contributions to BitChord! Please review our [Contributing Guide](CONTRIBUTING.md) and [Code of Conduct](CODE_OF_CONDUCT.md) before submitting a Pull Request.

[**Contributing Guide**](CONTRIBUTING.md) · [**Code of Conduct**](CODE_OF_CONDUCT.md) · [**Maintainers**](MAINTAINERS.md) · [**Additional Docs**](ADDITIONAL.md)

### Thanks to all contributors ❤

<a href="https://github.com/kushagrasinghx/BitChord/graphs/contributors">
  <img src="https://raw.githubusercontent.com/kushagrasinghx/BitChord/contributors/contributors.svg" />
</a>

</div>

---

<div align="center">

<h1><a id="support"></a>Support</h1>

BitChord is free and always will be — if it's earned a spot in your rotation, you can chip in here:

<a href="https://ko-fi.com/kushagrasinghx" target="_blank" rel="noopener noreferrer"><img src="https://cdn.jsdelivr.net/gh/JMcrafter26/badges@main/src/assets/donate/kofi-singular-alt/cozy.svg" alt="Support me on Ko-fi" height="55"/></a>
<a href="https://paypal.me/kuxhagrasingh" target="_blank" rel="noopener noreferrer"><img src="https://cdn.jsdelivr.net/gh/JMcrafter26/badges@main/src/assets/donate/paypal-plural/cozy.svg" alt="Support us on PayPal" height="55"/></a>

<br/>
<img src="upi_support.jpg" alt="UPI Support" width="250" />

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
