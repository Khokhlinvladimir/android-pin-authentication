package com.android.pinlibrary.api

data class PinAuthConfig(
    val pinLength: Int = 4,
    val maxAttempts: Int = 5,
    val biometricEnabled: Boolean = true,
    val autoLaunchBiometric: Boolean = true
) {
    init {
        require(pinLength in 4..12) { "PIN length must be between 4 and 12" }
        require(maxAttempts in 1..100) { "Maximum PIN attempts must be between 1 and 100" }
    }
}

enum class PinAuthScenario {
    CREATION,
    VALIDATION,
    CHANGE,
    DELETION
}

enum class PinAuthStep {
    ENTER_PIN,
    CONFIRM_PIN,
    ENTER_CURRENT_PIN,
    ENTER_NEW_PIN,
    CONFIRM_NEW_PIN,
    COMPLETED
}

sealed interface PinAuthMessage {
    data class IncorrectPin(val remainingAttempts: Int) : PinAuthMessage
    data object PinMismatch : PinAuthMessage
    data object PinAlreadyConfigured : PinAuthMessage
    data object PinNotConfigured : PinAuthMessage
    data object StorageError : PinAuthMessage
}

data class PinAuthUiState(
    val scenario: PinAuthScenario? = null,
    val step: PinAuthStep = PinAuthStep.ENTER_PIN,
    val pinLength: Int = 4,
    val enteredDigits: Int = 0,
    val remainingAttempts: Int = 0,
    val isProcessing: Boolean = false,
    val isLocked: Boolean = false,
    val message: PinAuthMessage? = null
) {
    val isInputEnabled: Boolean
        get() = scenario != null && !isProcessing && !isLocked && step != PinAuthStep.COMPLETED
}

sealed interface PinAuthAction {
    data class Digit(val value: Int) : PinAuthAction
    data object Backspace : PinAuthAction
    data object RequestBiometric : PinAuthAction
    data object BiometricAuthenticated : PinAuthAction
    data object RequestReset : PinAuthAction
}

sealed interface PinAuthResult {
    data object Created : PinAuthResult
    data object Validated : PinAuthResult
    data object Changed : PinAuthResult
    data object Deleted : PinAuthResult
    data object AttemptsExhausted : PinAuthResult
    data object ResetRequested : PinAuthResult
    data object PinAlreadyConfigured : PinAuthResult
    data object PinNotConfigured : PinAuthResult
    data class Error(val cause: Throwable) : PinAuthResult
}
