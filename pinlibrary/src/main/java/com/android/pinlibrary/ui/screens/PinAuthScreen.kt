package com.android.pinlibrary.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.pinlibrary.R
import com.android.pinlibrary.api.PinAuthAction
import com.android.pinlibrary.api.PinAuthController
import com.android.pinlibrary.api.PinAuthMessage
import com.android.pinlibrary.api.PinAuthResult
import com.android.pinlibrary.api.PinAuthScenario
import com.android.pinlibrary.api.PinAuthStep
import com.android.pinlibrary.api.PinAuthUiState
import com.android.pinlibrary.ui.components.Keyboard
import com.android.pinlibrary.ui.components.PinCodeScreenHeader
import com.android.pinlibrary.ui.components.PinCodeScreenNotification
import com.android.pinlibrary.ui.systemdesign.indicator.RoundedBoxesRow
import com.android.pinlibrary.utils.enums.PinCodeScenario
import com.android.pinlibrary.utils.keyboard.KeyboardButtonEnum
import kotlinx.coroutines.launch

/**
 * State-driven Compose entry point. The caller owns [controller] and handles terminal results.
 */
@Composable
fun PinAuthScreen(
    controller: PinAuthController,
    scenario: PinAuthScenario,
    onResult: (PinAuthResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(controller) {
        controller.results.collect(onResult)
    }
    LaunchedEffect(controller, scenario) {
        controller.start(scenario)
    }

    PinAuthContent(
        state = state,
        onAction = { action -> scope.launch { controller.dispatch(action) } },
        modifier = modifier
    )
}

/** Stateless UI surface suitable for previews, screenshot tests, and custom hosts. */
@Composable
fun PinAuthContent(
    state: PinAuthUiState,
    onAction: (PinAuthAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PinCodeScreenHeader(text = stringResource(headerResource(state)))
        PinCodeScreenNotification(text = messageText(state.message))
        RoundedBoxesRow(startQuantity = state.pinLength, quantity = state.enteredDigits)
        Keyboard(
            pinCodeScenario = PinCodeScenario.STUB,
            enabled = state.isInputEnabled,
            onButtonClick = { button ->
                when (button) {
                    KeyboardButtonEnum.BUTTON_CLEAR -> onAction(PinAuthAction.Backspace)
                    KeyboardButtonEnum.BUTTON_FINGERPRINT -> Unit
                    else -> onAction(PinAuthAction.Digit(button.buttonValue))
                }
            }
        )
        if (state.scenario != null && state.scenario != PinAuthScenario.CREATION) {
            Text(
                text = stringResource(R.string.pin_code_forgot_text),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clickable(enabled = !state.isProcessing) {
                        onAction(PinAuthAction.RequestReset)
                    }
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun messageText(message: PinAuthMessage?): String = when (message) {
    is PinAuthMessage.IncorrectPin -> stringResource(
        R.string.pin_code_attempts,
        message.remainingAttempts
    )
    PinAuthMessage.PinMismatch -> stringResource(R.string.pin_codes_do_not_match)
    PinAuthMessage.PinAlreadyConfigured -> stringResource(R.string.pin_code_already_configured)
    PinAuthMessage.PinNotConfigured -> stringResource(R.string.pin_code_not_configured)
    PinAuthMessage.StorageError -> stringResource(R.string.pin_code_storage_error)
    null -> ""
}

private fun headerResource(state: PinAuthUiState): Int = when (state.step) {
    PinAuthStep.ENTER_PIN,
    PinAuthStep.ENTER_NEW_PIN -> R.string.pin_code_step_create
    PinAuthStep.CONFIRM_PIN,
    PinAuthStep.CONFIRM_NEW_PIN -> R.string.pin_code_step_enable_confirm
    PinAuthStep.ENTER_CURRENT_PIN -> when (state.scenario) {
        PinAuthScenario.DELETION -> R.string.pin_code_step_disable
        PinAuthScenario.CHANGE -> R.string.pin_code_step_change
        else -> R.string.pin_code_step_unlock
    }
    PinAuthStep.COMPLETED -> R.string.password_successfully_saved
}
