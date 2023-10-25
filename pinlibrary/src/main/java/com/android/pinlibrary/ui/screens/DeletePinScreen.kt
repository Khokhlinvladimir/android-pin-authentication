package com.android.pinlibrary.ui.screens

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
import com.android.pinlibrary.utils.preferences.AttemptCounter
import com.android.pinlibrary.utils.preferences.PinCodeManager
import com.android.pinlibrary.utils.state.PinCodeStateManager
import com.android.pinlibrary.utils.state.deletepin.DeletePinScreenIntent
import com.android.pinlibrary.utils.state.deletepin.DeletePinScreenState
import com.android.pinlibrary.viewmodel.PinViewModel

@Composable
fun DeletePinScreen(
    viewModel: PinViewModel,
    pinCodeStateManager: PinCodeStateManager
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

    if (!isInitScenario) {
        viewModel.processIntent(DeletePinScreenIntent.InitialState)
        isInitScenario = true
    }

    when (val state = viewModel.deletePinScreenState.observeAsState().value) {
        is DeletePinScreenState.InitialState -> {
            headerId = R.string.pin_code_step_unlock
        }

        is DeletePinScreenState.EnteringPinState -> {
            if (pinCodeManager.isPinCodeCorrect(state.pin)) {
                viewModel.processIntent(DeletePinScreenIntent.DeletePin)
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

        is DeletePinScreenState.PinDeletedState -> {
            pinCodeStateManager.setDeletionSuccess(true)
        }

        is DeletePinScreenState.ErrorState -> {
            pinCodeStateManager.setDeletionSuccess(false)
        }

        else -> {}
    }

    PinCodeContent(
        headerId = headerId,
        notification = notification,
        forgotMessageId = forgotMessageId,
        pinCodeStateManager = pinCodeStateManager
    ) {
        val pinCode = it.toList().joinToString("")
        viewModel.processIntent(DeletePinScreenIntent.EnterPin(pinCode))
        isError = false
    }
}