# Daily Report — BitChord Desktop v2.0.0

Date: 2026-11-09

## Completed
- Set desktop release packaging to use the public version `2.0.0`; Android/iOS version metadata remains unchanged.
- Refined the desktop player Translate control to use the single Google Translate glyph supplied as reference, rather than stacked symbols.
- Included the current desktop navigation/search pill and sidebar icon work, player translation/pronunciation menu wiring, and macOS Now Playing, media key, and Touch Bar bridge.
- Built the macOS ARM64 DMG locally with version property `2.0.0` and copied it to Downloads.

## Verification
- Initial Gradle invocation used Temurin JDK 27 and failed because the bundled Kotlin parser cannot parse that runtime's version string. Re-ran with installed JetBrains Runtime 21.
- Passed: `:shared:jvmTest`, `:desktopUi:jvmTest`, `:desktopApp:test`, `:desktopApp:packageDmg`.
- Verified app bundle `CFBundleShortVersionString` and `CFBundleVersion` are both `2.0.0`.
- `hdiutil verify` passed for the copied DMG.
- DMG: `/Users/christambayong/Downloads/BitChord-2.0.0-macos-arm64.dmg` (338 MB).
- SHA-256: `a1e8170ef5c5ddb0bf110e44451e41b3eb6eea8bdaeb3b4821376db66891c191`.

## Release state
- Intended destination: `fumakill4/BitChord` (`user-origin`).
- Repository workflow creates a draft release from the `v2.0.0` tag and builds platform assets in GitHub Actions.
- Commit `d9b7bf33` and tag `v2.0.0` were pushed to `user-origin`.
- First Actions run exposed an unsupported ARM64 runner mapping for `macos-14`; the `macos-15` ARM64 label resolved that issue. The Intel runner then failed because the default Xcode toolchain was not selected, so the workflow now selects the latest stable Xcode explicitly before running Gradle.

## Caveat
- Build and packaging verification passed. Visual behavior of the Liquid Glass pill and Translate hover control has not been runtime-reviewed in this pass.
