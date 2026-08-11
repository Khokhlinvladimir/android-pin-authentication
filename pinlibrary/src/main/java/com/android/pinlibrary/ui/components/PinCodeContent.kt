package com.android.pinlibrary.ui.components

import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.android.pinlibrary.ui.systemdesign.indicator.RoundedBoxesRow
import com.android.pinlibrary.ui.motion.PinAuthMotionSpec
import com.android.pinlibrary.ui.systemdesign.indicator.PinIndicatorFeedback
import com.android.pinlibrary.utils.biometric.BiometricScannerScreen
import com.android.pinlibrary.utils.enums.PinCodeScenario
import com.android.pinlibrary.utils.helpers.fillArrayWithButtons
import com.android.pinlibrary.utils.keyboard.KeyboardButtonEnum
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
) = PinCodeContent(
    headerId = headerId,
    notification = notification,
    forgotMessageId = forgotMessageId,
    pinCodeStateManager = pinCodeStateManager,
    pinCodeScenario = pinCodeScenario,
    authenticationCallback = authenticationCallback,
    enabled = true,
    onClick = onClick
)

@Composable
fun PinCodeContent(
    headerId: Int,
    notification: String,
    forgotMessageId: Int,
    pinCodeStateManager: PinCodeStateManager? = null,
    pinCodeScenario: PinCodeScenario = PinCodeScenario.STUB,
    authenticationCallback: BiometricPrompt.AuthenticationCallback? = null,
    enabled: Boolean,
    onClick: (buttonArray: SnapshotStateList<Int>) -> Unit
) {
    val settingsManager = SettingsManager(context = LocalContext.current)
    val pinLength = settingsManager.getPinLength()
    val buttonArray = remember { mutableStateListOf<Int>() }
    var quantity by remember { mutableIntStateOf(0) }
    var showBiometricScreen by remember { mutableStateOf(false) }
    LaunchedEffect(pinCodeScenario) {
        if (pinCodeScenario == PinCodeScenario.VALIDATION) {
            showBiometricScreen = settingsManager.isAutoLaunchBiometricEnabled()
        }
    }

    val onKeyboardButtonClick: (KeyboardButtonEnum) -> Unit = { keyboardEnum ->
        if (enabled) {
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
        val dismissingCallback = remember(authenticationCallback) {
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    showBiometricScreen = false
                    authenticationCallback.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    showBiometricScreen = false
                    authenticationCallback.onAuthenticationSucceeded(result)
                }

                override fun onAuthenticationFailed() {
                    authenticationCallback.onAuthenticationFailed()
                }
            }
        }
        BiometricScannerScreen(authenticationCallback = dismissingCallback)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(
            targetState = headerId,
            transitionSpec = {
                (slideInHorizontally(tween(280)) { it / 3 } + fadeIn()) togetherWith
                    (slideOutHorizontally(tween(220)) { -it / 3 } + fadeOut())
            },
            label = "legacy-pin-step"
        ) { currentHeaderId ->
            PinCodeScreenHeader(stringResource(id = currentHeaderId))
        }
        Box(modifier = Modifier.height(64.dp), contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = notification,
                transitionSpec = {
                    (slideInVertically(tween(170)) { it / 3 } + fadeIn()) togetherWith
                        (slideOutVertically(tween(120)) { -it / 3 } + fadeOut())
                },
                label = "legacy-pin-notification"
            ) { currentNotification ->
                PinCodeScreenNotification(text = currentNotification)
            }
        }
        RoundedBoxesRow(
            startQuantity = pinLength,
            quantity = quantity,
            feedback = if (notification.isNotEmpty()) {
                PinIndicatorFeedback.ERROR
            } else {
                PinIndicatorFeedback.NORMAL
            },
            feedbackToken = notification,
            isProcessing = !enabled && notification.isEmpty(),
            motionSpec = PinAuthMotionSpec.Premium
        )
        Keyboard(
            pinCodeScenario = pinCodeScenario,
            enabled = enabled,
            onButtonClick = onKeyboardButtonClick
        )
        pinCodeStateManager?.let {
            PinCodeScreenForgot(stringResource(id = forgotMessageId), pinCodeStateManager = it)
        }
    }
}
