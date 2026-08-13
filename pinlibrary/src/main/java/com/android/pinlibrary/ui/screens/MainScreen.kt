package com.android.pinlibrary.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.pinlibrary.utils.enums.PinCodeScenario
import com.android.pinlibrary.ui.customization.PinAuthCustomization
import com.android.pinlibrary.utils.state.PinCodeStateManager
import com.android.pinlibrary.viewmodel.PinViewModel

@Composable
fun PinCodeScreen() = PinCodeScreen(PinAuthCustomization.Default)

@Composable
fun PinCodeScreen(customization: PinAuthCustomization) {

    val pinCodeStateManager = PinCodeStateManager.getInstance()
    val currentScenario = pinCodeStateManager.currentScenario.observeAsState()
    val pinViewModel = viewModel<PinViewModel>()

    AnimatedContent(
        targetState = currentScenario.value,
        transitionSpec = {
            (fadeIn(tween(240)) + scaleIn(initialScale = 0.975f)) togetherWith
                (fadeOut(tween(160)) + scaleOut(targetScale = 1.025f))
        },
        label = "legacy-pin-scenario"
    ) { scenario ->
        when (scenario) {
            PinCodeScenario.CREATION -> CreatePinScreen(
                pinViewModel, pinCodeStateManager, customization
            )
            PinCodeScenario.CHANGE -> ChangePinScreen(
                pinViewModel, pinCodeStateManager, customization
            )
            PinCodeScenario.DELETION -> DeletePinScreen(
                pinViewModel, pinCodeStateManager, customization
            )
            PinCodeScenario.VALIDATION -> ValidationPinScreen(
                pinViewModel,
                pinCodeStateManager,
                PinCodeScenario.VALIDATION,
                customization
            )

            else -> Unit
        }
    }
}
