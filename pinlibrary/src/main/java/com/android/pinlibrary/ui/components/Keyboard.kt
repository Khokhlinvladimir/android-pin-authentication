package com.android.pinlibrary.ui.components

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
import androidx.compose.ui.unit.dp
import com.android.pinlibrary.R
import com.android.pinlibrary.utils.enums.PinCodeScenario
import com.android.pinlibrary.utils.keyboard.KeyboardButtonEnum
import com.android.pinlibrary.utils.listeners.NumberListener
import com.android.pinlibrary.utils.preferences.SettingsManager


@Composable
fun Keyboard(pinCodeScenario: PinCodeScenario) {

    val rowModifier = Modifier.wrapContentSize()

    Column(
        modifier = Modifier
            .wrapContentSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = rowModifier
        ) {
            TextButton(
                stringResource(id = R.string.button1_large_text),
                KeyboardButtonEnum.BUTTON_1
            )
            TextButton(
                stringResource(id = R.string.button2_large_text),
                KeyboardButtonEnum.BUTTON_2
            )
            TextButton(
                stringResource(id = R.string.button3_large_text),
                KeyboardButtonEnum.BUTTON_3
            )
        }
        Row(
            modifier = rowModifier
        ) {
            TextButton(
                stringResource(id = R.string.button4_large_text),
                KeyboardButtonEnum.BUTTON_4
            )
            TextButton(
                stringResource(id = R.string.button5_large_text),
                KeyboardButtonEnum.BUTTON_5
            )
            TextButton(
                stringResource(id = R.string.button6_large_text),
                KeyboardButtonEnum.BUTTON_6
            )
        }
        Row(
            modifier = rowModifier
        ) {
            TextButton(
                stringResource(id = R.string.button7_large_text),
                KeyboardButtonEnum.BUTTON_7
            )
            TextButton(
                stringResource(id = R.string.button8_large_text),
                KeyboardButtonEnum.BUTTON_8
            )
            TextButton(
                stringResource(id = R.string.button9_large_text),
                KeyboardButtonEnum.BUTTON_9
            )
        }
        Row(
            modifier = rowModifier
        ) {
            val settingsManager = SettingsManager(context = LocalContext.current)
            if (pinCodeScenario == PinCodeScenario.VALIDATION && settingsManager.isBiometricEnabled()) {
                ImageButton(
                    if (isSystemInDarkTheme()) R.drawable.ic_fingerprint_white_30 else R.drawable.ic_fingerprint_30,
                    KeyboardButtonEnum.BUTTON_FINGERPRINT
                )
            } else ImageButtonStub(R.drawable.ic_fingerprint_transparent_30)
            TextButton(
                stringResource(id = R.string.button0_large_text),
                KeyboardButtonEnum.BUTTON_0
            )
            ImageButton(
                if (isSystemInDarkTheme()) R.drawable.ic_clear_white_30 else R.drawable.ic_clear_30,
                KeyboardButtonEnum.BUTTON_CLEAR
            )
        }
    }
}

var onNumberClickListener: NumberListener? = null
fun setNumberClickListener(
    onNumberClickListener: NumberListener?,
    keyboardEnum: KeyboardButtonEnum
) {
    onNumberClickListener?.onNumberTriggered(keyboardEnum)
}
