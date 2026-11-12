import AppKit
import Foundation
import MediaPlayer

public typealias BitChordMediaAction = @convention(c) (Int32) -> Void
public typealias BitChordSeekAction = @convention(c) (Int64) -> Void

private enum BitChordMediaState {
    static var action: BitChordMediaAction?
    static var seekAction: BitChordSeekAction?
    static var commandRegistrations: [(MPRemoteCommand, Any)] = []
    static var artworkTask: Task<Void, Never>?
    static var cachedArtworkURL: URL?
    static var cachedArtwork: MPMediaItemArtwork?
    static var currentArtworkURL: URL?
    static var window: NSWindow?
    static var touchBarDelegate: BitChordTouchBarDelegate?
    static var lastLyricText: String = ""
    static var lastRevealedChars: Int = -1
    static var lastInstrumental: Bool = false
}

private final class BitChordTouchBarTarget: NSObject {
    @objc func previous(_ sender: Any?) { BitChordMediaState.action?(4) }
    @objc func playPause(_ sender: Any?) { BitChordMediaState.action?(2) }
    @objc func next(_ sender: Any?) { BitChordMediaState.action?(3) }
    @objc func lyricTapped(_ sender: Any?) { BitChordMediaState.action?(5) }
}

private final class BitChordTouchBarLyricView: NSView {
    private let label = NSTextField(labelWithString: "")
    private var currentText: String = ""
    private var currentRevealedChars: Int = -1
    private var currentInstrumental: Bool = false

    override init(frame frameRect: NSRect) {
        super.init(frame: frameRect)
        translatesAutoresizingMaskIntoConstraints = false
        wantsLayer = true
        layer?.cornerRadius = 6
        layer?.masksToBounds = true
        layer?.backgroundColor = NSColor(white: 1.0, alpha: 0.08).cgColor

        label.isEditable = false
        label.isBezeled = false
        label.drawsBackground = false
        label.usesSingleLineMode = true
        label.maximumNumberOfLines = 1
        label.lineBreakMode = .byTruncatingTail
        label.translatesAutoresizingMaskIntoConstraints = true
        addSubview(label)

        setContentHuggingPriority(.defaultLow, for: .horizontal)
        setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        heightAnchor.constraint(equalToConstant: 30).isActive = true
        let minWidth = widthAnchor.constraint(greaterThanOrEqualToConstant: 160)
        minWidth.priority = .defaultLow
        minWidth.isActive = true
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func update(text: String, revealedChars: Int, instrumental: Bool) {
        currentText = text
        currentRevealedChars = revealedChars
        currentInstrumental = instrumental

        let baseFont = NSFont.systemFont(ofSize: 13, weight: .medium)
        let sungFont = NSFont.systemFont(ofSize: 13, weight: .semibold)
        let fullLength = (text as NSString).length
        let attr = NSMutableAttributedString(
            string: text,
            attributes: [
                .font: baseFont,
                .foregroundColor: instrumental
                    ? NSColor(white: 1.0, alpha: 0.55)
                    : (revealedChars >= 0
                        ? NSColor(white: 1.0, alpha: 0.45)
                        : NSColor(white: 1.0, alpha: 0.92)),
            ]
        )

        if !instrumental && revealedChars > 0 && fullLength > 0 {
            let clamped = min(revealedChars, fullLength)
            let sungRange = NSRange(location: 0, length: clamped)
            attr.addAttributes(
                [
                    .font: sungFont,
                    .foregroundColor: NSColor.white,
                ],
                range: sungRange
            )
        }

        label.attributedStringValue = attr
        needsLayout = true
    }

    override func layout() {
        super.layout()
        let padX: CGFloat = 10
        let availWidth = max(0, bounds.width - padX * 2)
        let attr = label.attributedStringValue
        let fullSize = attr.size()
        let labelHeight = max(16, ceil(fullSize.height))
        let y = floor((bounds.height - labelHeight) * 0.5)

        if fullSize.width <= availWidth {
            label.lineBreakMode = .byTruncatingTail
            let x = padX + floor((availWidth - fullSize.width) * 0.5)
            label.frame = NSRect(x: x, y: y, width: ceil(fullSize.width) + 2, height: labelHeight)
        } else if currentRevealedChars > 0 && !currentInstrumental {
            let fullLength = (currentText as NSString).length
            let clamped = min(currentRevealedChars, fullLength)
            let prefix = attr.attributedSubstring(from: NSRange(location: 0, length: clamped))
            let sungWidth = prefix.size().width
            let maxShift = max(0, fullSize.width - availWidth)
            let shift = min(maxShift, max(0, sungWidth - availWidth * 0.6))
            label.lineBreakMode = .byClipping
            label.frame = NSRect(x: padX - shift, y: y, width: ceil(fullSize.width) + 4, height: labelHeight)
        } else {
            label.lineBreakMode = .byTruncatingTail
            label.frame = NSRect(x: padX, y: y, width: availWidth, height: labelHeight)
        }
    }
}

private final class BitChordTouchBarDelegate: NSObject, NSTouchBarDelegate {
    static let previousID = NSTouchBarItem.Identifier("com.music.bitchord.previous")
    static let playPauseID = NSTouchBarItem.Identifier("com.music.bitchord.play-pause")
    static let nextID = NSTouchBarItem.Identifier("com.music.bitchord.next")
    static let lyricID = NSTouchBarItem.Identifier("com.music.bitchord.lyric")

    let target = BitChordTouchBarTarget()
    var playing = false
    var canNext = true
    var canPrevious = true

    private var previousButton: NSButton?
    private var playPauseButton: NSButton?
    private var nextButton: NSButton?
    private var lyricView: BitChordTouchBarLyricView?

    func makeTouchBar() -> NSTouchBar {
        let bar = NSTouchBar()
        bar.delegate = self
        bar.defaultItemIdentifiers = [Self.previousID, Self.playPauseID, Self.nextID, Self.lyricID]
        return bar
    }

    func updateTransport(isPlaying: Bool, canNext: Bool, canPrevious: Bool) {
        self.playing = isPlaying
        self.canNext = canNext
        self.canPrevious = canPrevious

        if let playPauseButton {
            let symbol = isPlaying ? "pause.fill" : "play.fill"
            let description = isPlaying ? "Pause" : "Play"
            playPauseButton.image = NSImage(systemSymbolName: symbol, accessibilityDescription: description) ?? NSImage()
        }
        previousButton?.isEnabled = canPrevious
        nextButton?.isEnabled = canNext
    }

    func updateLyric(text: String, revealedChars: Int, instrumental: Bool) {
        lyricView?.update(text: text, revealedChars: revealedChars, instrumental: instrumental)
    }

    func touchBar(_ touchBar: NSTouchBar, makeItemForIdentifier identifier: NSTouchBarItem.Identifier) -> NSTouchBarItem? {
        if identifier == Self.lyricID {
            let item = NSCustomTouchBarItem(identifier: identifier)
            let view = BitChordTouchBarLyricView(frame: .zero)
            let click = NSClickGestureRecognizer(target: target, action: #selector(BitChordTouchBarTarget.lyricTapped(_:)))
            view.addGestureRecognizer(click)
            view.update(
                text: BitChordMediaState.lastLyricText,
                revealedChars: BitChordMediaState.lastRevealedChars,
                instrumental: BitChordMediaState.lastInstrumental
            )
            lyricView = view
            item.view = view
            return item
        }

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
        button.setContentHuggingPriority(.required, for: .horizontal)
        button.setContentCompressionResistancePriority(.required, for: .horizontal)
        switch identifier {
        case Self.previousID:
            button.isEnabled = canPrevious
            previousButton = button
        case Self.playPauseID:
            playPauseButton = button
        case Self.nextID:
            button.isEnabled = canNext
            nextButton = button
        default:
            break
        }
        item.view = button
        return item
    }
}

private func onMain(_ action: @escaping () -> Void) {
    if Thread.isMainThread { action() } else { DispatchQueue.main.async(execute: action) }
}

private func unregisterRemoteCommands() {
    for (command, token) in BitChordMediaState.commandRegistrations {
        command.removeTarget(token)
        command.isEnabled = false
    }
    BitChordMediaState.commandRegistrations.removeAll()
}

@_cdecl("bitchord_macos_media_start")
public func bitchordMacMediaStart(
    _ action: @escaping BitChordMediaAction,
    _ seekAction: @escaping BitChordSeekAction
) -> Int32 {
    onMain {
        unregisterRemoteCommands()
        BitChordMediaState.action = action
        BitChordMediaState.seekAction = seekAction
        let center = MPRemoteCommandCenter.shared()
        let commands: [(MPRemoteCommand, Int32)] = [
            (center.playCommand, 0),
            (center.pauseCommand, 1),
            (center.togglePlayPauseCommand, 2),
            (center.nextTrackCommand, 3),
            (center.previousTrackCommand, 4),
        ]
        var registrations: [(MPRemoteCommand, Any)] = commands.map { command, button in
            command.isEnabled = true
            let token = command.addTarget { _ in
                BitChordMediaState.action?(button)
                return .success
            }
            return (command, token)
        }
        center.changePlaybackPositionCommand.isEnabled = true
        let seekToken = center.changePlaybackPositionCommand.addTarget { event in
            guard let positionEvent = event as? MPChangePlaybackPositionCommandEvent else {
                return .commandFailed
            }
            let clampedSeconds = max(0, positionEvent.positionTime)
            let positionMs = Int64((clampedSeconds * 1_000).rounded())
            let nowPlaying = MPNowPlayingInfoCenter.default()
            if var info = nowPlaying.nowPlayingInfo {
                info[MPNowPlayingInfoPropertyElapsedPlaybackTime] = clampedSeconds
                nowPlaying.nowPlayingInfo = info
            }
            BitChordMediaState.seekAction?(positionMs)
            return .success
        }
        registrations.append((center.changePlaybackPositionCommand, seekToken))
        BitChordMediaState.commandRegistrations = registrations
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
    _ playbackRateValue: Float,
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
    let playbackRate = max(0.0, Double(playbackRateValue))
    let canNext = canNextValue != 0
    let canPrevious = canPreviousValue != 0
    let duration = max(0, Double(durationMilliseconds) / 1_000)
    let rawPosition = max(0, Double(positionMilliseconds) / 1_000)
    let position = duration > 0 ? min(rawPosition, duration) : rawPosition

    onMain {
        let center = MPNowPlayingInfoCenter.default()
        let commands = MPRemoteCommandCenter.shared()
        if title.isEmpty {
            BitChordMediaState.artworkTask?.cancel()
            BitChordMediaState.artworkTask = nil
            BitChordMediaState.currentArtworkURL = nil
            BitChordMediaState.cachedArtworkURL = nil
            BitChordMediaState.cachedArtwork = nil
            center.nowPlayingInfo = nil
            center.playbackState = .stopped
            commands.changePlaybackPositionCommand.isEnabled = false
            BitChordMediaState.lastLyricText = ""
            BitChordMediaState.lastRevealedChars = -1
            BitChordMediaState.lastInstrumental = false
            BitChordMediaState.touchBarDelegate?.updateLyric(text: "", revealedChars: -1, instrumental: false)
            BitChordMediaState.window?.touchBar = nil
            return
        }

        if let delegate = BitChordMediaState.touchBarDelegate {
            delegate.updateTransport(isPlaying: isPlaying, canNext: canNext, canPrevious: canPrevious)
            if let window = BitChordMediaState.window, window.touchBar == nil {
                window.touchBar = delegate.makeTouchBar()
            }
        }

        BitChordMediaState.currentArtworkURL = artworkURL
        var info: [String: Any] = [
            MPMediaItemPropertyTitle: title,
            MPMediaItemPropertyArtist: artist,
            MPMediaItemPropertyAlbumTitle: album,
            MPMediaItemPropertyPlaybackDuration: duration,
            MPNowPlayingInfoPropertyElapsedPlaybackTime: position,
            MPNowPlayingInfoPropertyPlaybackRate: playbackRate,
            MPNowPlayingInfoPropertyDefaultPlaybackRate: 1.0,
            MPNowPlayingInfoPropertyMediaType: MPNowPlayingInfoMediaType.audio.rawValue,
        ]
        if let artworkURL, artworkURL == BitChordMediaState.cachedArtworkURL,
           let cachedArtwork = BitChordMediaState.cachedArtwork {
            info[MPMediaItemPropertyArtwork] = cachedArtwork
            center.nowPlayingInfo = info
        } else {
            center.nowPlayingInfo = info
        }
        center.playbackState = isPlaying ? .playing : .paused
        commands.nextTrackCommand.isEnabled = canNext
        commands.previousTrackCommand.isEnabled = canPrevious
        commands.changePlaybackPositionCommand.isEnabled = duration > 0

        guard let artworkURL else {
            BitChordMediaState.artworkTask?.cancel()
            BitChordMediaState.artworkTask = nil
            return
        }
        if artworkURL == BitChordMediaState.cachedArtworkURL,
           BitChordMediaState.cachedArtwork != nil {
            return
        }
        BitChordMediaState.artworkTask?.cancel()
        BitChordMediaState.artworkTask = Task {
            guard let (data, _) = try? await URLSession.shared.data(from: artworkURL),
                  !Task.isCancelled,
                  let image = NSImage(data: data) else { return }
            let artwork = MPMediaItemArtwork(boundsSize: image.size) { _ in image }
            onMain {
                guard BitChordMediaState.currentArtworkURL == artworkURL else { return }
                BitChordMediaState.cachedArtworkURL = artworkURL
                BitChordMediaState.cachedArtwork = artwork
                var current = center.nowPlayingInfo ?? info
                current[MPMediaItemPropertyArtwork] = artwork
                center.nowPlayingInfo = current
            }
        }
    }
}

@_cdecl("bitchord_macos_media_update_lyric")
public func bitchordMacMediaUpdateLyric(
    _ textPointer: UnsafePointer<CChar>?,
    _ revealedCharsValue: Int32,
    _ isInstrumentalValue: Int32
) {
    let text = textPointer.map(String.init(cString:)) ?? ""
    let revealedChars = Int(revealedCharsValue)
    let instrumental = isInstrumentalValue != 0

    onMain {
        BitChordMediaState.lastLyricText = text
        BitChordMediaState.lastRevealedChars = revealedChars
        BitChordMediaState.lastInstrumental = instrumental
        BitChordMediaState.touchBarDelegate?.updateLyric(
            text: text,
            revealedChars: revealedChars,
            instrumental: instrumental
        )
    }
}

@_cdecl("bitchord_macos_media_stop")
public func bitchordMacMediaStop() {
    onMain {
        unregisterRemoteCommands()
        BitChordMediaState.action = nil
        BitChordMediaState.seekAction = nil
        BitChordMediaState.artworkTask?.cancel()
        BitChordMediaState.artworkTask = nil
        BitChordMediaState.currentArtworkURL = nil
        BitChordMediaState.cachedArtworkURL = nil
        BitChordMediaState.cachedArtwork = nil
        BitChordMediaState.lastLyricText = ""
        BitChordMediaState.lastRevealedChars = -1
        BitChordMediaState.lastInstrumental = false
        BitChordMediaState.window?.touchBar = nil
        BitChordMediaState.window = nil
        BitChordMediaState.touchBarDelegate = nil
        let nowPlaying = MPNowPlayingInfoCenter.default()
        nowPlaying.nowPlayingInfo = nil
        nowPlaying.playbackState = .stopped
    }
}
