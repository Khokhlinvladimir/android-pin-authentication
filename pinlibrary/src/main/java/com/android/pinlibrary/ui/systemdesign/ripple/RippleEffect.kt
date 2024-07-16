package com.android.pinlibrary.ui.systemdesign.ripple

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun RippleView(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isRippleAnimating by remember { mutableStateOf(false) }
    var rippleSize by remember { mutableFloatStateOf(0f) }
    var ripplePosition by remember { mutableStateOf(Offset(0f, 0f)) }

    val rippleAlpha by animateFloatAsState(
        targetValue = if (isRippleAnimating) 0f else 0.3f,
        animationSpec = tween(durationMillis = 400), label = ""
    )

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    isRippleAnimating = true
                    rippleSize = 2f * max(size.width, size.height)
                    ripplePosition = offset
                }
            }
    ) {

        /** Nested content goes here **/
        content()

        if (isRippleAnimating) {
            Box(
                modifier = Modifier
                    .size(rippleSize.dp)
                    .offset {
                        IntOffset(
                            (ripplePosition.x - rippleSize / 2).toInt(),
                            (ripplePosition.y - rippleSize / 2).toInt()
                        )
                    }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = rippleAlpha),
                                Color.Transparent
                            ),
                            radius = (rippleSize / 2),
                            center = Offset((rippleSize / 2), (rippleSize / 2))
                        ),
                        shape = RoundedCornerShape(percent = 50)
                    )
            )
        }
    }
}