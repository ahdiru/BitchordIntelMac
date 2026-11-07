package com.music.bitchord.desktop

// This App was build by Chris Tambayong - Fumakill4

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import org.jetbrains.skia.Surface
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DesktopGlassLensTest {
    @Test fun capsulePositionClampsAtBothEndsAndCentersOnEachTab() {
        assertEquals(0f, glassTabPosition(-50f, 100f, 4))
        assertEquals(0f, glassTabPosition(50f, 100f, 4))
        assertEquals(1f, glassTabPosition(150f, 100f, 4))
        assertEquals(1.5f, glassTabPosition(200f, 100f, 4))
        assertEquals(3f, glassTabPosition(900f, 100f, 4))
        assertEquals(0f, glassTabPosition(100f, 0f, 4))
    }

    @Test fun shaderDisplacesRimPixelsAndPreservesCenterAndOutside() {
        RuntimeEffect.makeForShader(GLASS_LENS_SHADER).use { effect ->
            RuntimeShaderBuilder(effect).use { builder ->
                builder.uniform("center", 150f, 30f)
                builder.uniform("extent", 50f, 27f)
                builder.uniform("rimWidth", 12f)
                ImageFilter.makeRuntimeShader(builder, "content", null).use { filter ->
                    Surface.makeRasterN32Premul(300, 60).use { surface ->
                        Paint().use { paint ->
                            paint.imageFilter = filter
                            surface.canvas.saveLayer(null, paint)
                            paint.imageFilter = null
                            // A red ramp makes the sampled x coordinate observable in the output.
                            for (x in 0 until 300) {
                                paint.color = (0xFF000000.toInt() or (x.coerceAtMost(255) shl 16))
                                surface.canvas.drawRect(Rect.makeXYWH(x.toFloat(), 0f, 1f, 60f), paint)
                            }
                            surface.canvas.restore()
                        }
                        Bitmap().use { bitmap ->
                            bitmap.allocN32Pixels(300, 60)
                            surface.makeImageSnapshot().use { assertTrue(it.readPixels(bitmap)) }
                            fun red(x: Int) = (bitmap.getColor(x, 30) ushr 16) and 255
                            assertEquals(50, red(50), "Outside the capsule must stay unchanged")
                            assertEquals(150, red(150), "The center must stay readable")
                            assertNotEquals(105, red(105), "The rim must actually sample displaced pixels")
                            assertTrue(red(105) > 105, "Left rim samples inward")
                            assertTrue(red(194) < 194, "Right rim samples inward")
                        }
                    }
                }
            }
        }
    }
}
