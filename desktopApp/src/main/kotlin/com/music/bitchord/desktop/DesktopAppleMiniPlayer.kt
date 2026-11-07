package com.music.bitchord.desktop

// This App was build by Chris Tambayong - Fumakill4

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.focusable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import kotlinx.coroutines.delay
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.data.model.Song
import com.music.bitchord.ui.icons.BitChordIcons
import com.music.bitchord.ui.player.ThinSlider
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.hazeEffect

/** A compact, full-artwork player with a frosted control deck and native window drag area. */
@Composable
internal fun DesktopAppleMiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    shuffle: Boolean,
    repeatMode: DesktopRepeatMode,
    volume: Float,
    isLiked: Boolean = false,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleLike: (() -> Unit)? = null,
    onRestoreFullWindow: () -> Unit,
    onOpenLyrics: (() -> Unit)? = null,
    onOpenQueue: (() -> Unit)? = null,
    isLyricsActive: Boolean = false,
    isQueueActive: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val haze = remember { HazeState() }
    val reduceBlur by DesktopAppearanceSettings.reduceDynamicBlur.collectAsState()
    val reduceMotion by DesktopAppearanceSettings.reduceAnimation.collectAsState()
    val hover = remember { MutableInteractionSource() }
    val hovered by hover.collectIsHoveredAsState()
    val inputMode = LocalInputModeManager.current
    var keyboardControls by remember { mutableStateOf(inputMode.inputMode == InputMode.Keyboard) }
    var interacting by remember { mutableStateOf(false) }
    var chromeVisible by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    var moreMenuOpen by remember { mutableStateOf(false) }
    var seekingFraction by remember(song?.videoId) { mutableStateOf<Float?>(null) }
    LaunchedEffect(hovered, keyboardControls, interacting, moreMenuOpen, song == null) {
        if (hovered || keyboardControls || interacting || moreMenuOpen || song == null) {
            chromeVisible = true
        } else {
            delay(180)
            chromeVisible = false
        }
    }
    Box(
        modifier.fillMaxSize().background(Color(0xFF242429))
            .hoverable(hover)
            .semantics {
                contentDescription = "MiniPlayer"
                customActions = listOf(CustomAccessibilityAction("Show playback controls") {
                    keyboardControls = true
                    true
                })
            }
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    val revealing = !chromeVisible
                    keyboardControls = true
                    revealing && event.key == Key.Tab
                } else false
            }
            .onFocusChanged { if (!it.hasFocus) keyboardControls = false }
            .focusRequester(focus)
            .focusable()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Final)
                        if (event.type == PointerEventType.Enter || event.type == PointerEventType.Move) {
                            keyboardControls = false
                        }
                        interacting = event.changes.any { it.pressed }
                    }
                }
            },
    ) {
        Box(Modifier.fillMaxSize().hazeSource(haze)) {
            DesktopArtwork(url = song?.thumbnailUrl, modifier = Modifier.fillMaxSize(), px = 800)
            if (song == null) {
                Icon(BitChordIcons.MusicNote, null, tint = Color.White.copy(alpha = .28f),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp).size(72.dp))
            }
        }
        // The entire control deck fades away after the pointer leaves, restoring the full sleeve.
        AnimatedVisibility(
            visible = chromeVisible,
            enter = fadeIn(tween(if (reduceMotion) 80 else 140)),
            exit = fadeOut(tween(if (reduceMotion) 80 else 240)),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
        Column(
            Modifier.fillMaxWidth()
                .then(if (reduceBlur) Modifier.background(Color(0xFF303035)) else Modifier.hazeEffect(haze) {
                    blurRadius = 20.dp
                    mask = Brush.verticalGradient(0f to Color.Transparent, .18f to Color.Black, 1f to Color.Black)
                    noiseFactor = .015f
                    tints = listOf(HazeTint(Color(0x30303035)))
                })
                .background(Brush.verticalGradient(
                    0f to Color.Transparent,
                    .12f to Color.Black.copy(alpha = MINI_METADATA_VEIL),
                    1f to Color.Black.copy(alpha = MINI_FOOTER_VEIL),
                ))
                .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(song?.title ?: "Not Playing", color = Color.White,
                        fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(2.dp))
                    Text(song?.artist ?: "—", color = Color.White.copy(alpha = MINI_METADATA_ALPHA),
                        fontSize = 13.sp, lineHeight = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (onToggleLike != null && song != null) {
                    IconButton(onClick = onToggleLike, modifier = Modifier.size(28.dp)) {
                        Icon(if (isLiked) BitChordIcons.HeartFilled else BitChordIcons.Heart,
                            if (isLiked) "Unlike" else "Like", tint = Color.White,
                            modifier = Modifier.size(15.dp))
                    }
                }
                Box {
                    IconButton(onClick = { moreMenuOpen = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Rounded.MoreHoriz, "More", tint = Color.White, modifier = Modifier.size(17.dp))
                    }
                    DropdownMenu(expanded = moreMenuOpen, onDismissRequest = { moreMenuOpen = false }) {
                        DropdownMenuItem(text = { Text("Return to full window") }, onClick = {
                            moreMenuOpen = false; onRestoreFullWindow()
                        })
                        if (song != null && onOpenLyrics != null) DropdownMenuItem(text = { Text("Show lyrics") }, onClick = {
                            moreMenuOpen = false; onOpenLyrics()
                        })
                        if (song != null && onOpenQueue != null) DropdownMenuItem(text = { Text("Show queue") }, onClick = {
                            moreMenuOpen = false; onOpenQueue()
                        })
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            val progress = seekingFraction ?: if (durationMs > 0L) positionMs.toFloat() / durationMs else 0f
            ThinSlider(
                value = progress.coerceIn(0f, 1f),
                onValueChange = { seekingFraction = it },
                onValueChangeFinished = {
                    if (durationMs > 0L) seekingFraction?.let { onSeek((it * durationMs).toLong()) }
                    seekingFraction = null
                },
                idleHeight = 3.dp, activeHeight = 6.dp,
                inactiveColor = Color.White.copy(alpha = .25f),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatMiniTime(seekingFraction?.let { (it * durationMs).toLong() } ?: positionMs),
                    color = Color.White.copy(alpha = MINI_TIME_ALPHA), fontSize = 10.sp, lineHeight = 13.sp)
                Text(if (durationMs > 0L) "-${formatMiniTime((durationMs - positionMs).coerceAtLeast(0L))}" else "--:--",
                    color = Color.White.copy(alpha = MINI_TIME_ALPHA), fontSize = 10.sp, lineHeight = 13.sp)
            }
            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                MiniControlButton(onClick = onToggleShuffle, contentDescription = "Shuffle", iconSize = 18.dp, active = shuffle) {
                    Icon(BitChordIcons.Shuffle, "Shuffle", tint = Color.White.copy(alpha = if (shuffle) 1f else .60f), modifier = Modifier.size(18.dp))
                }
                SkipPreviousButton(positionMs = positionMs, onPrevious = onPrevious, onRestart = { onSeek(0L) })
                AppleMusicPlayPauseButton(isPlaying = isPlaying, onClick = onPlayPause)
                MiniControlButton(onClick = onNext, contentDescription = "Next", iconSize = 30.dp) {
                    Icon(Icons.Rounded.FastForward, "Next", tint = Color.White, modifier = Modifier.size(30.dp))
                }
                MiniControlButton(onClick = onToggleRepeat, contentDescription = "Repeat: ${repeatMode.name}", iconSize = 18.dp,
                    active = repeatMode != DesktopRepeatMode.OFF) {
                    if (repeatMode == DesktopRepeatMode.ONE) Text("1", color = Color.White, fontSize = 16.sp)
                    else Icon(BitChordIcons.Repeat, "Repeat", tint = Color.White.copy(alpha = if (repeatMode != DesktopRepeatMode.OFF) 1f else .60f), modifier = Modifier.size(18.dp))
                }
            }
        }
        }
        AnimatedVisibility(
            visible = chromeVisible,
            enter = fadeIn(tween(if (reduceMotion) 80 else 140)),
            exit = fadeOut(tween(if (reduceMotion) 80 else 240)),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
        DesktopTitleBarDragArea(Modifier.fillMaxWidth().height(34.dp)) {
            Row(Modifier.fillMaxSize().padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Box(Modifier.size(20.dp).clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = .48f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null,
                        role = Role.Button, onClick = onRestoreFullWindow), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.OpenInFull, "Return to full window", tint = Color.White, modifier = Modifier.size(13.dp))
                }
                Row(Modifier.clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = .48f)).padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    if (song != null && onOpenLyrics != null) {
                        IconButton(onClick = onOpenLyrics, modifier = Modifier.size(24.dp)) {
                            Icon(BitChordIcons.LyricsQuote, "Show lyrics", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                    if (song != null && onOpenQueue != null) {
                        IconButton(onClick = onOpenQueue, modifier = Modifier.size(24.dp)) {
                            Icon(BitChordIcons.Queue, "Show queue", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                    ThinSlider(value = volume, onValueChange = onVolumeChange, modifier = Modifier.width(68.dp),
                        idleHeight = 3.dp, activeHeight = 6.dp, glassThumb = true, accessibilityLabel = "Volume")
                    Spacer(Modifier.width(5.dp))
                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AppleMusicPlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reduceMotion by DesktopAppearanceSettings.reduceAnimation.collectAsState()
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val isHovered by interaction.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.88f
            isHovered -> 1.04f
            else -> 1.0f
        },
        animationSpec = if (reduceMotion) tween(0) else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "playPauseScale",
    )

    Box(
        modifier = modifier
            .size(44.dp)
            .scale(scale)
            .clickable(
                role = Role.Button,
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.White,
            modifier = Modifier.size(40.dp),
        )
    }
}

@Composable
private fun SkipPreviousButton(
    positionMs: Long,
    onPrevious: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reduceMotion by DesktopAppearanceSettings.reduceAnimation.collectAsState()
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = if (reduceMotion) tween(0) else spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label = "skipPrevScale",
    )

    Box(
        modifier = modifier
            .size(40.dp)
            .scale(scale)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button) {
                if (positionMs > 3_000L) onRestart() else onPrevious()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.FastRewind,
            contentDescription = "Previous",
            tint = Color.White.copy(alpha = 0.90f),
            modifier = Modifier.size(30.dp),
        )
    }
}

@Composable
private fun MiniControlButton(
    onClick: () -> Unit,
    contentDescription: String,
    iconSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    content: @Composable () -> Unit,
) {
    val reduceMotion by DesktopAppearanceSettings.reduceAnimation.collectAsState()
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1.0f,
        animationSpec = if (reduceMotion) tween(0) else spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label = "ctrlScale",
    )

    Box(
        modifier = modifier
            .size(iconSize + 16.dp)
            .scale(scale)
            .clickable(
                role = Role.Button,
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

private fun formatMiniTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

// These veils keep secondary text readable even over a white album cover.
internal const val MINI_METADATA_VEIL = .60f
internal const val MINI_FOOTER_VEIL = .72f
internal const val MINI_METADATA_ALPHA = .85f
internal const val MINI_TIME_ALPHA = .70f
