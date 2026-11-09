# Save Point — BitChord Desktop v2.0.0 Release

Date: 2026-11-09 (Asia/Makassar)

## State before release work
- Repository: `/Users/christambayong/Downloads/Project/BitChord`
- Starting branch: `agent/windows-build31`
- Starting HEAD: `996fad73`
- Release target authorized by user: desktop `v2.0.0`; do not name/package this as build37.
- User-origin remote: `https://github.com/fumakill4/BitChord.git` (`user-origin`). `origin` is the upstream project and is not the publishing destination.
- Current local edits include desktop navigation/search pill, sidebar icons, Translate menu/icon, and macOS media key/Touch Bar bridge. Android/iOS root version remains 1.9.1.
- Do not stage unrelated untracked archives, screenshots, IPA plans, or artifacts.

## Planned validation and release
1. Review/stage only the desktop source and required native Swift bridge files.
2. Run formatting/diff checks and desktop tests/build using version property `2.0.0`.
3. Produce a local ARM64 DMG with a v2.0.0 filename; do not overwrite existing user DMGs.
4. Commit on a topic branch and push to `user-origin` only after validation.
5. Push `v2.0.0` tag to trigger the repository's release workflow; workflow creates a draft release with desktop assets.

## Known limits
- The final Liquid Glass and Translate hover behavior requires visual runtime verification; a passing compilation does not establish pixel/interaction parity with Apple Music.
- GitHub's workflow must complete before the draft assets are available.
