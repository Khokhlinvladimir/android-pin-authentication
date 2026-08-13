package com.android.pinlibrary.ui.systemdesign.indicator

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.android.pinlibrary.ui.customization.PinMaskContent
import com.android.pinlibrary.ui.customization.PinMaskState
import com.android.pinlibrary.ui.customization.PinMaskStyle
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
) = RoundedBoxesRow(
    startQuantity = startQuantity,
    quantity = quantity,
    feedback = feedback,
    feedbackToken = feedbackToken,
    isProcessing = isProcessing,
    motionSpec = motionSpec,
    style = PinMaskStyle(),
    maskContent = { state, style, motion ->
        DefaultPinMask(state = state, style = style, motionSpec = motion)
    },
    modifier = modifier
)

@Composable
fun RoundedBoxesRow(
    startQuantity: Int,
    quantity: Int,
    feedback: PinIndicatorFeedback,
    feedbackToken: Any?,
    isProcessing: Boolean,
    motionSpec: PinAuthMotionSpec,
    style: PinMaskStyle,
    maskContent: PinMaskContent,
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
            .height(style.indicatorHeight)
            .widthIn(min = style.minimumWidth)
            .graphicsLayer { translationX = shake.value.dp.toPx() },
        contentAlignment = Alignment.Center
    ) {
        maskContent(
            PinMaskState(
                totalDigits = startQuantity,
                enteredDigits = quantity,
                feedback = feedback,
                isProcessing = isProcessing
            ),
            style,
            motionSpec
        )
    }
}

@Composable
fun DefaultPinMask(
    state: PinMaskState,
    style: PinMaskStyle = PinMaskStyle(),
    motionSpec: PinAuthMotionSpec = PinAuthMotionSpec.Premium
) {
    if (state.feedback == PinIndicatorFeedback.SUCCESS) {
        MergingSuccessIndicator(
            count = state.totalDigits,
            motionSpec = motionSpec,
            style = style
        )
    } else {
        PinDots(
            count = state.totalDigits,
            filledCount = state.enteredDigits,
            feedback = state.feedback,
            isProcessing = state.isProcessing,
            motionSpec = motionSpec,
            style = style
        )
    }
}

@Composable
private fun PinDots(
    count: Int,
    filledCount: Int,
    feedback: PinIndicatorFeedback,
    isProcessing: Boolean,
    motionSpec: PinAuthMotionSpec,
    style: PinMaskStyle
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
    val primary = style.filledColor.resolved(MaterialTheme.colorScheme.primary)
    val error = style.errorColor.resolved(MaterialTheme.colorScheme.error)
    val outline = style.emptyColor.resolved(
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f)
    )

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
                style = style,
                modifier = Modifier.padding(horizontal = style.dotSpacing)
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
    style: PinMaskStyle,
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
            .size(style.dotSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = pulse
            }
    ) {
        val radius = style.dotRadius.toPx()
        drawCircle(
            color = if (fillProgress > 0f) color.copy(alpha = fillProgress) else Color.Transparent,
            radius = radius * (0.78f + 0.22f * fillProgress)
        )
        drawCircle(
            color = if (fillProgress > 0f) color else outline,
            radius = radius,
            style = Stroke(width = style.outlineWidth.toPx())
        )
    }
}

@Composable
private fun MergingSuccessIndicator(
    count: Int,
    motionSpec: PinAuthMotionSpec,
    style: PinMaskStyle
) {
    val progress = remember { Animatable(if (motionSpec.enabled) 0f else 1f) }
    val initialColor = style.filledColor.resolved(MaterialTheme.colorScheme.primary)
    val themedSuccessColor = if (isSystemInDarkTheme()) {
        Color(0xFF63E69A)
    } else {
        Color(0xFF168A4A)
    }
    val successColor = style.successColor.resolved(themedSuccessColor)
    LaunchedEffect(motionSpec.enabled) {
        if (motionSpec.enabled) {
            progress.snapTo(0f)
            progress.animateTo(
                1f,
                animationSpec = tween(
                    durationMillis = motionSpec.feedbackDurationMillis,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    Canvas(
        modifier = Modifier
            .size(
                width = maxOf(style.minimumWidth, style.dotSize * count + style.dotSpacing * count * 2),
                height = style.successSize + 8.dp
            )
    ) {
        val mergeProgress = (progress.value / 0.78f).coerceIn(0f, 1f)
        val colorProgress = ((progress.value - 0.18f) / 0.66f).coerceIn(0f, 1f)
        val checkProgress = ((progress.value - 0.76f) / 0.24f).coerceIn(0f, 1f)
        val dotRadius = style.dotRadius.toPx()
        val finalRadius = style.successSize.toPx() / 2f
        val radius = dotRadius + (finalRadius - dotRadius) * mergeProgress
        val spacing = (style.dotSize + style.dotSpacing * 2).toPx()
        val center = this.center
        val mergedColor = lerp(initialColor, successColor, colorProgress)

        repeat(count.coerceAtLeast(1)) { index ->
            val initialOffset = (index - (count - 1) / 2f) * spacing
            drawCircle(
                color = mergedColor,
                radius = radius,
                center = Offset(
                    x = center.x + initialOffset * (1f - mergeProgress),
                    y = center.y
                )
            )
        }

        if (style.showSuccessCheck && checkProgress > 0f) {
            val stroke = style.successStrokeWidth.toPx()
            val scale = style.successSize.toPx() / 50.dp.toPx()
            val a = Offset(center.x - 10.dp.toPx() * scale, center.y)
            val b = Offset(center.x - 2.dp.toPx() * scale, center.y + 8.dp.toPx() * scale)
            val c = Offset(center.x + 13.dp.toPx() * scale, center.y - 9.dp.toPx() * scale)
            val first = (checkProgress * 2f).coerceIn(0f, 1f)
            val second = ((checkProgress - 0.5f) * 2f).coerceIn(0f, 1f)
            drawLine(
                color = style.successContentColor,
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
                    color = style.successContentColor,
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
}

private fun Color.resolved(defaultColor: Color): Color =
    if (this == Color.Unspecified) defaultColor else this

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
