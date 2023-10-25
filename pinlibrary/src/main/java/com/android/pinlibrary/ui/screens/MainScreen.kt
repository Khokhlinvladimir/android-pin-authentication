package com.android.pinlibrary.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.pinlibrary.utils.enums.PinCodeScenario
import com.android.pinlibrary.utils.state.PinCodeStateManager
import com.android.pinlibrary.viewmodel.PinViewModel

@Composable
fun PinCodeScreen() {

    val pinCodeStateManager = PinCodeStateManager.getInstance()
    val currentScenario = pinCodeStateManager.currentScenario.observeAsState()
    val pinViewModel = viewModel<PinViewModel>()

    when (currentScenario.value) {
        PinCodeScenario.CREATION -> CreatePinScreen(pinViewModel, pinCodeStateManager)
        PinCodeScenario.CHANGE -> ChangePinScreen(pinViewModel, pinCodeStateManager)
        PinCodeScenario.DELETION -> DeletePinScreen(pinViewModel, pinCodeStateManager)
        PinCodeScenario.VALIDATION -> ValidationPinScreen(
            pinViewModel,
            pinCodeStateManager,
            PinCodeScenario.VALIDATION
        )

        else -> {}
    }
}
