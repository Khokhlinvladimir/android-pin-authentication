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
import com.android.pinlibrary.utils.preferences.AttemptCounter
import com.android.pinlibrary.utils.preferences.PinCodeManager
import com.android.pinlibrary.utils.state.PinCodeStateManager
import com.android.pinlibrary.utils.state.createpin.CreatePinScreenIntent
import com.android.pinlibrary.viewmodel.PinViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CreatePinScreen(
    viewModel: PinViewModel,
    pinCodeStateManager: PinCodeStateManager
) {
    val context = LocalContext.current
    val pinCodeManager = remember(context) { PinCodeManager(context) }
    val attemptCounter = remember(context) { AttemptCounter(context) }
    val coroutineScope = rememberCoroutineScope()
    var headerId by remember { mutableIntStateOf(R.string.pin_code_step_create) }
    var notification by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.processIntent(CreatePinScreenIntent.InitialState)
    }

    PinCodeContent(
        headerId = headerId,
        notification = notification,
        forgotMessageId = R.string.pin_code_forgot_text,
        enabled = !isProcessing && !isCompleted
    ) { pinValue ->
        if (isProcessing || isCompleted) return@PinCodeContent
        val pinCode = pinValue.joinToString("")
        val pendingPin = firstPin
        if (pendingPin == null) {
            firstPin = pinCode
            headerId = R.string.pin_code_step_enable_confirm
            notification = ""
            viewModel.processIntent(CreatePinScreenIntent.EnterPin(pinCode))
        } else if (pendingPin != pinCode) {
            firstPin = null
            headerId = R.string.pin_code_step_create
            notification = context.getString(R.string.pin_codes_do_not_match)
            viewModel.processIntent(CreatePinScreenIntent.InitialState)
        } else {
            isProcessing = true
            viewModel.processIntent(CreatePinScreenIntent.ConfirmPin(pinCode))
            coroutineScope.launch {
                val result = runCatching {
                    withContext(Dispatchers.Default) { pinCodeManager.savePinCode(pinCode) }
                }
                if (result.isSuccess) {
                    attemptCounter.resetAttempts()
                    isCompleted = true
                    viewModel.processIntent(CreatePinScreenIntent.CreatePin)
                    pinCodeStateManager.setCreationSuccess(true)
                } else {
                    firstPin = null
                    headerId = R.string.pin_code_step_create
                    notification = context.getString(R.string.pin_code_storage_error)
                    pinCodeStateManager.setCreationSuccess(false)
                }
                isProcessing = false
            }
        }
    }
}
