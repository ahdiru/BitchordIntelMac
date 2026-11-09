package com.music.bitchord.desktop

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import java.awt.EventQueue
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean

/** macOS Now Playing and Touch Bar controls backed by MediaPlayer and AppKit. */
internal object DesktopMacMedia {
    private interface NativeApi : Library {
        fun bitchord_macos_media_start(action: ButtonCallback): Int
        fun bitchord_macos_media_install_touch_bar(window: Pointer?): Int
        fun bitchord_macos_media_update(
            title: String,
            artist: String,
            album: String,
            artworkURL: String,
            isPlaying: Int,
            positionMs: Long,
            durationMs: Long,
            canNext: Int,
            canPrevious: Int,
        )
        fun bitchord_macos_media_stop()
    }

    private fun interface ButtonCallback : Callback {
        fun invoke(button: Int)
    }

    internal class Controller(
        val onPlay: () -> Unit,
        val onPause: () -> Unit,
        val onPlayPause: () -> Unit,
        val onNext: () -> Unit,
        val onPrevious: () -> Unit,
    )

    private val started = AtomicBoolean(false)
    private val shutdownHookAdded = AtomicBoolean(false)
    @Volatile private var controller: Controller? = null
    @Volatile private var buttonCallback: ButtonCallback? = null
    @Volatile private var nativeApi: NativeApi? = null

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
        buttonCallback = callback
        if (native.bitchord_macos_media_start(callback) == 0) {
            buttonCallback = null
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

    fun publish(state: DesktopPlaybackState) {
        if (!started.get()) return
        val song = state.song
        runCatching {
            api()?.bitchord_macos_media_update(
                title = song?.title.orEmpty(),
                artist = song?.artist.orEmpty(),
                album = song?.albumName.orEmpty(),
                artworkURL = song?.thumbnailUrl.orEmpty(),
                isPlaying = if (state.isPlaying) 1 else 0,
                positionMs = state.positionMs,
                durationMs = state.durationMs,
                canNext = if (song == null) 0 else 1,
                canPrevious = if (song == null) 0 else 1,
            )
        }.onFailure { DesktopTrackLog.log("macOS Now Playing update failed: ${it.message}") }
    }

    fun stop() {
        if (!started.compareAndSet(true, false)) return
        controller = null
        runCatching { api()?.bitchord_macos_media_stop() }
        buttonCallback = null
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
            }
        }
    }

    private const val LIBRARY = "bitchord_macos_media"
}
