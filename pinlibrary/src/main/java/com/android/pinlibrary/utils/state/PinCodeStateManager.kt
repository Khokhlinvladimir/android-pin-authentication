package com.android.pinlibrary.utils.state

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.pinlibrary.utils.enums.PinCodeScenario
import com.android.pinlibrary.utils.preferences.AttemptCounter
import com.android.pinlibrary.utils.preferences.PinCodeManager
import com.android.pinlibrary.utils.preferences.SettingsManager

/**
 * val pinCodeStateManager = PinCodeStateManager.getInstance()
 *
 * Installing the scenario
 * pinCodeStateManager.setScenario(PinCodeScenario.CREATION)
 *
 * Getting the current scenario using [LiveData]
 *
 * val currentScenarioLiveData = pinCodeStateManager.currentScenario
 * currentScenarioLiveData.observe(owner) { scenario ->
 * Handling a scenario change
 * }
 */

class PinCodeStateManager private constructor() : IPinCodeStateManager {

    private val _currentScenario = MutableLiveData<PinCodeScenario>()
    override val currentScenario: LiveData<PinCodeScenario> = _currentScenario

    private var onLoginAttemptsExpended: (() -> Unit)? = null
    private var onCreationSuccess: ((isSuccess: Boolean) -> Unit)? = null
    private var onValidationSuccess: ((isSuccess: Boolean) -> Unit)? = null
    private var onChangeSuccess: ((isSuccess: Boolean) -> Unit)? = null
    private var onDeletionSuccess: ((isSuccess: Boolean) -> Unit)? = null
    private var onBiometricAuthentication: ((isSuccess: Boolean) -> Unit)? = null
    private var onResetPassword: (() -> Unit)? = null
    var isValidationEnabled: Boolean = true

    companion object {
        private var instance: PinCodeStateManager? = null
        fun getInstance(): PinCodeStateManager {
            return instance ?: synchronized(this) {
                instance ?: PinCodeStateManager().also { instance = it }
            }
        }
    }

    override fun setScenario(scenario: PinCodeScenario) {
        _currentScenario.value = scenario
    }

    internal fun setCreationSuccess(isSuccess: Boolean) {
        onCreationSuccess?.invoke(isSuccess)
    }

    override fun onCreationSuccess(callback: (isSuccess: Boolean) -> Unit) {
        this.onCreationSuccess = callback
    }

    internal fun setValidationSuccess(isSuccess: Boolean) {
        onValidationSuccess?.invoke(isSuccess)
    }

    override fun onValidationSuccess(callback: (isSuccess: Boolean) -> Unit) {
        this.onValidationSuccess = callback
    }

    internal fun setChangeSuccess(isSuccess: Boolean) {
        onChangeSuccess?.invoke(isSuccess)
    }

    override fun onChangeSuccess(callback: (isSuccess: Boolean) -> Unit) {
        this.onChangeSuccess = callback
    }

    internal fun setDeletionSuccess(isSuccess: Boolean) {
        onDeletionSuccess?.invoke(isSuccess)
    }

    override fun onDeletionSuccess(callback: (isSuccess: Boolean) -> Unit) {
        this.onDeletionSuccess = callback
    }

    internal fun setLoginAttemptsExpended() {
        onLoginAttemptsExpended?.invoke()
    }

    override fun onLoginAttemptsExpended(callback: () -> Unit) {
        this.onLoginAttemptsExpended = callback
    }

    internal fun setBiometricAuthentication(isSuccess: Boolean) {
        onBiometricAuthentication?.invoke(isSuccess)
    }

    override fun onBiometricAuthentication(callback: (isSuccess: Boolean) -> Unit) {
        this.onBiometricAuthentication = callback
    }

    internal fun setResetPassword() {
        onResetPassword?.invoke()
    }

    override fun onResetPassword(callback: () -> Unit) {
        this.onResetPassword = callback
    }

    override fun clearConfiguration(application: Application) {
        val pinCodeManager = PinCodeManager(context = application)
        val attemptCounter = AttemptCounter(context = application)
        pinCodeManager.clearPinCode()
        attemptCounter.resetAttempts()
    }

    override fun isPinCodeSaved(application: Application): Boolean {
        val pinCodeManager = PinCodeManager(context = application)
        return pinCodeManager.loadPinCode() != null
    }

    /**
     * Set the value of the "Enable biometrics" option and return the current value.
     *
     * @param enabled true to enable biometrics; false to turn off.
     * @param application Application context.
     * @return true if biometrics is enabled; false otherwise.
     */
    override fun setBiometricEnabled(enabled: Boolean, application: Application): Boolean {
        val settingsManager = SettingsManager(context = application)
        settingsManager.setBiometricEnabled(enabled)
        return settingsManager.isBiometricEnabled()
    }

    /**
     * Set the maximum number of pin code entry attempts and return the current value.
     *
     * @param maxAttempts New maximum number of attempts.
     * @param application Application context.
     * @return The current maximum number of retries.
     */
    override fun setMaxPinAttempts(maxAttempts: Int, application: Application): Int {
        val settingsManager = SettingsManager(context = application)
        settingsManager.setMaxPinAttempts(maxAttempts)
        return settingsManager.getMaxPinAttempts()
    }

    /**
     * Set pin length and return current value.
     *
     * @param pinLength New pin length.
     * @param application Application context.
     * @return Current pin length.
     */
    override fun setPinLength(pinLength: Int, application: Application): Int {
        val settingsManager = SettingsManager(context = application)
        settingsManager.setPinLength(pinLength)
        return settingsManager.getPinLength()
    }

    override fun setTemporaryPinValidationState(isEnabled: Boolean) {
        isValidationEnabled = isEnabled
    }
}