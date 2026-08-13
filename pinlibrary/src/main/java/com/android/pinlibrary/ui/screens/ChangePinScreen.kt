package com.android.pinlibrary.ui.screens

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
import com.android.pinlibrary.utils.preferences.AttemptCounter
import com.android.pinlibrary.utils.preferences.PinCodeManager
import com.android.pinlibrary.utils.state.PinCodeStateManager
import com.android.pinlibrary.utils.state.changepin.ChangePinScreenIntent
import com.android.pinlibrary.viewmodel.PinViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class ChangePinStep {
    CURRENT,
    NEW,
    CONFIRM
}

@Composable
fun ChangePinScreen(
    viewModel: PinViewModel,
    pinCodeStateManager: PinCodeStateManager,
    customization: PinAuthCustomization = PinAuthCustomization.Default
) {
    val context = LocalContext.current
    val pinCodeManager = remember(context) { PinCodeManager(context) }
    val attemptCounter = remember(context) { AttemptCounter(context) }
    val coroutineScope = rememberCoroutineScope()
    var step by remember { mutableStateOf(ChangePinStep.CURRENT) }
    var headerId by remember { mutableIntStateOf(R.string.pin_code_step_unlock) }
    var notification by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf<String?>(null) }
    var attempts by remember { mutableIntStateOf(attemptCounter.getAttempts()) }
    var isProcessing by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var exhaustionReported by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.processIntent(ChangePinScreenIntent.InitialState)
        if (attempts == 0 && !exhaustionReported) {
            notification = context.getString(R.string.pin_code_attempts, 0)
            exhaustionReported = true
            pinCodeStateManager.setLoginAttemptsExpended()
        }
    }

    PinCodeContent(
        headerId = headerId,
        notification = notification,
        forgotMessageId = R.string.pin_code_forgot_text,
        pinCodeStateManager = pinCodeStateManager,
        enabled = !isProcessing && !isCompleted && (step != ChangePinStep.CURRENT || attempts > 0),
        customization = customization
    ) { pinValue ->
        if (isProcessing || isCompleted) return@PinCodeContent
        val pinCode = pinValue.joinToString("")
        when (step) {
            ChangePinStep.CURRENT -> {
                if (attempts == 0) return@PinCodeContent
                isProcessing = true
                viewModel.processIntent(ChangePinScreenIntent.EnterCurrentPin(pinCode))
                coroutineScope.launch {
                    val isCorrect = withContext(Dispatchers.Default) {
                        pinCodeManager.isPinCodeCorrect(pinCode)
                    }
                    if (isCorrect) {
                        attemptCounter.resetAttempts()
                        attempts = attemptCounter.getAttempts()
                        step = ChangePinStep.NEW
                        headerId = R.string.pin_code_step_create
                        notification = ""
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

            ChangePinStep.NEW -> {
                newPin = pinCode
                step = ChangePinStep.CONFIRM
                headerId = R.string.pin_code_step_enable_confirm
                notification = ""
                viewModel.processIntent(ChangePinScreenIntent.EnterNewPin(pinCode))
            }

            ChangePinStep.CONFIRM -> {
                viewModel.processIntent(ChangePinScreenIntent.ConfirmNewPin(pinCode))
                if (newPin != pinCode) {
                    newPin = null
                    step = ChangePinStep.NEW
                    headerId = R.string.pin_code_step_create
                    notification = context.getString(R.string.pin_codes_do_not_match)
                } else {
                    isProcessing = true
                    coroutineScope.launch {
                        val result = runCatching {
                            withContext(Dispatchers.Default) { pinCodeManager.savePinCode(pinCode) }
                        }
                        if (result.isSuccess) {
                            isCompleted = true
                            viewModel.processIntent(ChangePinScreenIntent.ChangePin)
                            pinCodeStateManager.setChangeSuccess(true)
                        } else {
                            newPin = null
                            step = ChangePinStep.NEW
                            headerId = R.string.pin_code_step_create
                            notification = context.getString(R.string.pin_code_storage_error)
                            pinCodeStateManager.setChangeSuccess(false)
                        }
                        isProcessing = false
                    }
                }
            }
        }
    }
}
