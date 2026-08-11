package com.android.pinlibrary.utils.preferences

import android.content.Context
import android.content.SharedPreferences
import com.android.pinlibrary.utils.encryption.Encryptor
import com.android.pinlibrary.utils.encryption.PinCodeHasher
import com.android.pinlibrary.utils.encryption.PinCodeProtector
import com.android.pinlibrary.utils.encryption.enums.Algorithm
import java.security.MessageDigest

class PinCodeManager internal constructor(
    context: Context,
    private val pinCodeHasher: PinCodeHasher,
    private val pinCodeProtector: PinCodeProtector
) : IPinCodeManager {

    constructor(context: Context) : this(context, PinCodeHasher(), PinCodeProtector())

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val settingsManager = SettingsManager(context)

    override fun savePinCode(pinCode: String) {
        require(pinCode.length == settingsManager.getPinLength()) {
            "PIN code length must match the configured PIN length"
        }
        val verifier = pinCodeProtector.create(pinCode)
        sharedPreferences.edit()
            .putString(PIN_CODE_KEY, verifier)
            .remove(PASSWORD_SALT_PREFERENCE_KEY)
            .remove(PASSWORD_ALGORITHM_PREFERENCE_KEY)
            .apply()
    }

    override fun loadPinCode(): String? {
        return sharedPreferences.getString(PIN_CODE_KEY, null)
    }

    override fun clearPinCode() {
        sharedPreferences.edit()
            .remove(PIN_CODE_KEY)
            .remove(PASSWORD_SALT_PREFERENCE_KEY)
            .remove(PASSWORD_ALGORITHM_PREFERENCE_KEY)
            .apply()
        runCatching { pinCodeProtector.deleteKey() }
    }

    override fun isPinCodeCorrect(enteredPinCode: String): Boolean {
        if (enteredPinCode.length != settingsManager.getPinLength()) return false
        val savedVerifier = loadPinCode() ?: return false
        if (pinCodeProtector.isVersionedRecord(savedVerifier)) {
            return pinCodeProtector.verify(enteredPinCode, savedVerifier)
        }
        if (pinCodeHasher.isVersionedRecord(savedVerifier)) {
            val isCorrect = pinCodeHasher.verify(enteredPinCode, savedVerifier)
            if (isCorrect) savePinCode(enteredPinCode)
            return isCorrect
        }

        val salt = sharedPreferences.getString(PASSWORD_SALT_PREFERENCE_KEY, null)
            ?: return false
        val legacyVerifier = Encryptor().getSHA(salt + enteredPinCode + salt, Algorithm.SHA256)
        val isCorrect = MessageDigest.isEqual(
            savedVerifier.toByteArray(Charsets.UTF_8),
            legacyVerifier.toByteArray(Charsets.UTF_8)
        )
        if (isCorrect) {
            savePinCode(enteredPinCode)
        }
        return isCorrect
    }

    companion object {
        private const val PREFS_NAME = "PinCodePrefs"
        private const val PIN_CODE_KEY = "pin_code"
        private const val PASSWORD_SALT_PREFERENCE_KEY = "PASSWORD_SALT_PREFERENCE_KEY"
        private const val PASSWORD_ALGORITHM_PREFERENCE_KEY = "ALGORITHM"
    }
}
