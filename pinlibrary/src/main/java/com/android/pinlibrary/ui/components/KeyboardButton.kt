package com.android.pinlibrary.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import com.android.pinlibrary.ui.customization.PinDigitContent
import com.android.pinlibrary.ui.customization.PinKeypadStyle
import com.android.pinlibrary.ui.motion.PinAuthMotionSpec
import com.android.pinlibrary.utils.keyboard.KeyboardButtonEnum
import com.android.pinlibrary.utils.listeners.NumberListener

@Composable
fun NumberButton(
    number: String,
    keyboardEnum: KeyboardButtonEnum
) = NumberButton(number, keyboardEnum, true) {}

@Composable
fun NumberButton(
    number: String,
    keyboardEnum: KeyboardButtonEnum,
    enabled: Boolean,
    onButtonClick: (KeyboardButtonEnum) -> Unit
) = NumberButton(
    number = number,
    keyboardEnum = keyboardEnum,
    enabled = enabled,
    motionSpec = PinAuthMotionSpec.Premium,
    onButtonClick = onButtonClick
)

@Composable
fun NumberButton(
    number: String,
    keyboardEnum: KeyboardButtonEnum,
    enabled: Boolean,
    motionSpec: PinAuthMotionSpec,
    onButtonClick: (KeyboardButtonEnum) -> Unit
) = NumberButton(
    number = number,
    keyboardEnum = keyboardEnum,
    enabled = enabled,
    motionSpec = motionSpec,
    style = PinKeypadStyle(),
    digitContent = { digit, _, style ->
        Text(
            text = digit.toString(),
            style = style.digitTextStyle,
            color = style.resolvedContentColor()
        )
    },
    onButtonClick = onButtonClick
)

@Composable
fun NumberButton(
    number: String,
    keyboardEnum: KeyboardButtonEnum,
    enabled: Boolean,
    motionSpec: PinAuthMotionSpec,
    style: PinKeypadStyle,
    digitContent: PinDigitContent,
    onButtonClick: (KeyboardButtonEnum) -> Unit
) {
    AnimatedKeySurface(
        enabled = enabled,
        motionSpec = motionSpec,
        style = style,
        onClick = { onButtonClick(keyboardEnum) }
    ) {
        digitContent(number.toInt(), enabled, style)
    }
}

@Composable
fun ImageButton(
    resourceId: Int,
    keyboardEnum: KeyboardButtonEnum
) = ImageButton(resourceId, keyboardEnum, true) {}

@Composable
fun ImageButton(
    resourceId: Int,
    keyboardEnum: KeyboardButtonEnum,
    enabled: Boolean,
    onButtonClick: (KeyboardButtonEnum) -> Unit
) = ImageButton(
    resourceId = resourceId,
    keyboardEnum = keyboardEnum,
    enabled = enabled,
    motionSpec = PinAuthMotionSpec.Premium,
    onButtonClick = onButtonClick
)

@Composable
fun ImageButton(
    resourceId: Int,
    keyboardEnum: KeyboardButtonEnum,
    enabled: Boolean,
    motionSpec: PinAuthMotionSpec,
    onButtonClick: (KeyboardButtonEnum) -> Unit
) = ImageButton(
    resourceId = resourceId,
    keyboardEnum = keyboardEnum,
    enabled = enabled,
    motionSpec = motionSpec,
    style = PinKeypadStyle(),
    onButtonClick = onButtonClick
)

@Composable
fun ImageButton(
    resourceId: Int,
    keyboardEnum: KeyboardButtonEnum,
    enabled: Boolean,
    motionSpec: PinAuthMotionSpec,
    style: PinKeypadStyle,
    onButtonClick: (KeyboardButtonEnum) -> Unit
) {
    val painter = painterResource(id = resourceId)
    AnimatedKeySurface(
        enabled = enabled,
        motionSpec = motionSpec,
        style = style,
        onClick = { onButtonClick(keyboardEnum) }
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.graphicsLayer {
                alpha = if (enabled) 1f else 0.45f
            }
        )
    }
}

@Composable
private fun AnimatedKeySurface(
    enabled: Boolean,
    motionSpec: PinAuthMotionSpec,
    style: PinKeypadStyle,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (motionSpec.enabled && isPressed) motionSpec.pressScale else 1f,
        animationSpec = if (motionSpec.enabled) {
            spring(dampingRatio = 0.42f, stiffness = 720f)
        } else {
            tween(0)
        },
        label = "pin-key-scale"
    )
    val view = LocalView.current
    val surfaceColor = if (isPressed) {
        style.pressedContainerColor.takeUnless { it == androidx.compose.ui.graphics.Color.Unspecified }
            ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
    } else {
        style.containerColor.takeUnless { it == androidx.compose.ui.graphics.Color.Unspecified }
            ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)
    }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(
                vertical = style.verticalKeySpacing,
                horizontal = style.horizontalKeySpacing
            )
    ) {
        Box(
            modifier = Modifier
                .size(style.keySize)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    alpha = if (enabled) 1f else style.disabledAlpha
                }
                .clip(style.keyShape)
                .background(surfaceColor)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = rememberRipple(
                        bounded = true,
                        radius = style.keySize / 2
                    )
                ) {
                    if (motionSpec.hapticsEnabled) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun PinKeypadStyle.resolvedContentColor() =
    if (contentColor == androidx.compose.ui.graphics.Color.Unspecified) {
        MaterialTheme.colorScheme.onSurface
    } else {
        contentColor
    }

@Composable
fun ImageButtonStub(resourceId: Int) {
    ImageButtonStub(resourceId = resourceId, style = PinKeypadStyle())
}

@Composable
fun ImageButtonStub(resourceId: Int, style: PinKeypadStyle) {
    val painter = painterResource(id = resourceId)
    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(
                vertical = style.verticalKeySpacing,
                horizontal = style.horizontalKeySpacing
            )
    ) {
        Box(
            modifier = Modifier
                .size(style.keySize)
                .clip(style.keyShape),
            contentAlignment = Alignment.Center
        ) {
            Image(painter = painter, contentDescription = null)
        }
    }
}

@Deprecated("The library keyboard now delivers clicks directly to its owning screen")
internal var onNumberClickListener: NumberListener? = null

@Deprecated("The library keyboard now delivers clicks directly to its owning screen")
internal fun setNumberClickListener(
    onNumberClickListener: NumberListener?,
    keyboardEnum: KeyboardButtonEnum
) {
    onNumberClickListener?.onNumberTriggered(keyboardEnum)
}
