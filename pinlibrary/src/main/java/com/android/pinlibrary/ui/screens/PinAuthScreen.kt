package com.android.pinlibrary.ui.screens

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
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
import com.android.pinlibrary.ui.motion.PinAuthMotionSpec
import com.android.pinlibrary.ui.systemdesign.indicator.PinIndicatorFeedback
import com.android.pinlibrary.ui.systemdesign.indicator.RoundedBoxesRow
import com.android.pinlibrary.utils.enums.PinCodeScenario
import com.android.pinlibrary.utils.keyboard.KeyboardButtonEnum
import kotlinx.coroutines.delay
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
) = PinAuthScreen(
    controller = controller,
    scenario = scenario,
    onResult = onResult,
    motionSpec = PinAuthMotionSpec.Premium,
    modifier = modifier
)

/** State-driven entry point with an explicit, reusable motion configuration. */
@Composable
fun PinAuthScreen(
    controller: PinAuthController,
    scenario: PinAuthScenario,
    onResult: (PinAuthResult) -> Unit,
    motionSpec: PinAuthMotionSpec,
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)

    LaunchedEffect(controller, motionSpec) {
        controller.results.collect { result ->
            if (motionSpec.enabled && result.isSuccessfulCompletion()) {
                delay(motionSpec.completionHoldMillis)
            }
            currentOnResult(result)
        }
    }
    LaunchedEffect(controller, scenario) {
        controller.start(scenario)
    }

    PinAuthContent(
        state = state,
        onAction = { action -> scope.launch { controller.dispatch(action) } },
        motionSpec = motionSpec,
        modifier = modifier
    )
}

/** Stateless UI surface suitable for previews, screenshot tests, and custom hosts. */
@Composable
fun PinAuthContent(
    state: PinAuthUiState,
    onAction: (PinAuthAction) -> Unit,
    modifier: Modifier = Modifier
) = PinAuthContent(
    state = state,
    onAction = onAction,
    motionSpec = PinAuthMotionSpec.Premium,
    modifier = modifier
)

/** Stateless UI surface with explicit motion and haptic behavior. */
@Composable
fun PinAuthContent(
    state: PinAuthUiState,
    onAction: (PinAuthAction) -> Unit,
    motionSpec: PinAuthMotionSpec,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val feedback = state.indicatorFeedback()
    val keyboardAlpha by animateFloatAsState(
        targetValue = when {
            state.step == PinAuthStep.COMPLETED -> 0.18f
            state.isProcessing -> 0.48f
            state.isLocked -> 0.38f
            else -> 1f
        },
        animationSpec = tween(if (motionSpec.enabled) motionSpec.stepDurationMillis else 0),
        label = "pin-keyboard-alpha"
    )
    val keyboardScale by animateFloatAsState(
        targetValue = if (state.isInputEnabled) 1f else 0.96f,
        animationSpec = if (motionSpec.enabled) {
            spring(dampingRatio = 0.72f, stiffness = 420f)
        } else {
            tween(0)
        },
        label = "pin-keyboard-scale"
    )

    LaunchedEffect(state.message, state.remainingAttempts, motionSpec.hapticsEnabled) {
        if (motionSpec.hapticsEnabled && state.message != null) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
    LaunchedEffect(state.step, motionSpec.hapticsEnabled) {
        if (motionSpec.hapticsEnabled && state.step == PinAuthStep.COMPLETED) {
            val haptic = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedbackConstants.CONFIRM
            } else {
                HapticFeedbackConstants.KEYBOARD_TAP
            }
            view.performHapticFeedback(haptic)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (motionSpec.ambientBackgroundEnabled) {
            PremiumSecurityBackground(motionSpec)
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    val direction = if (targetState.ordinal >= initialState.ordinal) 1 else -1
                    if (motionSpec.enabled) {
                        (slideInHorizontally(
                            animationSpec = tween(motionSpec.stepDurationMillis)
                        ) { direction * it / 3 } + fadeIn()) togetherWith
                            (slideOutHorizontally(
                                animationSpec = tween(motionSpec.stepDurationMillis)
                            ) { -direction * it / 3 } + fadeOut())
                    } else {
                        fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                    }
                },
                label = "pin-step-title"
            ) { step ->
                PinCodeScreenHeader(
                    text = stringResource(headerResource(step, state.scenario))
                )
            }

            Box(modifier = Modifier.height(64.dp), contentAlignment = Alignment.Center) {
                AnimatedContent(
                    targetState = state.message,
                    transitionSpec = {
                        if (motionSpec.enabled) {
                            (fadeIn(tween(160)) + slideInVertically { it / 3 }) togetherWith
                                (fadeOut(tween(110)) + slideOutVertically { -it / 3 })
                        } else {
                            fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                        }
                    },
                    label = "pin-notification"
                ) { message ->
                    PinCodeScreenNotification(text = messageText(message))
                }
            }

            RoundedBoxesRow(
                startQuantity = state.pinLength,
                quantity = state.enteredDigits,
                feedback = feedback,
                feedbackToken = state.message to state.remainingAttempts,
                isProcessing = state.isProcessing,
                motionSpec = motionSpec
            )

            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = keyboardAlpha
                    scaleX = keyboardScale
                    scaleY = keyboardScale
                }
            ) {
                Keyboard(
                    pinCodeScenario = PinCodeScenario.STUB,
                    enabled = state.isInputEnabled,
                    motionSpec = motionSpec,
                    onButtonClick = { button ->
                        when (button) {
                            KeyboardButtonEnum.BUTTON_CLEAR -> onAction(PinAuthAction.Backspace)
                            KeyboardButtonEnum.BUTTON_FINGERPRINT -> Unit
                            else -> onAction(PinAuthAction.Digit(button.buttonValue))
                        }
                    }
                )
            }

            AnimatedVisibility(
                visible = state.scenario != null &&
                    state.scenario != PinAuthScenario.CREATION &&
                    state.step != PinAuthStep.COMPLETED,
                enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.94f),
                exit = fadeOut(tween(120)) + scaleOut(targetScale = 0.94f)
            ) {
                Text(
                    text = stringResource(R.string.pin_code_forgot_text),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable(enabled = !state.isProcessing) {
                            onAction(PinAuthAction.RequestReset)
                        }
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PremiumSecurityBackground(motionSpec: PinAuthMotionSpec) {
    val phase = if (motionSpec.enabled) {
        val transition = rememberInfiniteTransition(label = "pin-ambient")
        val value by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(8_000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pin-ambient-phase"
        )
        value
    } else {
        0.5f
    }
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.105f), Color.Transparent),
                center = Offset(
                    x = size.width * (0.18f + 0.58f * phase),
                    y = size.height * (0.12f + 0.28f * (1f - phase))
                ),
                radius = size.maxDimension * 0.62f
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(secondary.copy(alpha = 0.07f), Color.Transparent),
                center = Offset(
                    x = size.width * (0.82f - 0.45f * phase),
                    y = size.height * (0.82f - 0.18f * phase)
                ),
                radius = size.maxDimension * 0.5f
            )
        )
    }
}

private fun PinAuthUiState.indicatorFeedback(): PinIndicatorFeedback = when {
    step == PinAuthStep.COMPLETED -> PinIndicatorFeedback.SUCCESS
    message is PinAuthMessage.IncorrectPin || message == PinAuthMessage.PinMismatch -> {
        PinIndicatorFeedback.ERROR
    }
    isLocked -> PinIndicatorFeedback.LOCKED
    else -> PinIndicatorFeedback.NORMAL
}

private fun PinAuthResult.isSuccessfulCompletion(): Boolean = when (this) {
    PinAuthResult.Created,
    PinAuthResult.Validated,
    PinAuthResult.Changed,
    PinAuthResult.Deleted -> true
    else -> false
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

private fun headerResource(step: PinAuthStep, scenario: PinAuthScenario?): Int = when (step) {
    PinAuthStep.ENTER_PIN,
    PinAuthStep.ENTER_NEW_PIN -> R.string.pin_code_step_create
    PinAuthStep.CONFIRM_PIN,
    PinAuthStep.CONFIRM_NEW_PIN -> R.string.pin_code_step_enable_confirm
    PinAuthStep.ENTER_CURRENT_PIN -> when (scenario) {
        PinAuthScenario.DELETION -> R.string.pin_code_step_disable
        PinAuthScenario.CHANGE -> R.string.pin_code_step_change
        else -> R.string.pin_code_step_unlock
    }
    PinAuthStep.COMPLETED -> when (scenario) {
        PinAuthScenario.CREATION -> R.string.pin_code_success_created
        PinAuthScenario.VALIDATION -> R.string.pin_code_success_validated
        PinAuthScenario.CHANGE -> R.string.pin_code_success_changed
        PinAuthScenario.DELETION -> R.string.pin_code_success_deleted
        null -> R.string.password_successfully_saved
    }
}
