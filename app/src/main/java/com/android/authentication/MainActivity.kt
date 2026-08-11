package com.android.authentication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.android.authentication.ui.screens.PinCodeStartScreen
import com.android.authentication.ui.theme.PinAuthenticationTheme
import com.android.pinlibrary.api.PinAuthConfig
import com.android.pinlibrary.api.PinAuthController
import com.android.pinlibrary.api.PinAuthResult
import com.android.pinlibrary.api.PinAuthScenario
import com.android.pinlibrary.ui.motion.PinAuthMotionSpec
import com.android.pinlibrary.ui.screens.PinAuthScreen
import com.android.pinlibrary.ui.screens.PinCodeScreen
import com.android.pinlibrary.utils.enums.PinCodeScenario
import com.android.pinlibrary.utils.preferences.AttemptCounter
import com.android.pinlibrary.utils.preferences.PinCodeManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PinAuthenticationTheme {
                MotionDemo()
            }
        }
    }
}

@Composable
private fun MotionDemo() {
    val context = LocalContext.current.applicationContext
    val pinCodeManager = remember(context) { PinCodeManager(context) }
    val attemptCounter = remember(context) { AttemptCounter(context) }
    val controller = remember(context) {
        PinAuthController.create(
            context = context,
            config = PinAuthConfig(
                pinLength = 4,
                maxAttempts = 4,
                biometricEnabled = false,
                autoLaunchBiometric = false
            )
        )
    }
    var scenario by remember { mutableStateOf<PinAuthScenario?>(null) }
    var isPinCodeCreated by remember {
        mutableStateOf(pinCodeManager.loadPinCode() != null)
    }

    DisposableEffect(controller) {
        onDispose { controller.close() }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = scenario,
            transitionSpec = {
                if (targetState != null) {
                    (slideInHorizontally(tween(340)) { it / 3 } + fadeIn()) togetherWith
                        (slideOutHorizontally(tween(220)) { -it / 4 } + fadeOut())
                } else {
                    (slideInHorizontally(tween(340)) { -it / 3 } + fadeIn()) togetherWith
                        (slideOutHorizontally(tween(220)) { it / 4 } + fadeOut())
                }
            },
            label = "demo-auth-navigation"
        ) { currentScenario ->
            if (currentScenario == null) {
                PinCodeStartScreen(isPinCodeCreated) { legacyScenario ->
                    scenario = legacyScenario.toControllerScenario()
                }
            } else {
                PinAuthScreen(
                    controller = controller,
                    scenario = currentScenario,
                    motionSpec = PinAuthMotionSpec.Premium,
                    onResult = { result ->
                        when (result) {
                            PinAuthResult.Created -> {
                                isPinCodeCreated = true
                                scenario = null
                            }
                            PinAuthResult.Validated,
                            PinAuthResult.Changed -> scenario = null
                            PinAuthResult.Deleted -> {
                                isPinCodeCreated = false
                                scenario = null
                            }
                            PinAuthResult.ResetRequested -> {
                                pinCodeManager.clearPinCode()
                                attemptCounter.resetAttempts()
                                isPinCodeCreated = false
                                scenario = null
                            }
                            PinAuthResult.PinAlreadyConfigured -> {
                                isPinCodeCreated = true
                                scenario = null
                            }
                            PinAuthResult.PinNotConfigured -> {
                                isPinCodeCreated = false
                                scenario = null
                            }
                            PinAuthResult.AttemptsExhausted,
                            is PinAuthResult.Error -> Unit
                        }
                    }
                )
            }
        }
    }
}

private fun PinCodeScenario.toControllerScenario(): PinAuthScenario = when (this) {
    PinCodeScenario.CREATION -> PinAuthScenario.CREATION
    PinCodeScenario.VALIDATION -> PinAuthScenario.VALIDATION
    PinCodeScenario.CHANGE -> PinAuthScenario.CHANGE
    PinCodeScenario.DELETION -> PinAuthScenario.DELETION
    PinCodeScenario.STUB -> error("The demo cannot start a stub PIN scenario")
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PinAuthenticationTheme {
        PinCodeScreen()
    }
}
