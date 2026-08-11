package com.android.pinlibrary

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.pinlibrary.api.PinAuthConfig
import com.android.pinlibrary.api.PinAuthAction
import com.android.pinlibrary.api.PinAuthController
import com.android.pinlibrary.api.PinAuthResult
import com.android.pinlibrary.api.PinAuthScenario
import com.android.pinlibrary.api.PinAuthStep
import com.android.pinlibrary.api.PinAuthUiState
import com.android.pinlibrary.ui.motion.PinAuthMotionSpec
import com.android.pinlibrary.ui.screens.PinAuthContent
import com.android.pinlibrary.ui.screens.PinAuthScreen
import com.android.pinlibrary.ui.screens.PinCodeScreen
import com.android.pinlibrary.utils.enums.PinCodeScenario
import com.android.pinlibrary.utils.preferences.AttemptCounter
import com.android.pinlibrary.utils.preferences.PinCodeManager
import com.android.pinlibrary.utils.state.PinCodeStateManager
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PinCodeScreenInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var application: Application
    private lateinit var stateManager: PinCodeStateManager

    @Before
    fun setUp() {
        application = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as Application
        stateManager = PinCodeStateManager.getInstance()
        stateManager.clearConfiguration(application)
        stateManager.setPinLength(4, application)
        stateManager.setMaxPinAttempts(3, application)
        stateManager.setBiometricEnabled(false, application)
        stateManager.autoLaunchBiometricEnabled(false, application)
    }

    @After
    fun tearDown() {
        stateManager.clearConfiguration(application)
    }

    @Test
    fun createThenValidatePin() {
        val creationSucceeded = AtomicBoolean(false)
        val validationSucceeded = AtomicBoolean(false)
        stateManager.onCreationSuccess(creationSucceeded::set)
        stateManager.onValidationSuccess(validationSucceeded::set)
        stateManager.setScenario(PinCodeScenario.CREATION)
        composeRule.setContent { PinCodeScreen() }

        enterPin("2468")
        enterPin("2468")
        composeRule.waitUntil(TEST_TIMEOUT_MILLIS) { creationSucceeded.get() }

        stateManager.setScenario(PinCodeScenario.VALIDATION)
        waitForScenario(PinCodeScenario.VALIDATION)
        enterPin("2468")
        composeRule.waitUntil(TEST_TIMEOUT_MILLIS) { validationSucceeded.get() }

        assertTrue(PinCodeManager(application).isPinCodeCorrect("2468"))
    }

    @Test
    fun changeThenDeletePin() {
        PinCodeManager(application).savePinCode("1234")
        val changeSucceeded = AtomicBoolean(false)
        val deletionSucceeded = AtomicBoolean(false)
        stateManager.onChangeSuccess(changeSucceeded::set)
        stateManager.onDeletionSuccess(deletionSucceeded::set)
        stateManager.setScenario(PinCodeScenario.CHANGE)
        composeRule.setContent { PinCodeScreen() }

        enterPin("1234")
        waitForText(application.getString(R.string.pin_code_step_create))
        enterPin("5678")
        enterPin("5678")
        composeRule.waitUntil(TEST_TIMEOUT_MILLIS) { changeSucceeded.get() }
        assertTrue(PinCodeManager(application).isPinCodeCorrect("5678"))

        stateManager.setScenario(PinCodeScenario.DELETION)
        waitForScenario(PinCodeScenario.DELETION)
        enterPin("5678")
        composeRule.waitUntil(TEST_TIMEOUT_MILLIS) { deletionSucceeded.get() }

        assertNull(PinCodeManager(application).loadPinCode())
    }

    @Test
    fun attemptLimitLocksValidation() {
        stateManager.setMaxPinAttempts(2, application)
        PinCodeManager(application).savePinCode("1234")
        val attemptsExhausted = AtomicBoolean(false)
        val validationSucceeded = AtomicBoolean(false)
        stateManager.onLoginAttemptsExpended { attemptsExhausted.set(true) }
        stateManager.onValidationSuccess(validationSucceeded::set)
        stateManager.setScenario(PinCodeScenario.VALIDATION)
        composeRule.setContent { PinCodeScreen() }

        enterPin("0000")
        composeRule.waitUntil(TEST_TIMEOUT_MILLIS) {
            AttemptCounter(application).getAttempts() == 1
        }
        enterPin("0000")
        composeRule.waitUntil(TEST_TIMEOUT_MILLIS) { attemptsExhausted.get() }

        assertEquals(0, AttemptCounter(application).getAttempts())
        assertFalse(validationSucceeded.get())
    }

    @Test
    fun controllerApiCreatesPinThroughComposeScreen() {
        val creationSucceeded = AtomicBoolean(false)
        val controller = PinAuthController.create(
            application,
            PinAuthConfig(
                pinLength = 4,
                maxAttempts = 3,
                biometricEnabled = false,
                autoLaunchBiometric = false
            )
        )
        composeRule.setContent {
            PinAuthScreen(
                controller = controller,
                scenario = PinAuthScenario.CREATION,
                onResult = { result ->
                    if (result == PinAuthResult.Created) creationSucceeded.set(true)
                }
            )
        }

        enterPin("1357")
        enterPin("1357")
        composeRule.waitUntil(TEST_TIMEOUT_MILLIS) { creationSucceeded.get() }

        assertTrue(PinCodeManager(application).isPinCodeCorrect("1357"))
        controller.close()
    }

    @Test
    fun forgotPinRequiresDialogConfirmation() {
        val resetRequested = AtomicBoolean(false)
        composeRule.setContent {
            PinAuthContent(
                state = PinAuthUiState(
                    scenario = PinAuthScenario.VALIDATION,
                    step = PinAuthStep.ENTER_CURRENT_PIN,
                    pinLength = 4,
                    remainingAttempts = 3
                ),
                onAction = { action ->
                    if (action == PinAuthAction.RequestReset) resetRequested.set(true)
                },
                motionSpec = PinAuthMotionSpec.None
            )
        }

        composeRule.onNodeWithText(application.getString(R.string.pin_code_forgot_text))
            .performClick()
        composeRule.onNodeWithText(application.getString(R.string.forgot_pin_instruction))
            .assertExists()
        assertFalse(resetRequested.get())

        composeRule.onNodeWithText(application.getString(R.string.ok_button)).performClick()
        composeRule.waitUntil(TEST_TIMEOUT_MILLIS) { resetRequested.get() }
    }

    private fun enterPin(pin: String) {
        pin.forEach { digit ->
            composeRule.onNodeWithText(digit.toString()).performClick()
        }
        composeRule.waitForIdle()
    }

    private fun waitForScenario(scenario: PinCodeScenario) {
        composeRule.waitUntil(TEST_TIMEOUT_MILLIS) {
            stateManager.currentScenario.value == scenario
        }
        composeRule.waitForIdle()
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(TEST_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private companion object {
        const val TEST_TIMEOUT_MILLIS = 30_000L
    }
}
