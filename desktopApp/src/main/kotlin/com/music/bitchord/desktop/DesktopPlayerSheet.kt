package com.music.bitchord.desktop

// This App was build by Chris Tambayong - Fumakill4

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/** Desktop player presentation stays in the current window and retains its keyboard/media focus. */
@Composable
internal fun DesktopPlayerSheet(
    visible: Boolean,
    chrome: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.(windowWidth: Dp, windowHeight: Dp) -> Unit,
) {
    val reduceMotion by DesktopAppearanceSettings.reduceAnimation.collectAsState()
    AnimatedVisibility(
        visible = visible,
        enter = if (reduceMotion) fadeIn(tween(100)) else
            fadeIn(tween(180)) + scaleIn(tween(220), initialScale = .985f),
        exit = if (reduceMotion) fadeOut(tween(100)) else
            fadeOut(tween(140)) + scaleOut(tween(180), targetScale = .985f),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val width = maxWidth
            val height = maxHeight
            Box(Modifier.fillMaxSize()) {
                content(width, height)
                chrome()
            }
        }
    }
}
