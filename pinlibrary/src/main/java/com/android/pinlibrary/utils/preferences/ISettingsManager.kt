package com.android.pinlibrary.utils.preferences

interface ISettingsManager {
    fun isBiometricEnabled(): Boolean
    fun isAutoLaunchBiometricEnabled(): Boolean
    fun setAutoLaunchBiometricEnabled(enabled: Boolean)
    fun setBiometricEnabled(enabled: Boolean)
    fun getMaxPinAttempts(): Int
    fun setMaxPinAttempts(maxAttempts: Int)
    fun getPinLength(): Int
    fun setPinLength(pinLength: Int)
}
