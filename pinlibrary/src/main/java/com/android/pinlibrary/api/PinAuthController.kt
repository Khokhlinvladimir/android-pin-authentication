package com.android.pinlibrary.api

import android.content.Context
import com.android.pinlibrary.utils.preferences.AttemptCounter
import com.android.pinlibrary.utils.preferences.PinCodeManager
import com.android.pinlibrary.utils.preferences.SettingsManager
import java.util.Arrays
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class PinAuthController(
    val config: PinAuthConfig,
    private val credentialStore: PinCredentialStore,
    private val attemptStore: PinAttemptStore
) {
    private val mutex = Mutex()
    private val enteredPin = mutableListOf<Char>()
    private var pendingPin: CharArray? = null
    private val _state = MutableStateFlow(
        PinAuthUiState(
            pinLength = config.pinLength,
            remainingAttempts = attemptStore.remainingAttempts()
        )
    )
    private val _results = MutableSharedFlow<PinAuthResult>(extraBufferCapacity = 8)

    val state: StateFlow<PinAuthUiState> = _state.asStateFlow()
    val results: SharedFlow<PinAuthResult> = _results.asSharedFlow()

    suspend fun start(scenario: PinAuthScenario): PinAuthResult? = mutex.withLock {
        clearEnteredPin()
        clearPendingPin()
        val hasPin = credentialStore.hasPin()
        val unavailableResult = when {
            scenario == PinAuthScenario.CREATION && hasPin -> PinAuthResult.PinAlreadyConfigured
            scenario != PinAuthScenario.CREATION && !hasPin -> PinAuthResult.PinNotConfigured
            else -> null
        }
        if (unavailableResult != null) {
            _state.value = PinAuthUiState(
                scenario = scenario,
                step = initialStep(scenario),
                pinLength = config.pinLength,
                remainingAttempts = attemptStore.remainingAttempts(),
                isLocked = true,
                message = if (unavailableResult == PinAuthResult.PinAlreadyConfigured) {
                    PinAuthMessage.PinAlreadyConfigured
                } else {
                    PinAuthMessage.PinNotConfigured
                }
            )
            publish(unavailableResult)
            return@withLock unavailableResult
        }

        val remainingAttempts = attemptStore.remainingAttempts()
        val isLocked = scenario != PinAuthScenario.CREATION && remainingAttempts <= 0
        _state.value = PinAuthUiState(
            scenario = scenario,
            step = initialStep(scenario),
            pinLength = config.pinLength,
            remainingAttempts = remainingAttempts,
            isLocked = isLocked,
            message = if (isLocked) PinAuthMessage.IncorrectPin(0) else null
        )
        if (isLocked) {
            PinAuthResult.AttemptsExhausted.also {
                publish(it)
                return@withLock it
            }
        }
        null
    }

    suspend fun dispatch(action: PinAuthAction): PinAuthResult? = mutex.withLock {
        when (action) {
            is PinAuthAction.Digit -> handleDigit(action.value)
            PinAuthAction.Backspace -> {
                handleBackspace()
                null
            }
            PinAuthAction.RequestReset -> PinAuthResult.ResetRequested.also { publish(it) }
        }
    }

    fun close() {
        clearEnteredPin()
        clearPendingPin()
    }

    private suspend fun handleDigit(value: Int): PinAuthResult? {
        require(value in 0..9) { "PIN digit must be between 0 and 9" }
        if (!_state.value.isInputEnabled || enteredPin.size >= config.pinLength) return null

        enteredPin += ('0'.code + value).toChar()
        _state.value = _state.value.copy(enteredDigits = enteredPin.size, message = null)
        if (enteredPin.size != config.pinLength) return null

        val submittedPin = enteredPin.toCharArray()
        clearEnteredPin()
        _state.value = _state.value.copy(enteredDigits = 0, isProcessing = true)
        return try {
            handleCompletedPin(submittedPin)
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            _state.value = _state.value.copy(
                enteredDigits = 0,
                isProcessing = false,
                message = PinAuthMessage.StorageError
            )
            PinAuthResult.Error(error).also { publish(it) }
        } finally {
            Arrays.fill(submittedPin, '\u0000')
        }
    }

    private suspend fun handleCompletedPin(pin: CharArray): PinAuthResult? {
        return when (_state.value.step) {
            PinAuthStep.ENTER_PIN -> beginCreation(pin)
            PinAuthStep.CONFIRM_PIN -> confirmCreation(pin)
            PinAuthStep.ENTER_CURRENT_PIN -> verifyCurrentPin(pin)
            PinAuthStep.ENTER_NEW_PIN -> beginPinChange(pin)
            PinAuthStep.CONFIRM_NEW_PIN -> confirmPinChange(pin)
            PinAuthStep.COMPLETED -> null
        }
    }

    private fun beginCreation(pin: CharArray): PinAuthResult? {
        replacePendingPin(pin)
        _state.value = _state.value.copy(
            step = PinAuthStep.CONFIRM_PIN,
            isProcessing = false,
            message = null
        )
        return null
    }

    private suspend fun confirmCreation(pin: CharArray): PinAuthResult? {
        if (pendingPin?.contentEquals(pin) != true) {
            clearPendingPin()
            _state.value = _state.value.copy(
                step = PinAuthStep.ENTER_PIN,
                isProcessing = false,
                message = PinAuthMessage.PinMismatch
            )
            return null
        }
        credentialStore.save(pin)
        clearPendingPin()
        attemptStore.reset()
        return complete(PinAuthResult.Created)
    }

    private suspend fun verifyCurrentPin(pin: CharArray): PinAuthResult? {
        if (!credentialStore.verify(pin)) return incorrectPin()

        attemptStore.reset()
        return when (_state.value.scenario) {
            PinAuthScenario.VALIDATION -> complete(PinAuthResult.Validated)
            PinAuthScenario.CHANGE -> {
                _state.value = _state.value.copy(
                    step = PinAuthStep.ENTER_NEW_PIN,
                    remainingAttempts = attemptStore.remainingAttempts(),
                    isProcessing = false,
                    message = null
                )
                null
            }
            PinAuthScenario.DELETION -> {
                credentialStore.delete()
                complete(PinAuthResult.Deleted)
            }
            else -> null
        }
    }

    private fun beginPinChange(pin: CharArray): PinAuthResult? {
        replacePendingPin(pin)
        _state.value = _state.value.copy(
            step = PinAuthStep.CONFIRM_NEW_PIN,
            isProcessing = false,
            message = null
        )
        return null
    }

    private suspend fun confirmPinChange(pin: CharArray): PinAuthResult? {
        if (pendingPin?.contentEquals(pin) != true) {
            clearPendingPin()
            _state.value = _state.value.copy(
                step = PinAuthStep.ENTER_NEW_PIN,
                isProcessing = false,
                message = PinAuthMessage.PinMismatch
            )
            return null
        }
        credentialStore.save(pin)
        clearPendingPin()
        return complete(PinAuthResult.Changed)
    }

    private suspend fun incorrectPin(): PinAuthResult? {
        val remainingAttempts = attemptStore.decrement()
        val isLocked = remainingAttempts <= 0
        _state.value = _state.value.copy(
            remainingAttempts = remainingAttempts,
            isProcessing = false,
            isLocked = isLocked,
            message = PinAuthMessage.IncorrectPin(remainingAttempts)
        )
        return if (isLocked) {
            PinAuthResult.AttemptsExhausted.also { publish(it) }
        } else {
            null
        }
    }

    private suspend fun complete(result: PinAuthResult): PinAuthResult {
        _state.value = _state.value.copy(
            step = PinAuthStep.COMPLETED,
            enteredDigits = 0,
            remainingAttempts = attemptStore.remainingAttempts(),
            isProcessing = false,
            isLocked = true,
            message = null
        )
        publish(result)
        return result
    }

    private fun handleBackspace() {
        if (!_state.value.isInputEnabled || enteredPin.isEmpty()) return
        enteredPin.removeAt(enteredPin.lastIndex)
        _state.value = _state.value.copy(enteredDigits = enteredPin.size)
    }

    private fun replacePendingPin(pin: CharArray) {
        clearPendingPin()
        pendingPin = pin.copyOf()
    }

    private fun clearEnteredPin() {
        enteredPin.indices.forEach { enteredPin[it] = '\u0000' }
        enteredPin.clear()
    }

    private fun clearPendingPin() {
        pendingPin?.let { Arrays.fill(it, '\u0000') }
        pendingPin = null
    }

    private suspend fun publish(result: PinAuthResult) {
        _results.emit(result)
    }

    private fun initialStep(scenario: PinAuthScenario): PinAuthStep = when (scenario) {
        PinAuthScenario.CREATION -> PinAuthStep.ENTER_PIN
        PinAuthScenario.VALIDATION,
        PinAuthScenario.CHANGE,
        PinAuthScenario.DELETION -> PinAuthStep.ENTER_CURRENT_PIN
    }

    companion object {
        @JvmStatic
        fun create(context: Context): PinAuthController {
            val appContext = context.applicationContext
            val settings = SettingsManager(appContext)
            return create(
                context = appContext,
                config = PinAuthConfig(
                    pinLength = settings.getPinLength(),
                    maxAttempts = settings.getMaxPinAttempts(),
                    biometricEnabled = settings.isBiometricEnabled(),
                    autoLaunchBiometric = settings.isAutoLaunchBiometricEnabled()
                )
            )
        }

        @JvmStatic
        fun create(context: Context, config: PinAuthConfig): PinAuthController {
            val appContext = context.applicationContext
            val settings = SettingsManager(appContext)
            val pinCodeManager = PinCodeManager(appContext)
            require(pinCodeManager.loadPinCode() == null || settings.getPinLength() == config.pinLength) {
                "Clear the saved PIN before changing its length"
            }
            val maxAttemptsChanged = settings.getMaxPinAttempts() != config.maxAttempts
            settings.setPinLength(config.pinLength)
            settings.setMaxPinAttempts(config.maxAttempts)
            settings.setBiometricEnabled(config.biometricEnabled)
            settings.setAutoLaunchBiometricEnabled(config.autoLaunchBiometric)
            val attemptCounter = AttemptCounter(appContext)
            if (maxAttemptsChanged) attemptCounter.resetAttempts()

            return PinAuthController(
                config = config,
                credentialStore = AndroidPinCredentialStore(pinCodeManager),
                attemptStore = AndroidPinAttemptStore(attemptCounter)
            )
        }
    }
}

private class AndroidPinCredentialStore(
    private val manager: PinCodeManager
) : PinCredentialStore {
    override suspend fun hasPin(): Boolean = manager.loadPinCode() != null

    override suspend fun save(pin: CharArray) {
        withContext(Dispatchers.Default) { manager.savePinCode(pin.concatToString()) }
    }

    override suspend fun verify(pin: CharArray): Boolean = withContext(Dispatchers.Default) {
        manager.isPinCodeCorrect(pin.concatToString())
    }

    override suspend fun delete() {
        manager.clearPinCode()
    }
}

private class AndroidPinAttemptStore(
    private val counter: AttemptCounter
) : PinAttemptStore {
    override fun remainingAttempts(): Int = counter.getAttempts()

    override fun decrement(): Int {
        counter.decrementAttempts()
        return counter.getAttempts()
    }

    override fun reset() {
        counter.resetAttempts()
    }
}
