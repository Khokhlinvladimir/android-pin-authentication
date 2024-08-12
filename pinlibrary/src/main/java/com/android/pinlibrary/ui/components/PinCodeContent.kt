package com.android.pinlibrary.ui.components

import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.android.pinlibrary.ui.systemdesign.indicator.RoundedBoxesRow
import com.android.pinlibrary.utils.biometric.BiometricScannerScreen
import com.android.pinlibrary.utils.enums.PinCodeScenario
import com.android.pinlibrary.utils.helpers.fillArrayWithButtons
import com.android.pinlibrary.utils.keyboard.KeyboardButtonEnum
import com.android.pinlibrary.utils.listeners.NumberListener
import com.android.pinlibrary.utils.preferences.SettingsManager
import com.android.pinlibrary.utils.state.PinCodeStateManager

@Composable
fun PinCodeContent(
    headerId: Int,
    notification: String,
    forgotMessageId: Int,
    pinCodeStateManager: PinCodeStateManager? = null,
    pinCodeScenario: PinCodeScenario = PinCodeScenario.STUB,
    authenticationCallback: BiometricPrompt.AuthenticationCallback? = null,
    onClick: (buttonArray: SnapshotStateList<Int>) -> Unit
) {
    val settingsManager = SettingsManager(context = LocalContext.current)
    val pinLength = settingsManager.getPinLength()
    val buttonArray = remember { mutableStateListOf<Int>() }
    var quantity by remember { mutableIntStateOf(0) }
    var showBiometricScreen by remember { mutableStateOf(false) }

    LaunchedEffect(onNumberClickListener) {
        onNumberClickListener = NumberListener { keyboardEnum ->
            Log.d("TAGSS", "keyboardEnum = $keyboardEnum, showBiometricScreen = $showBiometricScreen")
            when (keyboardEnum) {
                KeyboardButtonEnum.BUTTON_FINGERPRINT -> showBiometricScreen = true
                else -> {
                    fillArrayWithButtons(keyboardEnum, buttonArray, pinLength) { updatedArray ->
                        quantity = updatedArray.size
                    }
                    if (quantity == pinLength) {
                        onClick(buttonArray)
                        quantity = 0
                        buttonArray.clear()
                    }
                    showBiometricScreen = false
                }
            }
        }
    }

    if (showBiometricScreen && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && authenticationCallback != null) {
        BiometricScannerScreen(authenticationCallback = authenticationCallback)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PinCodeScreenHeader(stringResource(id = headerId))
        PinCodeScreenNotification(text = notification)
        RoundedBoxesRow(startQuantity = pinLength, quantity = quantity)
        Keyboard(pinCodeScenario = pinCodeScenario)
        pinCodeStateManager?.let {
            PinCodeScreenForgot(stringResource(id = forgotMessageId), pinCodeStateManager = it)
        }
    }
}
