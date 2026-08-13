package com.android.pinlibrary.ui.systemdesign.ripple

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import kotlin.math.max

/** A reusable radial touch effect. New keyboard buttons use Material ripple directly. */
@Composable
fun RippleView(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val progress = remember { Animatable(1f) }
    var ripplePosition by remember { mutableStateOf(Offset.Zero) }
    val scope = rememberCoroutineScope()
    val rippleColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                ripplePosition = offset
                scope.launch {
                    progress.snapTo(0f)
                    progress.animateTo(1f, tween(360))
                }
            }
        }
    ) {
        content()
        if (progress.value < 1f) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val maxRadius = max(
                    max(ripplePosition.x, size.width - ripplePosition.x),
                    max(ripplePosition.y, size.height - ripplePosition.y)
                ) * 1.1f
                drawCircle(
                    color = rippleColor.copy(alpha = 0.22f * (1f - progress.value)),
                    radius = maxRadius * progress.value,
                    center = ripplePosition
                )
            }
        }
    }
}
