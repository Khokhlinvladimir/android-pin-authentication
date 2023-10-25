package com.android.pinlibrary.utils.state

import android.app.Application
import androidx.lifecycle.LiveData
import com.android.pinlibrary.utils.enums.PinCodeScenario

interface IPinCodeStateManager {
    val currentScenario: LiveData<PinCodeScenario>

    fun setScenario(scenario: PinCodeScenario)

    fun onCreationSuccess(callback: (isSuccess: Boolean) -> Unit)

    fun onValidationSuccess(callback: (isSuccess: Boolean) -> Unit)

    fun onChangeSuccess(callback: (isSuccess: Boolean) -> Unit)

    fun onDeletionSuccess(callback: (isSuccess: Boolean) -> Unit)

    fun onLoginAttemptsExpended(callback: () -> Unit)
    fun onBiometricAuthentication(callback: (isSuccess: Boolean) -> Unit)

    fun onResetPassword(callback: () -> Unit)

    fun clearConfiguration(application: Application)
    fun isPinCodeSaved(application: Application): Boolean

    fun setBiometricEnabled(enabled: Boolean, application: Application): Boolean
    fun setMaxPinAttempts(maxAttempts: Int, application: Application): Int
    fun setPinLength(pinLength: Int, application: Application): Int

    fun setTemporaryPinValidationState(isEnabled: Boolean)
}
