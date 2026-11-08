package com.music.bitchord

import androidx.lifecycle.Lifecycle
import com.music.bitchord.ui.isUiForeground
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricsVisibilityTest {
    @Test fun losingFocusDoesNotStopAVisibleLyricPanel() {
        assertTrue(isUiForeground(Lifecycle.State.RESUMED, true))
        assertTrue(isUiForeground(Lifecycle.State.STARTED, true))
        assertFalse(isUiForeground(Lifecycle.State.STARTED, false))
        assertFalse(isUiForeground(Lifecycle.State.CREATED, true))
        assertFalse(isUiForeground(Lifecycle.State.DESTROYED, true))
    }
}
