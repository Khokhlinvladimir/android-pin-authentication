package com.android.pinlibrary.utils.preferences

import android.content.Context
import android.content.SharedPreferences

class AttemptCounter(val context: Context) : IAttemptCounter {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val settingsManager = SettingsManager(context = context)

    override fun saveAttempts(attempts: Int) {
        sharedPreferences.edit().putInt(KEY_ATTEMPTS, attempts).apply()
    }

    override fun getAttempts(): Int {
        return sharedPreferences.getInt(KEY_ATTEMPTS, settingsManager.getMaxPinAttempts())
    }

    override fun decrementAttempts() {
        val currentAttempts = getAttempts()
        if (currentAttempts > 0) {
            saveAttempts(currentAttempts - 1)
        }
    }

    override fun resetAttempts() {
        saveAttempts(settingsManager.getMaxPinAttempts())
    }

    companion object {
        private const val PREFS_NAME = "AttemptPrefs"
        private const val KEY_ATTEMPTS = "attempts"
    }
}
