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

    override fun getMaxPinAttempts(): Int {
        return sharedPreferences.getInt(MAX_PIN_ATTEMPTS_KEY, 5)
        /** Default 5 attempts */
    }

    override fun setMaxPinAttempts(maxAttempts: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(MAX_PIN_ATTEMPTS_KEY, maxAttempts)
        editor.apply()
    }

    override fun getPinLength(): Int {
        return sharedPreferences.getInt(PIN_LENGTH_KEY, 4)
        /** Default 4 characters */
    }

    override fun setPinLength(pinLength: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(PIN_LENGTH_KEY, pinLength)
        editor.apply()
    }

    companion object {
        private const val BIOMETRIC_ENABLED_KEY = "biometric_enabled"
        private const val MAX_PIN_ATTEMPTS_KEY = "max_pin_attempts"
        private const val PIN_LENGTH_KEY = "pin_length"
    }
}
