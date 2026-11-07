package com.music.bitchord.desktop

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** How the window is sitting on the screen: floating, maximized, or filling it. */
internal object DesktopWindowMode {

    private val _placement = MutableStateFlow(WindowPlacement.Floating)
    private val _fullScreen = MutableStateFlow(false)
    private val _maximized = MutableStateFlow(false)
    private val _miniPlayer = MutableStateFlow(false)
    private val _alwaysOnTop = MutableStateFlow(false)

    private var savedWidth: Dp = 1220.dp
    private var savedHeight: Dp = 780.dp

    /** What the window's own `placement` is driven from. */
    val placement: StateFlow<WindowPlacement> = _placement

    /** Whether the player is filling the screen, for the controls that say so. */
    val fullScreen: StateFlow<Boolean> = _fullScreen

    /** Whether the window is maximized, for the caption button's own glyph. */
    val maximized: StateFlow<Boolean> = _maximized

    /** Whether the Apple Music-style compact MiniPlayer mode is active. */
    val miniPlayer: StateFlow<Boolean> = _miniPlayer

    /** Whether the window is pinned always on top (floats above other windows). */
    val alwaysOnTop: StateFlow<Boolean> = _alwaysOnTop

    fun toggleAlwaysOnTop() {
        _alwaysOnTop.value = !_alwaysOnTop.value
    }

    fun setAlwaysOnTop(on: Boolean) {
        _alwaysOnTop.value = on
    }

    fun setMiniPlayer(on: Boolean, state: WindowState? = null) {
        if (_miniPlayer.value == on) return
        _miniPlayer.value = on
        if (on) {
            _fullScreen.value = false
            _placement.value = WindowPlacement.Floating
            _maximized.value = false
            if (state != null) {
                if (state.size.width >= 500.dp) savedWidth = state.size.width
                if (state.size.height >= 500.dp) savedHeight = state.size.height
                state.size = DpSize(340.dp, 360.dp)
            }
            _alwaysOnTop.value = true
        } else {
            if (state != null) {
                state.size = DpSize(
                    if (savedWidth >= 500.dp) savedWidth else 1220.dp,
                    if (savedHeight >= 500.dp) savedHeight else 780.dp,
                )
            }
            _alwaysOnTop.value = false
        }
    }

    fun toggleMiniPlayer(state: WindowState? = null) {
        setMiniPlayer(!_miniPlayer.value, state)
    }

    /**
     * The placement the player's full-screen button asks for.
     *
     * Maximized on Windows, not Fullscreen: Compose routes Fullscreen through Skiko's layer, which
     * swaps the rendering surface and there leaves the whole screen white.
     */
    private val fillsScreen: WindowPlacement =
        if (DesktopPlatform.drawsOwnWindowFrame) WindowPlacement.Maximized else WindowPlacement.Fullscreen

    fun toggle() = setFullScreen(!_fullScreen.value)

    /**
     * Maximize or restore, which is what the caption button in the corner does and what
     * double-clicking the title bar has always done.
     */
    fun toggleMaximized() {
        if (DesktopPlatform.isWindows && _fullScreen.value && DesktopWindowsFrame.setFullscreen(false)) {
            DesktopWindowBackdrop.apply()
        }
        _fullScreen.value = false
        set(if (_placement.value == WindowPlacement.Maximized) WindowPlacement.Floating else WindowPlacement.Maximized)
    }

    /**
     * Leaves full screen, for the paths that can only mean "get me out" — closing the player, or
     * the window being put away to the tray.
     */
    fun exit() {
        if (_fullScreen.value) setFullScreen(false)
    }

    /** Takes the window's word for how it is sitting, rather than this object's. */
    fun adopt(placement: WindowPlacement) {
        if (placement == _placement.value) return
        if (placement == WindowPlacement.Floating) _fullScreen.value = false
        set(placement)
    }

    private fun setFullScreen(on: Boolean) {
        _fullScreen.value = on
        // On Windows the only thing that covers a taskbar told to stay on top is a native borderless
        // window raised above it; no Compose placement does, and true Fullscreen paints the screen
        // white. With that in place the window's placement is left as it was — it is the Win32 style
        // that changes, not the placement.
        if (DesktopPlatform.isWindows && DesktopWindowsFrame.setFullscreen(on)) {
            // The native window resets DWM's frame margin on the way out; the chosen material goes
            // back over it so the window is exactly as it was before fullscreen.
            if (!on) DesktopWindowBackdrop.apply()
            return
        }
        set(if (on) fillsScreen else WindowPlacement.Floating)
    }

    private fun set(placement: WindowPlacement) {
        _placement.value = placement
        _maximized.value = placement == WindowPlacement.Maximized
    }
}
