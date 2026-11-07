package com.music.bitchord.desktop

import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopMiniPlayerTest {

    @Test
    fun enteringMiniPlayerAndResizingItPreservesTheFullWindowSize() {
        DesktopWindowMode.setMiniPlayer(false)
        val window = WindowState(size = DpSize(1100.dp, 700.dp))
        try {
            DesktopWindowMode.setMiniPlayer(true, window)
            assertEquals(DpSize(340.dp, 340.dp), window.size)
            window.size = DpSize(420.dp, 420.dp)
            DesktopWindowMode.setMiniPlayer(false, window)
            assertEquals(DpSize(1100.dp, 700.dp), window.size)
        } finally {
            DesktopWindowMode.setMiniPlayer(false, window)
        }
    }

    @Test
    fun testMiniPlayerToggle() {
        DesktopWindowMode.setMiniPlayer(false)
        assertFalse(DesktopWindowMode.miniPlayer.value)

        DesktopWindowMode.toggleMiniPlayer()
        assertTrue(DesktopWindowMode.miniPlayer.value)
        assertTrue(DesktopWindowMode.alwaysOnTop.value)
        assertEquals(WindowPlacement.Floating, DesktopWindowMode.placement.value)

        DesktopWindowMode.toggleMiniPlayer()
        assertFalse(DesktopWindowMode.miniPlayer.value)
        assertFalse(DesktopWindowMode.alwaysOnTop.value)
    }

    @Test
    fun testAlwaysOnTopToggle() {
        val initial = DesktopWindowMode.alwaysOnTop.value
        DesktopWindowMode.toggleAlwaysOnTop()
        assertEquals(!initial, DesktopWindowMode.alwaysOnTop.value)
        DesktopWindowMode.toggleAlwaysOnTop()
        assertEquals(initial, DesktopWindowMode.alwaysOnTop.value)
    }
}
