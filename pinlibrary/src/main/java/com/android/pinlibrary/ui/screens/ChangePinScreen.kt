package com.android.pinlibrary.ui.screens

import android.util.Log
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
import com.android.pinlibrary.utils.state.changepin.ChangePinScreenIntent
import com.android.pinlibrary.utils.state.changepin.ChangePinScreenState
import com.android.pinlibrary.viewmodel.PinViewModel

private var tempPinCode: String = ""

@Composable
fun ChangePinScreen(
    viewModel: PinViewModel,
    pinCodeStateManager: PinCodeStateManager
) {

    val context = LocalContext.current
    val pinCodeManager = PinCodeManager(context = context)
    val attemptCounter = AttemptCounter(context = context)
    var isError by remember { mutableStateOf(false) }
    var isInitScenario by remember { mutableStateOf(false) }
    var headerId by remember { mutableIntStateOf(R.string.pin_code_step_create) }
    val notificationText = stringResource(id = R.string.empty)
    var notification by remember { mutableStateOf(notificationText) }
    val forgotMessageId by remember { mutableIntStateOf(R.string.pin_code_forgot_text) }
    var isPinEntering by remember { mutableStateOf(false) }
    var isNewPinEntering by remember { mutableStateOf(false) }

    if (!isInitScenario) {
        viewModel.processIntent(ChangePinScreenIntent.InitialState)
        isInitScenario = true
    }

    when (val state = viewModel.changePinScreenState.observeAsState().value) {
        is ChangePinScreenState.InitialState -> {
            headerId = R.string.pin_code_step_unlock
            isPinEntering = false
            isNewPinEntering = false
        }

        is ChangePinScreenState.EnteringCurrentPin -> {
            if (pinCodeManager.isPinCodeCorrect(state.currentPin)) {
                headerId = R.string.pin_code_step_create
                isPinEntering = true
                attemptCounter.resetAttempts()
                notification = stringResource(id = R.string.empty)
            } else {
                if (!isError) {
                    Log.d("TAG", "PinCodeUi decrementAttempts")
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

        is ChangePinScreenState.EnteringNewPin -> {
            headerId = R.string.pin_code_step_enable_confirm
            isNewPinEntering = true
            tempPinCode = state.newPin
        }

        is ChangePinScreenState.ConfirmingNewPin -> {
            if (tempPinCode == state.newPin) {
                pinCodeManager.savePinCode(state.newPin)
                viewModel.processIntent(ChangePinScreenIntent.ChangePin)
            } else {
                notification = stringResource(id = R.string.pin_codes_do_not_match)
                viewModel.processIntent(ChangePinScreenIntent.InitialState)
            }
        }

        is ChangePinScreenState.PinChangedSuccess -> {
            pinCodeStateManager.setChangeSuccess(true)
        }

        is ChangePinScreenState.ErrorState -> {
            pinCodeStateManager.setChangeSuccess(false)
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

        if (!isPinEntering) {
            viewModel.processIntent(ChangePinScreenIntent.EnterCurrentPin(pinCode))
        } else {
            viewModel.processIntent(ChangePinScreenIntent.EnterNewPin(pinCode))
        }
        if (isNewPinEntering) {
            viewModel.processIntent(ChangePinScreenIntent.ConfirmNewPin(pinCode))
        }
        isError = false
    }
}
