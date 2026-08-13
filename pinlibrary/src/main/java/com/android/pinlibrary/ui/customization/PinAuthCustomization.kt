package com.android.pinlibrary.ui.customization

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.pinlibrary.ui.motion.PinAuthMotionSpec
import com.android.pinlibrary.ui.systemdesign.indicator.DefaultPinMask
import com.android.pinlibrary.ui.systemdesign.indicator.PinIndicatorFeedback

/** Visual properties of the numeric keypad. Unspecified colors inherit MaterialTheme. */
@Immutable
data class PinKeypadStyle(
    val keySize: Dp = 50.dp,
    val horizontalKeySpacing: Dp = 23.dp,
    val verticalKeySpacing: Dp = 10.dp,
    val horizontalContentPadding: Dp = 20.dp,
    val verticalContentPadding: Dp = 16.dp,
    val keyShape: Shape = CircleShape,
    val containerColor: Color = Color.Unspecified,
    val pressedContainerColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
    val disabledAlpha: Float = 0.5f,
    val digitTextStyle: TextStyle = TextStyle(fontSize = 25.sp)
) {
    init {
        require(keySize > 0.dp) { "Key size must be positive" }
        require(horizontalKeySpacing >= 0.dp) { "Horizontal key spacing cannot be negative" }
        require(verticalKeySpacing >= 0.dp) { "Vertical key spacing cannot be negative" }
        require(horizontalContentPadding >= 0.dp) { "Horizontal content padding cannot be negative" }
        require(verticalContentPadding >= 0.dp) { "Vertical content padding cannot be negative" }
        require(disabledAlpha in 0f..1f) { "Disabled alpha must be between 0 and 1" }
    }
}

/** Visual properties of the default PIN mask and its success confirmation. */
@Immutable
data class PinMaskStyle(
    val indicatorHeight: Dp = 72.dp,
    val minimumWidth: Dp = 96.dp,
    val dotSize: Dp = 26.dp,
    val dotSpacing: Dp = 5.dp,
    val dotRadius: Dp = 11.dp,
    val outlineWidth: Dp = 1.6.dp,
    val filledColor: Color = Color.Unspecified,
    val emptyColor: Color = Color.Unspecified,
    val errorColor: Color = Color.Unspecified,
    val successColor: Color = Color.Unspecified,
    val successContentColor: Color = Color.White,
    val successSize: Dp = 50.dp,
    val successStrokeWidth: Dp = 3.5.dp
) {
    init {
        require(indicatorHeight > 0.dp) { "Indicator height must be positive" }
        require(minimumWidth > 0.dp) { "Indicator minimum width must be positive" }
        require(dotSize > 0.dp) { "Dot size must be positive" }
        require(dotSpacing >= 0.dp) { "Dot spacing cannot be negative" }
        require(dotRadius > 0.dp && dotRadius <= dotSize / 2) {
            "Dot radius must fit inside dot size"
        }
        require(outlineWidth >= 0.dp) { "Outline width cannot be negative" }
        require(successSize > 0.dp) { "Success size must be positive" }
        require(successStrokeWidth > 0.dp) { "Success stroke width must be positive" }
    }
}

/** Immutable state passed to a custom PIN mask renderer. */
@Immutable
data class PinMaskState(
    val totalDigits: Int,
    val enteredDigits: Int,
    val feedback: PinIndicatorFeedback,
    val isProcessing: Boolean
)

typealias PinDigitContent = @Composable (
    digit: Int,
    enabled: Boolean,
    style: PinKeypadStyle
) -> Unit

typealias PinMaskContent = @Composable (
    state: PinMaskState,
    style: PinMaskStyle,
    motionSpec: PinAuthMotionSpec
) -> Unit

/**
 * Customization entry point for [com.android.pinlibrary.ui.screens.PinAuthScreen].
 *
 * The library keeps key interaction, haptics, enabled state, layout, and controller wiring.
 * A host can style those surfaces or replace only their visual content through the slots.
 */
@Immutable
class PinAuthCustomization(
    val keypadStyle: PinKeypadStyle = PinKeypadStyle(),
    val maskStyle: PinMaskStyle = PinMaskStyle(),
    val digitContent: PinDigitContent = { digit, _, style ->
        Text(
            text = digit.toString(),
            style = style.digitTextStyle,
            color = style.contentColor.takeOrElse { MaterialTheme.colorScheme.onSurface }
        )
    },
    val maskContent: PinMaskContent = { state, style, motionSpec ->
        DefaultPinMask(state = state, style = style, motionSpec = motionSpec)
    }
) {
    companion object {
        @JvmField
        val Default = PinAuthCustomization()
    }
}

private inline fun Color.takeOrElse(defaultValue: () -> Color): Color =
    if (this == Color.Unspecified) defaultValue() else this
