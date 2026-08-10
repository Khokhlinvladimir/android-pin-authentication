package com.android.pinlibrary.api

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PinAuthControllerTest {

    @Test
    fun createsAndValidatesPin() = runBlocking {
        val credentials = FakeCredentialStore()
        val attempts = FakeAttemptStore(maxAttempts = 3)
        val controller = controller(credentials, attempts)

        assertNull(controller.start(PinAuthScenario.CREATION))
        assertNull(submit(controller, "2468"))
        assertEquals(PinAuthResult.Created, submit(controller, "2468"))
        assertTrue(credentials.matches("2468"))

        assertNull(controller.start(PinAuthScenario.VALIDATION))
        assertEquals(PinAuthResult.Validated, submit(controller, "2468"))
        assertEquals(3, attempts.remainingAttempts())
    }

    @Test
    fun mismatchedConfirmationRestartsCreation() = runBlocking {
        val controller = controller(FakeCredentialStore(), FakeAttemptStore(3))
        controller.start(PinAuthScenario.CREATION)

        submit(controller, "1234")
        assertNull(submit(controller, "4321"))

        assertEquals(PinAuthStep.ENTER_PIN, controller.state.value.step)
        assertEquals(PinAuthMessage.PinMismatch, controller.state.value.message)
        assertTrue(controller.state.value.isInputEnabled)
    }

    @Test
    fun locksAfterConfiguredNumberOfWrongAttempts() = runBlocking {
        val credentials = FakeCredentialStore("1234")
        val attempts = FakeAttemptStore(maxAttempts = 2)
        val controller = controller(credentials, attempts)
        controller.start(PinAuthScenario.VALIDATION)

        assertNull(submit(controller, "0000"))
        assertEquals(PinAuthResult.AttemptsExhausted, submit(controller, "0000"))
        assertTrue(controller.state.value.isLocked)
        assertFalse(controller.state.value.isInputEnabled)

        assertEquals(PinAuthResult.AttemptsExhausted, controller.start(PinAuthScenario.VALIDATION))
    }

    @Test
    fun changesThenDeletesPin() = runBlocking {
        val credentials = FakeCredentialStore("1234")
        val controller = controller(credentials, FakeAttemptStore(3))

        controller.start(PinAuthScenario.CHANGE)
        assertNull(submit(controller, "1234"))
        assertEquals(PinAuthStep.ENTER_NEW_PIN, controller.state.value.step)
        submit(controller, "5678")
        assertEquals(PinAuthResult.Changed, submit(controller, "5678"))
        assertTrue(credentials.matches("5678"))

        controller.start(PinAuthScenario.DELETION)
        assertEquals(PinAuthResult.Deleted, submit(controller, "5678"))
        assertFalse(credentials.hasPin())
    }

    @Test
    fun rejectsScenariosThatDoNotMatchStoredState() = runBlocking {
        val credentials = FakeCredentialStore()
        val controller = controller(credentials, FakeAttemptStore(3))

        assertEquals(PinAuthResult.PinNotConfigured, controller.start(PinAuthScenario.VALIDATION))
        assertTrue(controller.state.value.isLocked)

        credentials.save("1234".toCharArray())
        assertEquals(PinAuthResult.PinAlreadyConfigured, controller.start(PinAuthScenario.CREATION))
        assertTrue(controller.state.value.isLocked)
    }

    @Test
    fun backspaceUpdatesOnlyEnteredDigitCount() = runBlocking {
        val controller = controller(FakeCredentialStore(), FakeAttemptStore(3))
        controller.start(PinAuthScenario.CREATION)

        controller.dispatch(PinAuthAction.Digit(1))
        controller.dispatch(PinAuthAction.Digit(2))
        controller.dispatch(PinAuthAction.Backspace)

        assertEquals(1, controller.state.value.enteredDigits)
        assertEquals(PinAuthStep.ENTER_PIN, controller.state.value.step)
    }

    private fun controller(
        credentials: FakeCredentialStore,
        attempts: FakeAttemptStore
    ) = PinAuthController(
        config = PinAuthConfig(pinLength = 4, maxAttempts = attempts.maxAttempts),
        credentialStore = credentials,
        attemptStore = attempts
    )

    private suspend fun submit(controller: PinAuthController, pin: String): PinAuthResult? {
        var result: PinAuthResult? = null
        pin.forEach { digit ->
            result = controller.dispatch(PinAuthAction.Digit(digit.digitToInt())) ?: result
        }
        return result
    }

    private class FakeCredentialStore(initialPin: String? = null) : PinCredentialStore {
        private var pin: CharArray? = initialPin?.toCharArray()

        override suspend fun hasPin(): Boolean = pin != null

        override suspend fun save(pin: CharArray) {
            this.pin?.fill('\u0000')
            this.pin = pin.copyOf()
        }

        override suspend fun verify(pin: CharArray): Boolean = this.pin?.contentEquals(pin) == true

        override suspend fun delete() {
            pin?.fill('\u0000')
            pin = null
        }

        fun matches(value: String): Boolean = pin?.contentEquals(value.toCharArray()) == true
    }

    private class FakeAttemptStore(val maxAttempts: Int) : PinAttemptStore {
        private var attempts = maxAttempts

        override fun remainingAttempts(): Int = attempts

        override fun decrement(): Int {
            attempts = (attempts - 1).coerceAtLeast(0)
            return attempts
        }

        override fun reset() {
            attempts = maxAttempts
        }
    }
}
