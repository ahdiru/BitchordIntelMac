package com.music.bitchord.desktop

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.data.model.Song
import com.music.bitchord.ui.icons.BitChordIcons
import com.music.bitchord.ui.player.ThinSlider

/**
 * Apple Music-style Liquid Glass MiniPlayer for macOS and Desktop.
 * Provides a compact, ultra-polished floating widget with tactile playback controls,
 * ambient artwork glow, hairline scrubber, volume control, and native window clearance.
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
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onRestoreFullWindow: () -> Unit,
    onOpenLyrics: (() -> Unit)? = null,
    onOpenQueue: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val alwaysOnTop by DesktopWindowMode.alwaysOnTop.collectAsState()
    val glassShape = RoundedCornerShape(16.dp)

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xD016161E),
                        Color(0xE60E0E14),
                    ),
                ),
            ),
    ) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight
        val isVeryCompact = availableHeight < 400.dp

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // 1. Header Drag Bar with macOS Traffic Light Clearance & Controls
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
                    // Traffic light clearance for macOS (left side)
                    if (DesktopPlatform.isMac) {
                        Spacer(Modifier.width(72.dp))
                    } else {
                        Spacer(Modifier.width(4.dp))
                    }

                    // Window title / label
                    Text(
                        text = "BitChord",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp,
                        ),
                        color = Color.White.copy(alpha = 0.45f),
                        maxLines = 1,
                    )

                    // Right action buttons: Pin (Always On Top) + Expand / Restore
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        // Always on top toggle button
                        IconButton(
                            onClick = { DesktopWindowMode.toggleAlwaysOnTop() },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (alwaysOnTop) DesktopAccent.copy(alpha = 0.22f)
                                    else Color.White.copy(alpha = 0.06f),
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PushPin,
                                contentDescription = if (alwaysOnTop) "Unpin from top" else "Pin on top",
                                tint = if (alwaysOnTop) DesktopAccent else Color.White.copy(alpha = 0.65f),
                                modifier = Modifier.size(15.dp),
                            )
                        }

                        // Restore to Full Window button
                        IconButton(
                            onClick = onRestoreFullWindow,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f)),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.OpenInFull,
                                contentDescription = "Expand Window",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }

            // 2. Main Player Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                // Large Rounded Artwork with Ambient Drop Glow
                val artSize = if (isVeryCompact) {
                    (availableHeight * 0.36f).coerceIn(110.dp, 160.dp)
                } else {
                    (availableWidth * 0.52f).coerceIn(140.dp, 200.dp)
                }

                Box(
                    modifier = Modifier
                        .size(artSize)
                        .drawBehind {
                            // Ambient radial glow behind the album art
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.14f),
                                        Color.Transparent,
                                    ),
                                    center = center,
                                    radius = size.minDimension * 0.72f,
                                ),
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(
                                elevation = 16.dp,
                                shape = glassShape,
                                ambientColor = Color.Black.copy(alpha = 0.6f),
                                spotColor = Color.Black.copy(alpha = 0.8f),
                            )
                            .clip(glassShape)
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.28f),
                                        Color.White.copy(alpha = 0.06f),
                                    ),
                                ),
                                shape = glassShape,
                            ),
                    ) {
                        DesktopArtwork(
                            url = song?.thumbnailUrl,
                            modifier = Modifier.fillMaxSize(),
                            px = 400,
                        )
                    }
                }

                // Song Title & Artist
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = song?.title ?: "Not Playing",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isVeryCompact) 14.sp else 16.sp,
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = song?.artist ?: "Select a song to play",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = if (isVeryCompact) 11.sp else 12.sp,
                        ),
                        color = DesktopSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }

                // Scrubber Slider (Apple hairline capsule)
                var seekingFraction by remember { mutableStateOf<Float?>(null) }
                val currentProgress = when {
                    durationMs <= 0L -> 0f
                    seekingFraction != null -> seekingFraction!!
                    else -> (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                ) {
                    ThinSlider(
                        value = currentProgress,
                        onValueChange = { seekingFraction = it },
                        onValueChangeFinished = {
                            seekingFraction?.let { frac ->
                                onSeek((frac * durationMs).toLong())
                            }
                            seekingFraction = null
                        },
                        idleHeight = 5.dp,
                        activeHeight = 9.dp,
                        activeColor = Color.White.copy(alpha = 0.95f),
                        inactiveColor = Color.White.copy(alpha = 0.22f),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        val displayPos = seekingFraction?.let { (it * durationMs).toLong() } ?: positionMs
                        val remaining = (durationMs - displayPos).coerceAtLeast(0L)
                        Text(
                            text = formatMiniTime(displayPos),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color.White.copy(alpha = 0.5f),
                        )
                        Text(
                            text = if (durationMs > 0L) "-${formatMiniTime(remaining)}" else "--:--",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color.White.copy(alpha = 0.5f),
                        )
                    }
                }

                // Tactile Playback Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Shuffle
                    IconButton(
                        onClick = onToggleShuffle,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = BitChordIcons.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (shuffle) DesktopAccent else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    // Skip Previous
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    // Play / Pause Glass Orb
                    LiquidGlassPlayButton(
                        isPlaying = isPlaying,
                        onClick = onPlayPause,
                    )

                    // Skip Next
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    // Repeat
                    val repeatActive = repeatMode != DesktopRepeatMode.OFF
                    IconButton(
                        onClick = onToggleRepeat,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = BitChordIcons.Repeat,
                            contentDescription = "Repeat: ${repeatMode.name}",
                            tint = if (repeatActive) DesktopAccent else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                // Bottom Volume & Panels Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(
                            width = 0.5.dp,
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(percent = 50),
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeDown,
                        contentDescription = "Volume",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(15.dp),
                    )

                    var localVol by remember(volume) { mutableFloatStateOf(volume) }
                    ThinSlider(
                        value = localVol.coerceIn(0f, 1f),
                        onValueChange = {
                            localVol = it
                            onVolumeChange(it)
                        },
                        idleHeight = 4.dp,
                        activeHeight = 7.dp,
                        activeColor = Color.White.copy(alpha = 0.85f),
                        inactiveColor = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.weight(1f),
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(15.dp),
                    )

                    if (onOpenLyrics != null) {
                        IconButton(
                            onClick = onOpenLyrics,
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                imageVector = BitChordIcons.LyricsQuote,
                                contentDescription = "Lyrics",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    }

                    if (onOpenQueue != null) {
                        IconButton(
                            onClick = onOpenQueue,
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                imageVector = BitChordIcons.Queue,
                                contentDescription = "Queue",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tactile Play/Pause circular button with Liquid Glass specular rim light
 * and spring scale animation on press.
 */
@Composable
private fun LiquidGlassPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val isHovered by interaction.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else if (isHovered) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "playScale",
    )

    val buttonBg by animateColorAsState(
        targetValue = if (isHovered) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.14f),
        label = "playBg",
    )

    Box(
        modifier = modifier
            .size(50.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(buttonBg)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.10f),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(50f, 50f),
                ),
                shape = CircleShape,
            )
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
            modifier = Modifier.size(26.dp),
        )
    }
}

private fun formatMiniTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
