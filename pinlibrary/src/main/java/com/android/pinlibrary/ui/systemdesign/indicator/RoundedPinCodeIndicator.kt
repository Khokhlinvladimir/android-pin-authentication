package com.android.pinlibrary.ui.systemdesign.indicator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.android.pinlibrary.ui.motion.PinAuthMotionSpec

enum class PinIndicatorFeedback {
    NORMAL,
    ERROR,
    LOCKED,
    SUCCESS
}

@Composable
fun RoundedBoxesRow(startQuantity: Int, quantity: Int) {
    RoundedBoxesRow(
        startQuantity = startQuantity,
        quantity = quantity,
        feedback = PinIndicatorFeedback.NORMAL,
        feedbackToken = quantity,
        isProcessing = false,
        motionSpec = PinAuthMotionSpec.Premium
    )
}

@Composable
fun RoundedBoxesRow(
    startQuantity: Int,
    quantity: Int,
    feedback: PinIndicatorFeedback,
    feedbackToken: Any?,
    isProcessing: Boolean,
    motionSpec: PinAuthMotionSpec,
    modifier: Modifier = Modifier
) {
    val shake = remember { Animatable(0f) }
    LaunchedEffect(feedback, feedbackToken, motionSpec.enabled) {
        if (motionSpec.enabled && feedback == PinIndicatorFeedback.ERROR) {
            shake.snapTo(0f)
            shake.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = motionSpec.feedbackDurationMillis
                    0f at 0
                    -13f at 55
                    11f at 105
                    -9f at 155
                    7f at 205
                    -4f at 255
                    0f at motionSpec.feedbackDurationMillis
                }
            )
        }
    }

    Box(
        modifier = modifier
            .height(72.dp)
            .widthIn(min = 96.dp)
            .graphicsLayer { translationX = shake.value.dp.toPx() },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = feedback == PinIndicatorFeedback.SUCCESS,
            transitionSpec = {
                fadeIn(tween(motionSpec.feedbackDurationMillis / 2)) togetherWith
                    fadeOut(tween(motionSpec.feedbackDurationMillis / 3))
            },
            label = "pin-indicator-result"
        ) { isSuccess ->
            if (isSuccess) {
                SuccessMark(motionSpec)
            } else {
                PinDots(
                    count = startQuantity,
                    filledCount = quantity,
                    feedback = feedback,
                    isProcessing = isProcessing,
                    motionSpec = motionSpec
                )
            }
        }
    }
}

@Composable
private fun PinDots(
    count: Int,
    filledCount: Int,
    feedback: PinIndicatorFeedback,
    isProcessing: Boolean,
    motionSpec: PinAuthMotionSpec
) {
    val pulse = if (motionSpec.enabled && isProcessing) {
        val transition = rememberInfiniteTransition(label = "pin-processing")
        val value by transition.animateFloat(
            initialValue = 0.55f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(motionSpec.processingPulseMillis),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pin-processing-alpha"
        )
        value
    } else {
        1f
    }
    val primary = MaterialTheme.colorScheme.primary
    val error = MaterialTheme.colorScheme.error
    val outline = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f)

    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            PinDot(
                isFilled = index < filledCount,
                color = if (feedback == PinIndicatorFeedback.ERROR ||
                    feedback == PinIndicatorFeedback.LOCKED
                ) error else primary,
                outline = outline,
                pulse = pulse,
                motionSpec = motionSpec,
                modifier = Modifier.padding(horizontal = 5.dp)
            )
        }
    }
}

@Composable
private fun PinDot(
    isFilled: Boolean,
    color: Color,
    outline: Color,
    pulse: Float,
    motionSpec: PinAuthMotionSpec,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isFilled) 1f else 0.78f,
        animationSpec = if (motionSpec.enabled) {
            spring(dampingRatio = 0.48f, stiffness = 560f)
        } else {
            tween(0)
        },
        label = "pin-dot-scale"
    )
    val fillProgress by animateFloatAsState(
        targetValue = if (isFilled) 1f else 0f,
        animationSpec = tween(if (motionSpec.enabled) 150 else 0),
        label = "pin-dot-fill"
    )

    Canvas(
        modifier = modifier
            .size(26.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = pulse
            }
    ) {
        val radius = size.minDimension / 2f - 2.dp.toPx()
        drawCircle(
            color = if (fillProgress > 0f) color.copy(alpha = fillProgress) else Color.Transparent,
            radius = radius * (0.78f + 0.22f * fillProgress)
        )
        drawCircle(
            color = if (fillProgress > 0f) color else outline,
            radius = radius,
            style = Stroke(width = 1.6.dp.toPx())
        )
    }
}

@Composable
private fun SuccessMark(motionSpec: PinAuthMotionSpec) {
    val progress = remember { Animatable(if (motionSpec.enabled) 0f else 1f) }
    val successColor = MaterialTheme.colorScheme.primary
    LaunchedEffect(motionSpec.enabled) {
        if (motionSpec.enabled) {
            progress.snapTo(0f)
            progress.animateTo(
                1f,
                animationSpec = tween(motionSpec.feedbackDurationMillis)
            )
        }
    }

    Canvas(
        modifier = Modifier
            .size(58.dp)
            .graphicsLayer {
                scaleX = 0.72f + 0.28f * progress.value
                scaleY = 0.72f + 0.28f * progress.value
                alpha = progress.value.coerceAtLeast(0.15f)
            }
    ) {
        val stroke = 4.dp.toPx()
        drawCircle(
            color = successColor.copy(alpha = 0.14f + 0.16f * progress.value),
            radius = size.minDimension / 2f
        )
        drawCircle(
            color = successColor,
            radius = size.minDimension / 2f - stroke / 2f,
            style = Stroke(width = stroke)
        )
        val first = (progress.value * 2f).coerceIn(0f, 1f)
        val second = ((progress.value - 0.5f) * 2f).coerceIn(0f, 1f)
        val a = Offset(size.width * 0.27f, size.height * 0.52f)
        val b = Offset(size.width * 0.44f, size.height * 0.68f)
        val c = Offset(size.width * 0.75f, size.height * 0.34f)
        drawLine(
            color = successColor,
            start = a,
            end = Offset(
                a.x + (b.x - a.x) * first,
                a.y + (b.y - a.y) * first
            ),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        if (second > 0f) {
            drawLine(
                color = successColor,
                start = b,
                end = Offset(
                    b.x + (c.x - b.x) * second,
                    b.y + (c.y - b.y) * second
                ),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun RoundedBox(isFilled: Boolean) {
    val color = if (isSystemInDarkTheme()) Color.White else Color.Black
    Canvas(modifier = Modifier.size(35.dp).padding(9.dp)) {
        if (isFilled) {
            drawCircle(color = color)
        } else {
            drawCircle(color = color, style = Stroke(width = 1.dp.toPx()))
        }
    }
}
