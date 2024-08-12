package com.android.pinlibrary.ui.screens

import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.android.pinlibrary.R
import com.android.pinlibrary.ui.components.PinCodeContent
import com.android.pinlibrary.utils.enums.PinCodeScenario
import com.android.pinlibrary.utils.preferences.AttemptCounter
import com.android.pinlibrary.utils.preferences.PinCodeManager
import com.android.pinlibrary.utils.state.PinCodeStateManager
import com.android.pinlibrary.utils.state.validationpin.ValidationPinScreenIntent
import com.android.pinlibrary.utils.state.validationpin.ValidationPinScreenState
import com.android.pinlibrary.viewmodel.PinViewModel

@Composable
fun ValidationPinScreen(
    viewModel: PinViewModel,
    pinCodeStateManager: PinCodeStateManager,
    pinCodeScenario: PinCodeScenario
) {

    val context = LocalContext.current
    val pinCodeManager = PinCodeManager(context = context)
    val attemptCounter = AttemptCounter(context = context)
    var isInitScenario by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var headerId by remember { mutableIntStateOf(R.string.pin_code_step_create) }
    val notificationText = stringResource(id = R.string.empty)
    var notification by remember { mutableStateOf(notificationText) }
    val forgotMessageId by remember { mutableIntStateOf(R.string.pin_code_forgot_text) }

    if (!isInitScenario && pinCodeStateManager.isValidationEnabled) {
        viewModel.processIntent(ValidationPinScreenIntent.InitialState)
        isInitScenario = true
    }

    when (val state = viewModel.validationPinScreenState.observeAsState().value) {
        is ValidationPinScreenState.InitialState -> {
            headerId = R.string.pin_code_step_unlock
        }

        is ValidationPinScreenState.EnteringPinState -> {
            if (pinCodeManager.isPinCodeCorrect(state.pin)) {
                viewModel.processIntent(ValidationPinScreenIntent.ValidatePin)
                attemptCounter.resetAttempts()
            } else {
                if (!isError) {
                    attemptCounter.decrementAttempts()
                    notification = stringResource(
                        id = R.string.pin_code_attempts,
                        attemptCounter.getAttempts()
                    )
                    if (attemptCounter.getAttempts() == 0) {
                        pinCodeStateManager.setLoginAttemptsExpended()
                    }
                    isError = true
                }
            }
        }

        is ValidationPinScreenState.PinValidatedState -> {
            pinCodeStateManager.setValidationSuccess(true)
        }

        is ValidationPinScreenState.ErrorState -> {
            pinCodeStateManager.setValidationSuccess(false)
        }

        else -> {}
    }

    val authenticationCallback = @RequiresApi(Build.VERSION_CODES.P)
    object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            pinCodeStateManager.setBiometricAuthentication(false)
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            pinCodeStateManager.setBiometricAuthentication(true)
        }

        override fun onAuthenticationFailed() {
            pinCodeStateManager.setBiometricAuthentication(false)
        }
    }

    PinCodeContent(
        headerId = headerId,
        notification = notification,
        pinCodeScenario = pinCodeScenario,
        authenticationCallback = authenticationCallback,
        forgotMessageId = forgotMessageId,
        pinCodeStateManager = pinCodeStateManager
    ) { pinValue ->
        val pinCode = pinValue.toList().joinToString("")
        viewModel.processIntent(ValidationPinScreenIntent.EnterPin(pinCode))
        isError = false
    }
}


