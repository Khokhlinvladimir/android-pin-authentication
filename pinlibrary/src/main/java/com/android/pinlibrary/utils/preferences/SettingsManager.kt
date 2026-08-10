package com.android.pinlibrary.utils.preferences

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) : ISettingsManager {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

    override fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean(BIOMETRIC_ENABLED_KEY, true)
        /** Enabled by default */
    }

    override fun setBiometricEnabled(enabled: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(BIOMETRIC_ENABLED_KEY, enabled)
        editor.apply()
    }

    override fun isAutoLaunchBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean(AUTO_LAUNCH_BIOMETRIC_ENABLED_KEY, true)
        /** Enabled by default */
    }

    override fun setAutoLaunchBiometricEnabled(enabled: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(AUTO_LAUNCH_BIOMETRIC_ENABLED_KEY, enabled)
        editor.apply()
    }

    override fun getMaxPinAttempts(): Int {
        return sharedPreferences.getInt(MAX_PIN_ATTEMPTS_KEY, 5)
        /** Default 5 attempts */
    }

    override fun setMaxPinAttempts(maxAttempts: Int) {
        require(maxAttempts in MIN_PIN_ATTEMPTS..MAX_PIN_ATTEMPTS) {
            "Maximum PIN attempts must be between $MIN_PIN_ATTEMPTS and $MAX_PIN_ATTEMPTS"
        }
        val editor = sharedPreferences.edit()
        editor.putInt(MAX_PIN_ATTEMPTS_KEY, maxAttempts)
        editor.apply()
    }

    override fun getPinLength(): Int {
        return sharedPreferences.getInt(PIN_LENGTH_KEY, 4)
        /** Default 4 characters */
    }

    override fun setPinLength(pinLength: Int) {
        require(pinLength in MIN_PIN_LENGTH..MAX_PIN_LENGTH) {
            "PIN length must be between $MIN_PIN_LENGTH and $MAX_PIN_LENGTH"
        }
        val editor = sharedPreferences.edit()
        editor.putInt(PIN_LENGTH_KEY, pinLength)
        editor.apply()
    }

    companion object {
        private const val BIOMETRIC_ENABLED_KEY = "biometric_enabled"
        private const val AUTO_LAUNCH_BIOMETRIC_ENABLED_KEY = "auto_launch_biometric_enabled"
        private const val MAX_PIN_ATTEMPTS_KEY = "max_pin_attempts"
        private const val PIN_LENGTH_KEY = "pin_length"
        const val MIN_PIN_ATTEMPTS = 1
        const val MAX_PIN_ATTEMPTS = 100
        const val MIN_PIN_LENGTH = 4
        const val MAX_PIN_LENGTH = 12
    }
}
