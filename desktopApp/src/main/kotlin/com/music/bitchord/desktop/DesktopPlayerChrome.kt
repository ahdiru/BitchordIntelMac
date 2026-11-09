package com.music.bitchord.desktop

// This App was build by Chris Tambayong - Fumakill4

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.hoverable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.GTranslate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
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
    translation: Boolean,
    pronunciation: Boolean,
    onLyrics: () -> Unit,
    onQueue: () -> Unit,
    onToggleTranslation: () -> Unit,
    onTogglePronunciation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reduceMotion by DesktopAppearanceSettings.reduceAnimation.collectAsState()
    val panelInteraction = remember { MutableInteractionSource() }
    val panelHovered by panelInteraction.collectIsHoveredAsState()
    var translateMenuOpen by remember { mutableStateOf(false) }
    val showTranslate = panelHovered || translateMenuOpen || translation || pronunciation
    val translateWidth by animateFloatAsState(
        if (showTranslate) 42f else 0f,
        animationSpec = if (reduceMotion) tween(0) else spring(dampingRatio = .72f, stiffness = 520f),
        label = "translateRevealWidth",
    )
    val translateAlpha by animateFloatAsState(
        if (showTranslate) 1f else 0f,
        animationSpec = if (reduceMotion) tween(0) else tween(140),
        label = "translateRevealAlpha",
    )
    val selected = when {
        queue -> 1
        lyrics -> 0
        else -> -1
    }
    val position by animateFloatAsState(
        selected.coerceAtLeast(0).toFloat(),
        animationSpec = if (reduceMotion) tween(0) else spring(dampingRatio = .42f, stiffness = 430f),
        label = "playerPanelSelection",
    )
    Row(
        modifier = modifier.hoverable(interactionSource = panelInteraction),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (translateWidth > 0.5f) 6.dp else 0.dp),
    ) {
        val translateInteraction = remember { MutableInteractionSource() }
        val translatePressed by translateInteraction.collectIsPressedAsState()
        Box(
            Modifier.width(translateWidth.dp).height(42.dp)
                .graphicsLayer {
                    alpha = translateAlpha
                    scaleX = if (translatePressed) .92f else .94f + .06f * translateAlpha
                    scaleY = scaleX
                }
                .clip(CircleShape)
                .background(Color.White.copy(alpha = .88f))
                .border(.7.dp, Color.White.copy(alpha = .78f), CircleShape)
                .clickable(interactionSource = translateInteraction, indication = null,
                    enabled = showTranslate, role = Role.Button, onClick = { translateMenuOpen = !translateMenuOpen }),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(25.dp)) {
                Icon(Icons.Rounded.GTranslate, "Translate lyrics", tint = Color(0xFF303034), modifier = Modifier.align(Alignment.Center).size(24.dp))
            }
            if (translateMenuOpen) {
                Popup(
                    popupPositionProvider = remember { TranslateOptionsPosition() },
                    onDismissRequest = { translateMenuOpen = false },
                    properties = PopupProperties(focusable = true),
                ) {
                    val menuShape = RoundedCornerShape(14.dp)
                    Column(
                        Modifier.width(246.dp)
                            .shadow(14.dp, menuShape)
                            .clip(menuShape)
                            .background(Color(0xED403E40))
                            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = .12f), Color.Transparent, Color.White.copy(alpha = .035f))))
                            .border(.7.dp, Color.White.copy(alpha = .42f), menuShape)
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        TranslateOptionRow(
                            icon = Icons.Rounded.GTranslate,
                            label = if (translation) "Hide Translation" else "Show Translation",
                            selected = translation,
                        ) {
                            translateMenuOpen = false
                            onToggleTranslation()
                        }
                        TranslateOptionRow(
                            icon = Icons.Rounded.RecordVoiceOver,
                            label = if (pronunciation) "Hide Pronunciation" else "Show Pronunciation",
                            selected = pronunciation,
                        ) {
                            translateMenuOpen = false
                            onTogglePronunciation()
                        }
                    }
                }
            }
        }

        Box(Modifier.playerChromePill().padding(3.dp)) {
            if (selected >= 0) {
                val deformation = kotlin.math.abs(position - selected).coerceAtMost(.4f)
                val bounds = glassLensBounds(80f, 20f + 40f * position, 40f + 8f * deformation, 0f)
                Box(Modifier.align(Alignment.CenterStart).offset(x = bounds.left.dp)
                    .width(bounds.width.dp).height((40f - 4f * deformation).dp).clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = .84f)))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val lyricsInteraction = remember { MutableInteractionSource() }
                val queueInteraction = remember { MutableInteractionSource() }
                Box(Modifier.size(40.dp).clickable(interactionSource = lyricsInteraction, indication = null,
                    role = Role.Button, onClick = onLyrics), contentAlignment = Alignment.Center) {
                    Icon(BitChordIcons.LyricsQuote, "Show lyrics",
                        tint = if (lyrics && kotlin.math.abs(position) < .4f) Color(0xFF303034) else Color.White, modifier = Modifier.size(20.dp))
                }
                Box(Modifier.size(40.dp).clickable(interactionSource = queueInteraction, indication = null,
                    role = Role.Button, onClick = onQueue), contentAlignment = Alignment.Center) {
                    Icon(BitChordIcons.Queue, "Show queue",
                        tint = if (queue && kotlin.math.abs(position - 1f) < .4f) Color(0xFF303034) else Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun TranslateOptionRow(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().height(38.dp).clip(RoundedCornerShape(9.dp))
            .background(if (selected) Color.White.copy(alpha = .16f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Icon(icon, null, tint = Color.White.copy(alpha = if (selected) 1f else .82f), modifier = Modifier.size(19.dp))
        Text(label, color = Color.White.copy(alpha = .94f), fontSize = 13.sp, maxLines = 1)
    }
}

private class TranslateOptionsPosition : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val margin = 10
        val x = anchorBounds.left.coerceIn(margin, (windowSize.width - popupContentSize.width - margin).coerceAtLeast(margin))
        val above = anchorBounds.top - popupContentSize.height - 9
        val y = if (above >= margin) above else (anchorBounds.bottom + 9).coerceAtMost(windowSize.height - popupContentSize.height - margin)
        return IntOffset(x, y.coerceAtLeast(margin))
    }
}
