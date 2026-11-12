package com.music.bitchord.desktop

import com.music.bitchord.data.lyrics.LyricLine
import com.music.bitchord.data.model.Song
import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import java.awt.EventQueue
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean

/** macOS Now Playing and Touch Bar controls backed by MediaPlayer and AppKit. */
internal object DesktopMacMedia {
    internal data class TouchBarLyric(
        val text: String,
        /** Character count already sung when word-synced, or `-1` for line-synced / fallback text. */
        val revealedChars: Int = -1,
        val isInstrumental: Boolean = false,
    ) {
        companion object {
            val EMPTY = TouchBarLyric("")
        }
    }

    private interface NativeApi : Library {
        fun bitchord_macos_media_start(action: ButtonCallback, seek: SeekCallback): Int
        fun bitchord_macos_media_install_touch_bar(window: Pointer?): Int
        fun bitchord_macos_media_update(
            title: String,
            artist: String,
            album: String,
            artworkURL: String,
            isPlaying: Int,
            playbackRate: Float,
            positionMs: Long,
            durationMs: Long,
            canNext: Int,
            canPrevious: Int,
        )
        fun bitchord_macos_media_update_lyric(
            text: String,
            revealedChars: Int,
            isInstrumental: Int,
        )
        fun bitchord_macos_media_stop()
    }

    private fun interface ButtonCallback : Callback {
        fun invoke(button: Int)
    }

    private fun interface SeekCallback : Callback {
        fun invoke(positionMs: Long)
    }

    internal class Controller(
        val onPlay: () -> Unit,
        val onPause: () -> Unit,
        val onPlayPause: () -> Unit,
        val onNext: () -> Unit,
        val onPrevious: () -> Unit,
        val onSeek: (Long) -> Unit = {},
        val onToggleLyrics: () -> Unit = {},
    )

    private val started = AtomicBoolean(false)
    private val shutdownHookAdded = AtomicBoolean(false)
    @Volatile private var controller: Controller? = null
    @Volatile private var buttonCallback: ButtonCallback? = null
    @Volatile private var seekCallback: SeekCallback? = null
    @Volatile private var nativeApi: NativeApi? = null
    @Volatile private var lastPublishedLyric: TouchBarLyric? = null

    private fun api(): NativeApi? {
        nativeApi?.let { return it }
        if (!DesktopPlatform.isMac) return null
        return runCatching {
            val path: Path = DesktopAnalysisRuntime.nativeLibraryPath(LIBRARY)
            Native.load(path.toString(), NativeApi::class.java).also { nativeApi = it }
        }.onFailure { DesktopTrackLog.log("macOS media controls unavailable: ${it.message}") }
            .getOrNull()
    }

    fun start(controller: Controller) {
        this.controller = controller
        if (!DesktopPlatform.isMac || !started.compareAndSet(false, true)) return
        val native = api()
        if (native == null) {
            started.set(false)
            return
        }
        val callback = ButtonCallback { button -> dispatchButton(button) }
        val onSeekCallback = SeekCallback { positionMs -> dispatchSeek(positionMs) }
        buttonCallback = callback
        seekCallback = onSeekCallback
        if (native.bitchord_macos_media_start(callback, onSeekCallback) == 0) {
            buttonCallback = null
            seekCallback = null
            started.set(false)
            DesktopTrackLog.log("macOS Now Playing command center could not start")
            return
        }
        DesktopMacFrame.installTouchBar()
        if (shutdownHookAdded.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(Thread(::stop, "bitchord-macos-media-stop"))
        }
    }

    fun installTouchBar(window: Pointer) {
        if (!started.get()) return
        runCatching { api()?.bitchord_macos_media_install_touch_bar(window) }
            .onFailure { DesktopTrackLog.log("macOS Touch Bar unavailable: ${it.message}") }
    }

    internal fun resolvePlaybackRate(
        isPlaying: Boolean,
        awaitingAudio: Boolean,
        playbackSpeed: Float,
    ): Float = if (isPlaying && !awaitingAudio) playbackSpeed.coerceIn(0.25f, 3.0f) else 0.0f

    internal fun clampPositionMs(positionMs: Long, durationMs: Long): Long =
        if (durationMs > 0L) positionMs.coerceIn(0L, durationMs) else positionMs.coerceAtLeast(0L)

    fun publish(state: DesktopPlaybackState, playbackSpeed: Float = 1.0f) {
        if (!started.get()) return
        val song = state.song
        val positionMs = clampPositionMs(state.positionMs, state.durationMs)
        val rate = resolvePlaybackRate(
            isPlaying = state.isPlaying,
            awaitingAudio = state.awaitingAudio,
            playbackSpeed = playbackSpeed,
        )
        runCatching {
            api()?.bitchord_macos_media_update(
                title = song?.title.orEmpty(),
                artist = song?.artist.orEmpty(),
                album = song?.albumName.orEmpty(),
                artworkURL = song?.thumbnailUrl.orEmpty(),
                isPlaying = if (state.isPlaying) 1 else 0,
                playbackRate = rate,
                positionMs = positionMs,
                durationMs = state.durationMs,
                canNext = if (song == null) 0 else 1,
                canPrevious = if (song == null) 0 else 1,
            )
        }.onFailure { DesktopTrackLog.log("macOS Now Playing update failed: ${it.message}") }
    }

    /** Resolves the lyric line (and word-sync progress) to show on the Touch Bar at [positionMs]. */
    internal fun resolveLyric(
        song: Song?,
        lines: List<LyricLine>?,
        positionMs: Long,
        lyricsOffsetMs: Long,
        syncedLyricsEnabled: Boolean,
    ): TouchBarLyric {
        if (song == null) return TouchBarLyric.EMPTY
        val fallback = if (song.artist.isNotBlank()) "${song.title} — ${song.artist}" else song.title
        if (!syncedLyricsEnabled || lines.isNullOrEmpty() || lines.none { it.timeMs > 0L }) {
            return TouchBarLyric(text = fallback)
        }
        val clockMs = (positionMs + lyricsOffsetMs).coerceAtLeast(0L)
        val activeIndex = lines.indexOfLast { it.timeMs <= clockMs }
        if (activeIndex < 0) {
            return TouchBarLyric(text = "♪ $fallback", isInstrumental = true)
        }
        val line = lines[activeIndex]
        if (line.isGap || line.text.isBlank()) {
            return TouchBarLyric(text = "♪ • • •", isInstrumental = true)
        }
        val revealed = if (line.isWordSynced) {
            line.revealedChars(clockMs).toInt().coerceIn(0, line.text.length)
        } else {
            -1
        }
        return TouchBarLyric(
            text = line.text,
            revealedChars = revealed,
            isInstrumental = false,
        )
    }

    fun publishLyric(lyric: TouchBarLyric) {
        if (!started.get()) return
        if (lyric == lastPublishedLyric) return
        lastPublishedLyric = lyric
        runCatching {
            api()?.bitchord_macos_media_update_lyric(
                text = lyric.text,
                revealedChars = lyric.revealedChars,
                isInstrumental = if (lyric.isInstrumental) 1 else 0,
            )
        }.onFailure { DesktopTrackLog.log("macOS Touch Bar lyric update failed: ${it.message}") }
    }

    fun stop() {
        if (!started.compareAndSet(true, false)) return
        controller = null
        lastPublishedLyric = null
        runCatching { api()?.bitchord_macos_media_stop() }
        buttonCallback = null
        seekCallback = null
    }

    private fun dispatchButton(button: Int) {
        EventQueue.invokeLater {
            val target = controller ?: return@invokeLater
            when (button) {
                0 -> target.onPlay()
                1 -> target.onPause()
                2 -> target.onPlayPause()
                3 -> target.onNext()
                4 -> target.onPrevious()
                5 -> target.onToggleLyrics()
            }
        }
    }

    private fun dispatchSeek(positionMs: Long) {
        EventQueue.invokeLater {
            val target = controller ?: return@invokeLater
            target.onSeek(positionMs.coerceAtLeast(0L))
        }
    }

    private const val LIBRARY = "bitchord_macos_media"
}
