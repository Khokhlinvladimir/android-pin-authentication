package com.android.pinlibrary.ui.screens

import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.android.pinlibrary.R
import com.android.pinlibrary.ui.components.PinCodeContent
import com.android.pinlibrary.ui.customization.PinAuthCustomization
import com.android.pinlibrary.utils.enums.PinCodeScenario
import com.android.pinlibrary.utils.preferences.AttemptCounter
import com.android.pinlibrary.utils.preferences.PinCodeManager
import com.android.pinlibrary.utils.state.PinCodeStateManager
import com.android.pinlibrary.utils.state.validationpin.ValidationPinScreenIntent
import com.android.pinlibrary.viewmodel.PinViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ValidationPinScreen(
    viewModel: PinViewModel,
    pinCodeStateManager: PinCodeStateManager,
    pinCodeScenario: PinCodeScenario,
    customization: PinAuthCustomization = PinAuthCustomization.Default
) {
    val context = LocalContext.current
    val pinCodeManager = remember(context) { PinCodeManager(context) }
    val attemptCounter = remember(context) { AttemptCounter(context) }
    val coroutineScope = rememberCoroutineScope()
    var attempts by remember { mutableIntStateOf(attemptCounter.getAttempts()) }
    var notification by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var exhaustionReported by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (pinCodeStateManager.isValidationEnabled) {
            viewModel.processIntent(ValidationPinScreenIntent.InitialState)
        }
        if (attempts == 0 && !exhaustionReported) {
            notification = context.getString(R.string.pin_code_attempts, 0)
            exhaustionReported = true
            pinCodeStateManager.setLoginAttemptsExpended()
        }
    }

    val authenticationCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        rememberPlatformBiometricCallback(pinCodeStateManager)
    } else {
        null
    }

    PinCodeContent(
        headerId = R.string.pin_code_step_unlock,
        notification = notification,
        pinCodeScenario = pinCodeScenario,
        authenticationCallback = authenticationCallback,
        forgotMessageId = R.string.pin_code_forgot_text,
        pinCodeStateManager = pinCodeStateManager,
        enabled = !isProcessing && !isCompleted && attempts > 0,
        customization = customization
    ) { pinValue ->
        if (isProcessing || isCompleted || attempts == 0) return@PinCodeContent
        val pinCode = pinValue.joinToString("")
        isProcessing = true
        viewModel.processIntent(ValidationPinScreenIntent.EnterPin(pinCode))

        coroutineScope.launch {
            val isCorrect = withContext(Dispatchers.Default) {
                pinCodeManager.isPinCodeCorrect(pinCode)
            }
            if (isCorrect) {
                attemptCounter.resetAttempts()
                attempts = attemptCounter.getAttempts()
                isCompleted = true
                viewModel.processIntent(ValidationPinScreenIntent.ValidatePin)
                pinCodeStateManager.setValidationSuccess(true)
            } else {
                attemptCounter.decrementAttempts()
                attempts = attemptCounter.getAttempts()
                notification = context.getString(R.string.pin_code_attempts, attempts)
                if (attempts == 0 && !exhaustionReported) {
                    exhaustionReported = true
                    pinCodeStateManager.setLoginAttemptsExpended()
                }
            }
            isProcessing = false
        }
    }
}

@Composable
@RequiresApi(Build.VERSION_CODES.P)
private fun rememberPlatformBiometricCallback(
    pinCodeStateManager: PinCodeStateManager
): BiometricPrompt.AuthenticationCallback = remember(pinCodeStateManager) {
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
    }
