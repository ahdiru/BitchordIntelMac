package com.music.bitchord.desktop

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopCaptionDragPolicyTest {
    @Test
    fun inactiveMacWindowActivationClickDoesNotStartCaptionDrag() {
        assertFalse(DesktopCaptionDragPolicy.shouldStart(platformIsMac = true, windowWasFocused = false))
    }

    @Test
    fun focusedMacCaptionCanStillStartWindowDrag() {
        assertTrue(DesktopCaptionDragPolicy.shouldStart(platformIsMac = true, windowWasFocused = true))
    }

    @Test
    fun otherDesktopPlatformsKeepTheirNativeDragBehavior() {
        assertTrue(DesktopCaptionDragPolicy.shouldStart(platformIsMac = false, windowWasFocused = false))
    }
}
