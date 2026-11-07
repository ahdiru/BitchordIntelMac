package com.music.bitchord.desktop

import androidx.compose.ui.window.WindowPlacement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopMiniPlayerTest {

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
