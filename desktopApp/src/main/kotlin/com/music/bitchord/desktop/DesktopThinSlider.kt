package com.music.bitchord.desktop

// This App was build by Chris Tambayong - Fumakill4

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.music.bitchord.ui.player.ThinSlider

/** Keep the top player and expanded player on the same pointer, keyboard and accessibility path. */
@Composable
internal fun DesktopThinSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null,
    mixing: Boolean = false,
    transitionWindow: ClosedFloatingPointRange<Float>? = null,
    idleHeight: Dp = 7.dp,
    activeHeight: Dp = 12.dp,
    activeColor: Color = Color.White.copy(alpha = 0.92f),
    inactiveColor: Color = Color.White.copy(alpha = 0.26f),
    markerColor: Color = Color.White.copy(alpha = 0.5f),
    glassThumb: Boolean = false,
    accessibilityLabel: String = "Playback position",
) = ThinSlider(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    onValueChangeFinished = onValueChangeFinished,
    mixing = mixing,
    transitionWindow = transitionWindow,
    idleHeight = idleHeight,
    activeHeight = activeHeight,
    activeColor = activeColor,
    inactiveColor = inactiveColor,
    markerColor = markerColor,
    glassThumb = glassThumb,
    accessibilityLabel = accessibilityLabel,
)
