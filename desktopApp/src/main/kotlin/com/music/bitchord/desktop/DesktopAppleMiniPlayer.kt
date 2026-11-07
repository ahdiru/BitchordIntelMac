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
import androidx.compose.material.icons.rounded.OpenInFull
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
 * Exact Apple Music MiniPlayer Architecture (matching video reference):
 *   1. Top Bar: Traffic lights clearance (left) | Return to Full Window button | Volume slider (right)
 *   2. Main Player Body:
 *      - Full Artwork with subtle rounded corners & ambient drop shadow
 *      - Metadata row: Title + Artist (left) | ♡ Favorite + ••• More options (right)
 *      - Scrubber: Hairline slider with elapsed & remaining timestamps
 *      - Playback controls: Shuffle | Skip-Back | Play/Pause | Skip-Next | Repeat
 *      - Bottom-Right Utility: Lyrics (💬) and Queue (📑) toggle buttons
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
    isLyricsActive: Boolean = false,
    isQueueActive: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1E26),
                        Color(0xFF121218),
                    ),
                ),
            ),
    ) {
        // ── 1. Top Bar: Traffic Lights clearance + Expand + Volume ───────────
        DesktopTitleBarDragArea(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .padding(horizontal = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Left: Traffic light clearance on macOS + Return/Expand button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (DesktopPlatform.isMac) {
                        Spacer(Modifier.width(72.dp))
                    }

                    // Return to full window button (identical to Apple Music)
                    IconButton(
                        onClick = onRestoreFullWindow,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f)),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.OpenInFull,
                            contentDescription = "Return to full window",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }

                // Right: Volume Slider (Apple Music places volume in the top bar of MiniPlayer)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeDown,
                        contentDescription = "Volume down",
                        tint = Color.White.copy(alpha = 0.50f),
                        modifier = Modifier.size(14.dp),
                    )

                    var localVol by remember(volume) { mutableFloatStateOf(volume) }
                    ThinSlider(
                        value = localVol.coerceIn(0f, 1f),
                        onValueChange = {
                            localVol = it
                            onVolumeChange(it)
                        },
                        idleHeight = 3.dp,
                        activeHeight = 6.dp,
                        activeColor = Color.White.copy(alpha = 0.85f),
                        inactiveColor = Color.White.copy(alpha = 0.20f),
                        modifier = Modifier.width(70.dp),
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = "Volume up",
                        tint = Color.White.copy(alpha = 0.50f),
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }

        // ── 2. Album Artwork ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            val artShape = RoundedCornerShape(10.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .shadow(
                        elevation = 16.dp,
                        shape = artShape,
                        ambientColor = Color.Black.copy(alpha = 0.5f),
                        spotColor = Color.Black.copy(alpha = 0.8f),
                    )
                    .clip(artShape)
                    .border(0.5.dp, Color.White.copy(alpha = 0.20f), artShape),
            ) {
                DesktopArtwork(
                    url = song?.thumbnailUrl,
                    modifier = Modifier.fillMaxSize(),
                    px = 600,
                )
            }
        }

        // ── 3. Metadata Row: Title + Artist (left) | Heart + More (right) ─────
        var moreMenuOpen by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
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

            // Favorite / Heart button
            if (onToggleLike != null) {
                IconButton(
                    onClick = onToggleLike,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = if (isLiked) BitChordIcons.HeartFilled else BitChordIcons.Heart,
                        contentDescription = if (isLiked) "Unlike" else "Like",
                        tint = if (isLiked) DesktopAccent else Color.White.copy(alpha = 0.65f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            // More Options (•••) button
            Box {
                IconButton(
                    onClick = { if (song != null) moreMenuOpen = true },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreHoriz,
                        contentDescription = "More",
                        tint = Color.White.copy(alpha = 0.65f),
                        modifier = Modifier.size(18.dp),
                    )
                }
                DropdownMenu(
                    expanded = moreMenuOpen,
                    onDismissRequest = { moreMenuOpen = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Restore full window") },
                        onClick = {
                            moreMenuOpen = false
                            onRestoreFullWindow()
                        },
                    )
                }
            }
        }

        // ── 4. Scrubber ───────────────────────────────────────────────────────
        var seekingFraction by remember { mutableStateOf<Float?>(null) }
        val currentProgress = when {
            durationMs <= 0L -> 0f
            seekingFraction != null -> seekingFraction!!
            else -> (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
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

        Spacer(Modifier.height(6.dp))

        // ── 5. Controls Row: Shuffle | Prev | Play/Pause | Next | Repeat ──────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 2.dp),
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

            // Skip Previous (restart track if > 3s, otherwise prev)
            SkipPreviousButton(
                positionMs = positionMs,
                onPrevious = onPrevious,
                onRestart = { onSeek(0L) },
            )

            // Play / Pause — flat Apple icon with spring bounce
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

        Spacer(Modifier.height(4.dp))

        // ── 6. Bottom Right Utilities: Lyrics (💬) & Queue (📑) ───────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onOpenLyrics != null) {
                IconButton(
                    onClick = onOpenLyrics,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isLyricsActive) DesktopAccent.copy(alpha = 0.22f) else Color.Transparent),
                ) {
                    Icon(
                        imageVector = BitChordIcons.LyricsQuote,
                        contentDescription = "Lyrics",
                        tint = if (isLyricsActive) DesktopAccent else Color.White.copy(alpha = 0.65f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(Modifier.width(4.dp))

            if (onOpenQueue != null) {
                IconButton(
                    onClick = onOpenQueue,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isQueueActive) DesktopAccent.copy(alpha = 0.22f) else Color.Transparent),
                ) {
                    Icon(
                        imageVector = BitChordIcons.Queue,
                        contentDescription = "Queue",
                        tint = if (isQueueActive) DesktopAccent else Color.White.copy(alpha = 0.65f),
                        modifier = Modifier.size(16.dp),
                    )
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
            tint = Color.White,
            modifier = Modifier.size(30.dp),
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
