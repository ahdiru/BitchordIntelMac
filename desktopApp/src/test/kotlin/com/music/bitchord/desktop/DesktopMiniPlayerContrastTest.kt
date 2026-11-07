package com.music.bitchord.desktop

// This App was build by Chris Tambayong - Fumakill4

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertTrue

class DesktopMiniPlayerContrastTest {
    @Test fun secondaryTextRemainsReadableOverTheBrightestCover() {
        fun contrast(veil: Float, alpha: Float): Float {
            val backdrop = Color.Black.copy(alpha = veil).compositeOver(Color.White)
            val foreground = Color.White.copy(alpha = alpha).compositeOver(backdrop)
            return (foreground.luminance() + .05f) / (backdrop.luminance() + .05f)
        }
        assertTrue(contrast(MINI_METADATA_VEIL, MINI_METADATA_ALPHA) >= 4.5f)
        assertTrue(contrast(MINI_FOOTER_VEIL, MINI_TIME_ALPHA) >= 4.5f)
    }
}
