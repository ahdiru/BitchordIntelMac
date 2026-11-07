package com.music.bitchord.desktop

// This App was build by Chris Tambayong - Fumakill4

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.music.bitchord.ui.icons.BitChordIcons

private fun Modifier.playerChromePill(): Modifier {
    val shape = RoundedCornerShape(50)
    return clip(shape).background(Color.White.copy(alpha = .10f))
        .border(.5.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = .35f), Color.White.copy(alpha = .07f))), shape)
}

@Composable
internal fun DesktopPlayerHeader(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    onClose: () -> Unit,
    onMiniPlayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DesktopTitleBarDragArea(modifier.fillMaxWidth().height(46.dp)) {
    Row(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.playerChromePill()) {
            IconButton(onClick = onClose, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Rounded.Close, "Close player", tint = Color.White, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onMiniPlayer, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Rounded.PictureInPictureAlt, "Open MiniPlayer", tint = Color.White, modifier = Modifier.size(17.dp))
            }
        }
        Row(Modifier.playerChromePill().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            DesktopThinSlider(value = volume, onValueChange = onVolumeChange,
                modifier = Modifier.width(100.dp), idleHeight = 3.dp, activeHeight = 6.dp,
                glassThumb = true, accessibilityLabel = "Volume")
            Spacer(Modifier.width(7.dp))
            Icon(Icons.AutoMirrored.Rounded.VolumeUp, null, tint = Color.White.copy(alpha = .85f), modifier = Modifier.size(16.dp))
        }
    }
    }
}

@Composable
internal fun DesktopPlayerPanelButtons(
    lyrics: Boolean,
    queue: Boolean,
    onLyrics: () -> Unit,
    onQueue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reduceMotion by DesktopAppearanceSettings.reduceAnimation.collectAsState()
    val selected = if (queue) 1 else 0
    val position by animateFloatAsState(
        selected.toFloat(),
        animationSpec = if (reduceMotion) tween(0) else spring(dampingRatio = .58f, stiffness = 480f),
        label = "playerPanelSelection",
    )
    Box(modifier.playerChromePill().padding(3.dp)) {
        if (lyrics || queue) {
            val deformation = kotlin.math.abs(position - selected).coerceAtMost(.4f)
            val bounds = glassLensBounds(60f, 15f + 30f * position, 30f + 8f * deformation, 0f)
            Box(Modifier.align(Alignment.CenterStart).offset(x = bounds.left.dp)
                .width(bounds.width.dp).height((30f - 4f * deformation).dp).clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = .80f)))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            val lyricsInteraction = remember { MutableInteractionSource() }
            val queueInteraction = remember { MutableInteractionSource() }
            Box(Modifier.size(30.dp).clickable(interactionSource = lyricsInteraction, indication = null,
                role = Role.Button, onClick = onLyrics), contentAlignment = Alignment.Center) {
                Icon(BitChordIcons.LyricsQuote, "Show lyrics",
                    tint = if (lyrics && kotlin.math.abs(position) < .4f) Color(0xFF303034) else Color.White, modifier = Modifier.size(17.dp))
            }
            Box(Modifier.size(30.dp).clickable(interactionSource = queueInteraction, indication = null,
                role = Role.Button, onClick = onQueue), contentAlignment = Alignment.Center) {
                Icon(BitChordIcons.Queue, "Show queue",
                    tint = if (queue && kotlin.math.abs(position - 1f) < .4f) Color(0xFF303034) else Color.White, modifier = Modifier.size(17.dp))
            }
        }
    }
}
