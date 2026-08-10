package com.android.pinlibrary.ui.components

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.android.pinlibrary.R
import com.android.pinlibrary.ui.systemdesign.theme.Dimens
import com.android.pinlibrary.utils.enums.PinCodeScenario
import com.android.pinlibrary.utils.keyboard.KeyboardButtonEnum
import com.android.pinlibrary.utils.preferences.SettingsManager

@Composable
fun Keyboard(pinCodeScenario: PinCodeScenario) = Keyboard(pinCodeScenario, true) {}

@Composable
fun Keyboard(
    pinCodeScenario: PinCodeScenario,
    enabled: Boolean,
    onButtonClick: (KeyboardButtonEnum) -> Unit
) {

    val rowModifier = Modifier.wrapContentSize()

    Column(
        modifier = Modifier
            .wrapContentSize()
            .padding(
                vertical = Dimens.verticalKeyboardPadding,
                horizontal = Dimens.horizontalKeyboardPadding
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = rowModifier
        ) {
            NumberButton(
                stringResource(id = R.string.button1_large_text),
                KeyboardButtonEnum.BUTTON_1,
                enabled,
                onButtonClick
            )
            NumberButton(
                stringResource(id = R.string.button2_large_text),
                KeyboardButtonEnum.BUTTON_2,
                enabled,
                onButtonClick
            )
            NumberButton(
                stringResource(id = R.string.button3_large_text),
                KeyboardButtonEnum.BUTTON_3,
                enabled,
                onButtonClick
            )
        }
        Row(
            modifier = rowModifier
        ) {
            NumberButton(
                stringResource(id = R.string.button4_large_text),
                KeyboardButtonEnum.BUTTON_4,
                enabled,
                onButtonClick
            )
            NumberButton(
                stringResource(id = R.string.button5_large_text),
                KeyboardButtonEnum.BUTTON_5,
                enabled,
                onButtonClick
            )
            NumberButton(
                stringResource(id = R.string.button6_large_text),
                KeyboardButtonEnum.BUTTON_6,
                enabled,
                onButtonClick
            )
        }
        Row(
            modifier = rowModifier
        ) {
            NumberButton(
                stringResource(id = R.string.button7_large_text),
                KeyboardButtonEnum.BUTTON_7,
                enabled,
                onButtonClick
            )
            NumberButton(
                stringResource(id = R.string.button8_large_text),
                KeyboardButtonEnum.BUTTON_8,
                enabled,
                onButtonClick
            )
            NumberButton(
                stringResource(id = R.string.button9_large_text),
                KeyboardButtonEnum.BUTTON_9,
                enabled,
                onButtonClick
            )
        }
        Row(
            modifier = rowModifier
        ) {
            val settingsManager = SettingsManager(context = LocalContext.current)
            if (
                pinCodeScenario == PinCodeScenario.VALIDATION
                && settingsManager.isBiometricEnabled()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                ) {
                ImageButton(
                    if (isSystemInDarkTheme()) R.drawable.ic_fingerprint_white_30 else R.drawable.ic_fingerprint_30,
                    KeyboardButtonEnum.BUTTON_FINGERPRINT,
                    enabled,
                    onButtonClick
                )
            } else {
                ImageButtonStub(R.drawable.ic_fingerprint_transparent_30)
            }
            NumberButton(
                stringResource(id = R.string.button0_large_text),
                KeyboardButtonEnum.BUTTON_0,
                enabled,
                onButtonClick
            )
            ImageButton(
                if (isSystemInDarkTheme()) R.drawable.ic_clear_white_30 else R.drawable.ic_clear_30,
                KeyboardButtonEnum.BUTTON_CLEAR,
                enabled,
                onButtonClick
            )
        }
    }
}
