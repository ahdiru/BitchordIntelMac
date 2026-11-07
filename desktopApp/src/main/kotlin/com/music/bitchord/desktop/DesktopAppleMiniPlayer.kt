package com.music.bitchord.desktop

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.data.model.Song
import com.music.bitchord.ui.icons.BitChordIcons
import com.music.bitchord.ui.player.ThinSlider

/**
 * Pixel-perfect Apple Music MiniPlayer replica for macOS Desktop.
 *
 * Layout (top to bottom):
 *   1. Album art — full-width, square, no side padding, square corners
 *   2. Metadata row — title + artist (left) | ♡ heart + ••• more (right)
 *   3. Scrubber — ThinSlider + elapsed / −remaining timestamps
 *   4. Controls — shuffle | ⏮ | ▶/⏸ | ⏭ | repeat  (all flat icons, no orb)
 *   5. Volume row — 🔉 ThinSlider 🔊
 *
 * macOS native traffic-light buttons float over the window via NSVisualEffectView;
 * this composable draws no title-bar clearance of its own (Apple Music style).
 */
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A20),
                        Color(0xFF111116),
                    ),
                ),
            ),
    ) {
        // ── 1. Album Art ─────────────────────────────────────────────────────
        // Full-width square, no side padding — identical to Apple Music mini.
        // macOS native traffic-light controls float above this area automatically.
        DesktopTitleBarDragArea(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            ) {
                DesktopArtwork(
                    url = song?.thumbnailUrl,
                    modifier = Modifier.fillMaxSize(),
                    px = 600,
                )
            }
        }

        // ── 2. Metadata Row ───────────────────────────────────────────────────
        // Title + Artist on the left; ♡ and ••• buttons on the right.
        var moreMenuOpen by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Title + Artist column
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = song?.title ?: "Not Playing",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text = song?.artist ?: "—",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color.White.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // ♡ Heart button
            if (onToggleLike != null) {
                MiniIconButton(
                    onClick = onToggleLike,
                    contentDescription = if (isLiked) "Unlike" else "Like",
                ) {
                    Icon(
                        imageVector = if (isLiked) BitChordIcons.HeartFilled else BitChordIcons.Heart,
                        contentDescription = null,
                        tint = if (isLiked) DesktopAccent else Color.White.copy(alpha = 0.65f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            // ••• More button
            Box {
                MiniIconButton(
                    onClick = { if (song != null) moreMenuOpen = true },
                    contentDescription = "More options",
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreHoriz,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.65f),
                        modifier = Modifier.size(18.dp),
                    )
                }
                DropdownMenu(
                    expanded = moreMenuOpen,
                    onDismissRequest = { moreMenuOpen = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Open full player") },
                        onClick = {
                            moreMenuOpen = false
                            onRestoreFullWindow()
                        },
                    )
                }
            }
        }

        // ── 3. Scrubber ───────────────────────────────────────────────────────
        var seekingFraction by remember { mutableStateOf<Float?>(null) }
        val currentProgress = when {
            durationMs <= 0L -> 0f
            seekingFraction != null -> seekingFraction!!
            else -> (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        ) {
            ThinSlider(
                value = currentProgress,
                onValueChange = { seekingFraction = it },
                onValueChangeFinished = {
                    seekingFraction?.let { frac -> onSeek((frac * durationMs).toLong()) }
                    seekingFraction = null
                },
                idleHeight = 3.dp,
                activeHeight = 6.dp,
                activeColor = Color.White.copy(alpha = 0.90f),
                inactiveColor = Color.White.copy(alpha = 0.20f),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val displayPos = seekingFraction?.let { (it * durationMs).toLong() } ?: positionMs
                val remaining = (durationMs - displayPos).coerceAtLeast(0L)
                Text(
                    text = formatMiniTime(displayPos),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.White.copy(alpha = 0.45f),
                )
                Text(
                    text = if (durationMs > 0L) "-${formatMiniTime(remaining)}" else "--:--",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.White.copy(alpha = 0.45f),
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // ── 4. Controls Row ───────────────────────────────────────────────────
        // Identical icon hierarchy to Apple Music:
        //   shuffle  |  ⏮  |  ▶/⏸ (plain icon, no orb)  |  ⏭  |  repeat
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Shuffle
            val shuffleActive = shuffle
            MiniControlButton(
                onClick = onToggleShuffle,
                contentDescription = "Shuffle",
                iconSize = 20.dp,
                active = shuffleActive,
            ) {
                Icon(
                    imageVector = BitChordIcons.Shuffle,
                    contentDescription = null,
                    tint = if (shuffleActive) DesktopAccent else Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.size(20.dp),
                )
            }

            // Skip Previous (double-tap = restart, single = previous)
            SkipPreviousButton(
                positionMs = positionMs,
                onPrevious = onPrevious,
                onRestart = { onSeek(0L) },
            )

            // Play / Pause — flat icon, same visual weight as other controls
            AppleMusicPlayPauseButton(
                isPlaying = isPlaying,
                onClick = onPlayPause,
            )

            // Skip Next
            MiniControlButton(
                onClick = onNext,
                contentDescription = "Next",
                iconSize = 24.dp,
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.90f),
                    modifier = Modifier.size(24.dp),
                )
            }

            // Repeat
            val repeatActive = repeatMode != DesktopRepeatMode.OFF
            MiniControlButton(
                onClick = onToggleRepeat,
                contentDescription = "Repeat: ${repeatMode.name}",
                iconSize = 20.dp,
                active = repeatActive,
            ) {
                Icon(
                    imageVector = BitChordIcons.Repeat,
                    contentDescription = null,
                    tint = if (repeatActive) DesktopAccent else Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // ── 5. Volume Row ─────────────────────────────────────────────────────
        var localVol by remember(volume) { mutableFloatStateOf(volume) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.VolumeDown,
                contentDescription = "Volume down",
                tint = Color.White.copy(alpha = 0.45f),
                modifier = Modifier.size(16.dp),
            )
            ThinSlider(
                value = localVol.coerceIn(0f, 1f),
                onValueChange = {
                    localVol = it
                    onVolumeChange(it)
                },
                idleHeight = 3.dp,
                activeHeight = 6.dp,
                activeColor = Color.White.copy(alpha = 0.80f),
                inactiveColor = Color.White.copy(alpha = 0.18f),
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                contentDescription = "Volume up",
                tint = Color.White.copy(alpha = 0.45f),
                modifier = Modifier.size(16.dp),
            )
        }

        Spacer(Modifier.height(10.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-components
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Apple Music-style flat Play/Pause button.
 * Large icon (28dp), NO circle background — same visual tier as the other controls,
 * but slightly bigger to signal primary action (matches Apple Music exactly).
 * Press gives a spring-bounce scale animation.
 */
@Composable
private fun AppleMusicPlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val isHovered by interaction.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.88f
            isHovered -> 1.08f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "playPauseScale",
    )

    val iconTint by animateColorAsState(
        targetValue = if (isHovered) Color.White else Color.White.copy(alpha = 0.95f),
        label = "playPauseTint",
    )

    Box(
        modifier = modifier
            .size(44.dp)
            .scale(scale)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = iconTint,
            modifier = Modifier.size(30.dp),
        )
    }
}

/**
 * Skip-Previous button with double-tap detection.
 * - Single tap → previous track (like Apple Music)
 * - Double tap within 300ms → restart current track (seek to 0)
 *
 * If positionMs > 3 seconds, single tap also restarts (Apple Music behavior).
 */
@Composable
private fun SkipPreviousButton(
    positionMs: Long,
    onPrevious: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label = "skipPrevScale",
    )

    Box(
        modifier = modifier
            .size(40.dp)
            .scale(scale)
            .pointerInput(positionMs) {
                detectTapGestures(
                    onDoubleTap = { onRestart() },
                    onTap = {
                        // Apple Music: if > 3s played, go back to start; else previous
                        if (positionMs > 3_000L) onRestart() else onPrevious()
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.SkipPrevious,
            contentDescription = "Previous",
            tint = Color.White.copy(alpha = 0.90f),
            modifier = Modifier.size(24.dp),
        )
    }
}

/**
 * Generic mini icon button for metadata row (heart, more).
 * Hit target is 36dp; no background by default.
 */
@Composable
private fun MiniIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(34.dp),
    ) {
        content()
    }
}

/**
 * Generic control button for the playback controls row.
 * Applies a subtle spring press animation; optionally tints active state.
 */
@Composable
private fun MiniControlButton(
    onClick: () -> Unit,
    contentDescription: String,
    iconSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1.0f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label = "ctrlScale",
    )

    Box(
        modifier = modifier
            .size(iconSize + 16.dp)
            .scale(scale)
            .clickable(
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
