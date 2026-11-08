package com.music.bitchord.desktop

// This App was build by Chris Tambayong - Fumakill4

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.ui.icons.BitChordIcons
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Paint
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import kotlin.math.abs

/** A single pointer owner prevents the tap and drag recognizers stealing each other's gestures. */
@Composable
internal fun DesktopLiquidNavigation(
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    haze: HazeState?,
    reduceBlur: Boolean,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    val tabs = remember { listOf(
        BitChordIcons.Play to "Play", BitChordIcons.Explore to "Explore",
        BitChordIcons.Library to "Library", BitChordIcons.Search to "Search",
    ) }
    val currentSelected by rememberUpdatedState(selectedIndex.coerceIn(0, tabs.lastIndex))
    val select by rememberUpdatedState(onSelected)
    var dragging by remember { mutableStateOf(false) }
    var pressing by remember { mutableStateOf(false) }
    var dragIndex by remember { mutableFloatStateOf(selectedIndex.toFloat()) }
    var releasePosition by remember { mutableStateOf<Float?>(null) }
    var dragSpeed by remember { mutableFloatStateOf(0f) }
    val settled = remember { Animatable(selectedIndex.toFloat()) }
    LaunchedEffect(currentSelected, dragging, reduceMotion) {
        if (dragging) {
            settled.stop()
        } else {
            releasePosition?.let { settled.snapTo(it) }
            releasePosition = null
            if (reduceMotion) settled.snapTo(currentSelected.toFloat())
            else settled.animateTo(currentSelected.toFloat(), spring(dampingRatio = GLASS_TAB_DAMPING, stiffness = GLASS_TAB_STIFFNESS))
        }
    }
    // The pointer owns the position during drag; only the release is spring-driven.
    val position = (if (dragging) dragIndex else releasePosition ?: settled.value)
        .coerceIn(-.06f, tabs.lastIndex + .06f)
    val lift by animateFloatAsState(
        if (pressing && !reduceMotion) 1f else 0f,
        animationSpec = if (reduceMotion) tween(0) else spring(dampingRatio = .58f, stiffness = 520f),
        label = "glassTabLift",
    )
    val stretch by animateFloatAsState(
        if (reduceMotion) 0f else
            (abs(currentSelected - position) * .10f + if (dragging) dragSpeed * .12f else 0f).coerceAtMost(.20f),
        animationSpec = if (reduceMotion) tween(0) else spring(dampingRatio = .56f, stiffness = 480f),
        label = "glassTabStretch",
    )
    val capsule = RoundedCornerShape(50)
    BoxWithConstraints(modifier.widthIn(max = 440.dp).fillMaxWidth().height(60.dp)) {
        val trackWidth = maxWidth
        val tabWidth = trackWidth / tabs.size
        val density = LocalDensity.current
        val tabPx = with(density) { tabWidth.toPx() }
        val heightPx = with(density) { 60.dp.toPx() }
        Box(Modifier.fillMaxSize()
            .pointerInput(tabPx) {
                if (tabPx <= 0f) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    pressing = true
                    val start = down.position.x
                    val selected = currentSelected.toFloat()
                    val anchor = if (abs(start / tabPx - (selected + .5f)) <= .5f) selected
                        else glassTabPosition(start, tabPx, tabs.size)
                    var moved = false
                    var released = false
                    var lastX = start
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                            val previousX = lastX
                            lastX = pointer.position.x
                            dragSpeed = (abs(lastX - previousX) / tabPx * 4f).coerceIn(0f, 1f)
                            if (!pointer.pressed) { pointer.consume(); released = true; break }
                            if (!moved && abs(lastX - start) > viewConfiguration.touchSlop) moved = true
                            if (moved) {
                                dragIndex = (anchor + (lastX - start) / tabPx).coerceIn(0f, tabs.lastIndex.toFloat())
                                dragging = true
                            }
                            pointer.consume()
                        }
                        if (released) select(if (moved) kotlin.math.round(dragIndex).toInt()
                            else (lastX / tabPx).toInt().coerceIn(0, tabs.lastIndex))
                    } finally {
                        if (dragging) releasePosition = dragIndex
                        dragging = false
                        pressing = false
                        dragSpeed = 0f
                    }
                }
            }) {
            Box(Modifier.align(Alignment.Center).fillMaxWidth().height(48.dp)
                .shadow(12.dp, capsule).clip(capsule)
                .then(if (haze != null && !reduceBlur) Modifier.hazeEffect(haze) {
                    blurRadius = 24.dp
                    noiseFactor = .015f
                    tints = listOf(HazeTint(Color(0xA32A292D)))
                } else Modifier.background(Color(0xFF343438)))
                .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = .10f), Color.Transparent)))
                .border(.5.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = .38f), Color.White.copy(alpha = .08f))), capsule))

            val bounds = glassLensBounds(
                trackWidth = trackWidth.value,
                desiredCenter = (tabWidth * (position + .5f)).value,
                desiredWidth = (tabWidth * (1f + stretch) + 2.dp + 4.dp * lift).value,
                inset = 1f,
            )
            val lensWidth = bounds.width.dp
            val lensLeft = bounds.left.dp
            Box(Modifier.align(Alignment.CenterStart).offset(x = lensLeft).width(lensWidth)
                .height(52.dp + 4.dp * lift - 8.dp * stretch).shadow(7.dp, capsule).clip(capsule)
                .background(if (reduceBlur) Color(0xFF6B6B70) else Color.White.copy(alpha = .14f))
                .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = .22f), Color.White.copy(alpha = .025f), Color.White.copy(alpha = .10f))))
                .border(.75.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = .8f), Color(0x355AE5F0), Color.White.copy(alpha = .15f), Color.White.copy(alpha = .55f))), capsule))

            Row(Modifier.fillMaxSize().selectableGroup()
                .desktopLensRefraction(with(density) { bounds.center.dp.toPx() }, with(density) { lensWidth.toPx() }, heightPx, !reduceBlur),
                verticalAlignment = Alignment.CenterVertically) {
                tabs.forEachIndexed { index, (icon, label) ->
                    val active = index == currentSelected
                    val tint = if (active) Color.White else Color(0xFFD6D6DA)
                    val interaction = remember { MutableInteractionSource() }
                    Column(Modifier.weight(1f).fillMaxHeight().clip(capsule)
                        .selectable(selected = active, interactionSource = interaction, indication = null,
                            role = Role.Tab, onClick = { select(index) }),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
                        Spacer(Modifier.height(2.dp))
                        Text(label, color = tint, fontSize = 11.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium, maxLines = 1)
                    }
                }
            }
        }
    }
}

internal const val GLASS_TAB_DAMPING = .56f
internal const val GLASS_TAB_STIFFNESS = 360f

internal data class GlassLensBounds(val left: Float, val width: Float) {
    val center: Float get() = left + width / 2f
}

/** Bounds the actual animated capsule, rather than just its center/tab index. */
internal fun glassLensBounds(trackWidth: Float, desiredCenter: Float, desiredWidth: Float, inset: Float): GlassLensBounds {
    val track = trackWidth.takeIf { it.isFinite() && it > 0f } ?: 0f
    val padding = inset.takeIf { it.isFinite() }?.coerceIn(0f, track / 2f) ?: 0f
    val available = (track - 2f * padding).coerceAtLeast(0f)
    val width = desiredWidth.takeIf { it.isFinite() }?.coerceIn(0f, available) ?: available
    val center = desiredCenter.takeIf { it.isFinite() } ?: track / 2f
    val left = (center - width / 2f).coerceIn(padding, (track - padding - width).coerceAtLeast(padding))
    return GlassLensBounds(left, width)
}

internal fun glassTabPosition(x: Float, tabWidth: Float, count: Int): Float =
    if (tabWidth <= 0f || count <= 1) 0f else (x / tabWidth - .5f).coerceIn(0f, (count - 1).toFloat())

/** Samples the rendered glyph pixels through a capsule lens instead of scaling entire labels. */
@Composable
private fun Modifier.desktopLensRefraction(centerX: Float, width: Float, height: Float, enabled: Boolean): Modifier {
    val effect = remember { runCatching { RuntimeEffect.makeForShader(GLASS_LENS_SHADER) }.getOrNull() }
    DisposableEffect(effect) { onDispose { effect?.close() } }
    if (!enabled || effect == null) return this
    return drawWithContent {
        RuntimeShaderBuilder(effect).use { builder ->
            builder.uniform("center", centerX, height / 2f)
            builder.uniform("extent", width / 2f, (height - 6.dp.toPx()) / 2f)
            builder.uniform("rimWidth", 12.dp.toPx())
            ImageFilter.makeRuntimeShader(builder, "content", null).use { filter ->
                Paint().use { paint ->
                    paint.imageFilter = filter
                    val canvas = drawContext.canvas.nativeCanvas
                    canvas.saveLayer(null, paint)
                    try { drawContent() } finally { canvas.restore() }
                }
            }
        }
    }
}

internal const val GLASS_LENS_SHADER = """
uniform shader content;
uniform float2 center;
uniform float2 extent;
uniform float rimWidth;
half4 main(float2 p) {
    float radius = extent.y;
    float2 local = p - center;
    float2 spine = float2(clamp(local.x, -max(extent.x-radius, 0.0), max(extent.x-radius, 0.0)), 0.0);
    float2 normal = local - spine;
    float distance = length(normal);
    float depth = radius - distance;
    if (depth <= 0.0) return content.eval(p);
    float rim = 1.0 - smoothstep(0.0, rimWidth, depth);
    float2 direction = normal / max(distance, 0.001);
    float2 samplePoint = p - direction * rim * rim * rimWidth * 0.48;
    return content.eval(samplePoint);
}
"""
