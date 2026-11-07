package com.music.bitchord.desktop

// This App was build by Chris Tambayong - Fumakill4

import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.math.abs

class DesktopGlassMotionTest {
    @Test fun tabSpringOvershootsThenSettlesWithoutSustainedOscillation() {
        val animation = TargetBasedAnimation(
            animationSpec = spring<Float>(dampingRatio = GLASS_TAB_DAMPING, stiffness = GLASS_TAB_STIFFNESS),
            typeConverter = Float.VectorConverter,
            initialValue = 0f,
            targetValue = 3f,
        )
        val samples = (0L..1200L step 8L).map { animation.getValueFromNanos(it * 1_000_000L) }
        assertTrue(samples.all { it.isFinite() })
        assertTrue(samples.max() > 3.05f, "The selection needs a visible release bounce")
        assertTrue(samples.max() < 3.6f, "The bounce must remain controlled")
        assertTrue(abs(samples.last() - 3f) < .005f, "The capsule must settle on the tab")
    }
    @Test fun animatedLensStaysInsideTrackEvenAtSpringAndStretchExtremes() {
        for (track in listOf(60f, 280f, 320f, 440f)) {
            for ((start, end) in listOf(0f to 3f, 3f to 0f)) {
                val animation = TargetBasedAnimation(
                    animationSpec = spring<Float>(dampingRatio = GLASS_TAB_DAMPING, stiffness = GLASS_TAB_STIFFNESS),
                    typeConverter = Float.VectorConverter, initialValue = start, targetValue = end,
                )
                for (ms in 0L..1200L step 8L) {
                    val index = animation.getValueFromNanos(ms * 1_000_000L)
                    for (stretch in listOf(1f, 1.2f, 1.5f, 8f)) {
                        val bounds = glassLensBounds(track, (index + .5f) * track / 4f, track / 4f * stretch + 6f, 1f)
                        assertTrue(bounds.left >= 1f)
                        assertTrue(bounds.left + bounds.width <= track - 1f + .0001f)
                        assertTrue(bounds.width >= 0f)
                        assertTrue(bounds.center in 1f..(track - 1f))
                    }
                }
            }
        }
    }

    @Test fun degenerateLensConstraintsDoNotProduceInvalidCoordinates() {
        for (width in listOf(0f, -1f, Float.NaN, Float.POSITIVE_INFINITY)) {
            val bounds = glassLensBounds(width, Float.NaN, Float.POSITIVE_INFINITY, 4f)
            assertTrue(bounds.left.isFinite() && bounds.width.isFinite() && bounds.center.isFinite())
            assertTrue(bounds.width >= 0f)
        }
    }

}
