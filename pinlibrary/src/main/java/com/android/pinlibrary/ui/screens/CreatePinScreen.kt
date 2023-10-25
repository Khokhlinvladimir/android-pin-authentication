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
import com.android.pinlibrary.utils.state.createpin.CreatePinScreenIntent
import com.android.pinlibrary.utils.state.createpin.CreatePinScreenState
import com.android.pinlibrary.viewmodel.PinViewModel

private var tempPinCode: String = ""

@Composable
fun CreatePinScreen(
    viewModel: PinViewModel,
    pinCodeStateManager: PinCodeStateManager
) {
    var isInitScenario by remember { mutableStateOf(false) }

    if (!isInitScenario) {
        viewModel.processIntent(CreatePinScreenIntent.InitialState)
        isInitScenario = true
    }

    val context = LocalContext.current
    val pinCodeManager = PinCodeManager(context = context)
    val attemptCounter = AttemptCounter(context = context)

    var headerId by remember { mutableIntStateOf(R.string.pin_code_step_create) }
    val notificationText = stringResource(id = R.string.empty)
    var notification by remember { mutableStateOf(notificationText) }
    val forgotMessageId by remember { mutableIntStateOf(R.string.pin_code_forgot_text) }
    var isPinEntering by remember { mutableStateOf(false) }

    when (val state = viewModel.createPinScreenState.observeAsState().value) {

        is CreatePinScreenState.InitialState -> {
            headerId = R.string.pin_code_step_create
            isPinEntering = false
        }

        is CreatePinScreenState.EnteringPinState -> {
            headerId = R.string.pin_code_step_enable_confirm
            notification = stringResource(id = R.string.empty)
            isPinEntering = true
            tempPinCode = state.pin
        }

        is CreatePinScreenState.ConfirmingPinState -> {
            if (tempPinCode == state.pin) {
                pinCodeManager.savePinCode(state.pin)
                viewModel.processIntent(CreatePinScreenIntent.CreatePin)
            } else {
                notification = stringResource(id = R.string.pin_codes_do_not_match)
                viewModel.processIntent(CreatePinScreenIntent.InitialState)
            }
        }

        is CreatePinScreenState.PinCreatedState -> {
            pinCodeStateManager.setCreationSuccess(true)
            attemptCounter.resetAttempts()
        }

        is CreatePinScreenState.ErrorState -> {
            pinCodeStateManager.setCreationSuccess(false)
            notification = stringResource(id = R.string.pin_codes_do_not_match)
        }

        else -> {}
    }

    PinCodeContent(
        headerId = headerId,
        notification = notification,
        forgotMessageId = forgotMessageId
    ) {
        val pinCode = it.toList().joinToString("")
        if (!isPinEntering) {
            viewModel.processIntent(CreatePinScreenIntent.EnterPin(pinCode))
        } else {
            viewModel.processIntent(CreatePinScreenIntent.ConfirmPin(pinCode))
        }
    }
}
