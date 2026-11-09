import AppKit
import Foundation
import MediaPlayer

public typealias BitChordMediaAction = @convention(c) (Int32) -> Void

private enum BitChordMediaState {
    static var action: BitChordMediaAction?
    static var commandTokens: [Any] = []
    static var artworkTask: Task<Void, Never>?
    static var window: NSWindow?
    static var touchBarDelegate: BitChordTouchBarDelegate?
}

private final class BitChordTouchBarTarget: NSObject {
    @objc func previous(_ sender: Any?) { BitChordMediaState.action?(4) }
    @objc func playPause(_ sender: Any?) { BitChordMediaState.action?(2) }
    @objc func next(_ sender: Any?) { BitChordMediaState.action?(3) }
}

private final class BitChordTouchBarDelegate: NSObject, NSTouchBarDelegate {
    static let previousID = NSTouchBarItem.Identifier("com.music.bitchord.previous")
    static let playPauseID = NSTouchBarItem.Identifier("com.music.bitchord.play-pause")
    static let nextID = NSTouchBarItem.Identifier("com.music.bitchord.next")

    let target = BitChordTouchBarTarget()
    var playing = false

    func makeTouchBar() -> NSTouchBar {
        let bar = NSTouchBar()
        bar.delegate = self
        bar.defaultItemIdentifiers = [Self.previousID, Self.playPauseID, Self.nextID]
        return bar
    }

    func touchBar(_ touchBar: NSTouchBar, makeItemForIdentifier identifier: NSTouchBarItem.Identifier) -> NSTouchBarItem? {
        let item = NSCustomTouchBarItem(identifier: identifier)
        let symbol: String
        let description: String
        let selector: Selector
        switch identifier {
        case Self.previousID:
            symbol = "backward.end.fill"
            description = "Previous Track"
            selector = #selector(BitChordTouchBarTarget.previous(_:))
        case Self.playPauseID:
            symbol = playing ? "pause.fill" : "play.fill"
            description = playing ? "Pause" : "Play"
            selector = #selector(BitChordTouchBarTarget.playPause(_:))
        case Self.nextID:
            symbol = "forward.end.fill"
            description = "Next Track"
            selector = #selector(BitChordTouchBarTarget.next(_:))
        default:
            return nil
        }
        let image = NSImage(systemSymbolName: symbol, accessibilityDescription: description) ?? NSImage()
        let button = NSButton(image: image, target: target, action: selector)
        button.bezelStyle = .texturedRounded
        item.view = button
        return item
    }
}

private func onMain(_ action: @escaping () -> Void) {
    if Thread.isMainThread { action() } else { DispatchQueue.main.async(execute: action) }
}

@_cdecl("bitchord_macos_media_start")
public func bitchordMacMediaStart(_ action: @escaping BitChordMediaAction) -> Int32 {
    onMain {
        BitChordMediaState.action = action
        let center = MPRemoteCommandCenter.shared()
        let commands: [(MPRemoteCommand, Int32)] = [
            (center.playCommand, 0),
            (center.pauseCommand, 1),
            (center.togglePlayPauseCommand, 2),
            (center.nextTrackCommand, 3),
            (center.previousTrackCommand, 4),
        ]
        BitChordMediaState.commandTokens = commands.map { command, button in
            command.isEnabled = true
            return command.addTarget { _ in
                BitChordMediaState.action?(button)
                return .success
            }
        }
        if let window = BitChordMediaState.window {
            _ = bitchordMacMediaInstallTouchBar(Unmanaged.passUnretained(window).toOpaque())
        }
    }
    return 1
}

@_cdecl("bitchord_macos_media_install_touch_bar")
public func bitchordMacMediaInstallTouchBar(_ windowPointer: UnsafeMutableRawPointer?) -> Int32 {
    guard let windowPointer else { return 0 }
    onMain {
        let window = Unmanaged<NSWindow>.fromOpaque(windowPointer).takeUnretainedValue()
        let delegate = BitChordMediaState.touchBarDelegate ?? BitChordTouchBarDelegate()
        BitChordMediaState.window = window
        BitChordMediaState.touchBarDelegate = delegate
        window.touchBar = delegate.makeTouchBar()
    }
    return 1
}

@_cdecl("bitchord_macos_media_update")
public func bitchordMacMediaUpdate(
    _ titlePointer: UnsafePointer<CChar>?,
    _ artistPointer: UnsafePointer<CChar>?,
    _ albumPointer: UnsafePointer<CChar>?,
    _ artworkURLPointer: UnsafePointer<CChar>?,
    _ isPlayingValue: Int32,
    _ positionMilliseconds: Int64,
    _ durationMilliseconds: Int64,
    _ canNextValue: Int32,
    _ canPreviousValue: Int32
) {
    let title = titlePointer.map(String.init(cString:)) ?? ""
    let artist = artistPointer.map(String.init(cString:)) ?? ""
    let album = albumPointer.map(String.init(cString:)) ?? ""
    let artworkURL = artworkURLPointer.flatMap { URL(string: String(cString: $0)) }
    let isPlaying = isPlayingValue != 0
    let position = max(0, Double(positionMilliseconds) / 1_000)
    let duration = max(0, Double(durationMilliseconds) / 1_000)

    onMain {
        let center = MPNowPlayingInfoCenter.default()
        if title.isEmpty {
            BitChordMediaState.artworkTask?.cancel()
            center.nowPlayingInfo = nil
            center.playbackState = .stopped
            BitChordMediaState.window?.touchBar = nil
            return
        }

        BitChordMediaState.touchBarDelegate?.playing = isPlaying
        if let delegate = BitChordMediaState.touchBarDelegate,
           let window = BitChordMediaState.window {
            window.touchBar = delegate.makeTouchBar()
        }

        let info: [String: Any] = [
            MPMediaItemPropertyTitle: title,
            MPMediaItemPropertyArtist: artist,
            MPMediaItemPropertyAlbumTitle: album,
            MPMediaItemPropertyPlaybackDuration: duration,
            MPNowPlayingInfoPropertyElapsedPlaybackTime: position,
            MPNowPlayingInfoPropertyPlaybackRate: isPlaying ? 1.0 : 0.0,
            MPNowPlayingInfoPropertyMediaType: MPNowPlayingInfoMediaType.audio.rawValue,
        ]
        center.nowPlayingInfo = info
        center.playbackState = isPlaying ? .playing : .paused
        let commands = MPRemoteCommandCenter.shared()
        commands.nextTrackCommand.isEnabled = canNextValue != 0
        commands.previousTrackCommand.isEnabled = canPreviousValue != 0

        BitChordMediaState.artworkTask?.cancel()
        guard let artworkURL else { return }
        BitChordMediaState.artworkTask = Task {
            guard let (data, _) = try? await URLSession.shared.data(from: artworkURL),
                  !Task.isCancelled,
                  let image = NSImage(data: data) else { return }
            let artwork = MPMediaItemArtwork(boundsSize: image.size) { _ in image }
            var updatedInfo = info
            updatedInfo[MPMediaItemPropertyArtwork] = artwork
            center.nowPlayingInfo = updatedInfo
        }
    }
}

@_cdecl("bitchord_macos_media_stop")
public func bitchordMacMediaStop() {
    onMain {
        let center = MPRemoteCommandCenter.shared()
        let commands: [MPRemoteCommand] = [
            center.playCommand,
            center.pauseCommand,
            center.togglePlayPauseCommand,
            center.nextTrackCommand,
            center.previousTrackCommand,
        ]
        for (command, token) in zip(commands, BitChordMediaState.commandTokens) {
            command.removeTarget(token)
            command.isEnabled = false
        }
        BitChordMediaState.commandTokens.removeAll()
        BitChordMediaState.action = nil
        BitChordMediaState.artworkTask?.cancel()
        BitChordMediaState.artworkTask = nil
        BitChordMediaState.window?.touchBar = nil
        BitChordMediaState.window = nil
        BitChordMediaState.touchBarDelegate = nil
        let nowPlaying = MPNowPlayingInfoCenter.default()
        nowPlaying.nowPlayingInfo = nil
        nowPlaying.playbackState = .stopped
    }
}
