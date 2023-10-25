package com.android.pinlibrary.utils.preferences

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Base64
import com.android.pinlibrary.utils.encryption.Encryptor
import com.android.pinlibrary.utils.encryption.enums.Algorithm
import java.security.SecureRandom

class PinCodeManager(context: Context) : IPinCodeManager {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun savePinCode(pinCode: String) {
        val salt = getSalt()
        val saltedPinCode = salt + pinCode + salt
        setAlgorithm(Algorithm.SHA256)
        val hashedPinCode = Encryptor().getSHA(saltedPinCode, Algorithm.SHA256)

        val editor = sharedPreferences.edit()
        editor.putString(PIN_CODE_KEY, hashedPinCode)
        editor.apply()
    }

    override fun loadPinCode(): String? {
        return sharedPreferences.getString(PIN_CODE_KEY, null)
    }

    override fun clearPinCode() {
        val editor = sharedPreferences.edit()
        editor.remove(PIN_CODE_KEY)
        editor.apply()
    }

    override fun isPinCodeCorrect(enteredPinCode: String): Boolean {
        val savedHashedPinCode = loadPinCode() ?: return false

        /**PIN was not saved*/
        val salt = getSalt()
        val saltedEnteredPinCode = salt + enteredPinCode + salt
        setAlgorithm(Algorithm.SHA256)
        val hashedEnteredPinCode = Encryptor().getSHA(saltedEnteredPinCode, Algorithm.SHA256)
        return savedHashedPinCode == hashedEnteredPinCode
    }


    private fun setAlgorithm(algorithm: Algorithm) {
        val editor = sharedPreferences.edit()
        editor.putString(PASSWORD_ALGORITHM_PREFERENCE_KEY, algorithm.value)
        editor.apply()
    }

    private fun getSalt(): String {
        var salt = sharedPreferences.getString(PASSWORD_SALT_PREFERENCE_KEY, null)
        if (salt == null) {
            salt = generateSalt()
            setSalt(salt)
        }
        return salt
    }

    private fun setSalt(salt: String) {
        val editor = sharedPreferences.edit()
        editor.putString(PASSWORD_SALT_PREFERENCE_KEY, salt)
        editor.apply()
    }

    private fun generateSalt(): String {
        val salt = ByteArray(KEY_LENGTH)
        return try {
            val sr =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) SecureRandom.getInstanceStrong()
                else SecureRandom.getInstance("SHA1PRNG")
            sr.nextBytes(salt)
            Base64.encodeToString(salt, Base64.DEFAULT)
        } catch (e: Exception) {
            DEFAULT_PASSWORD_SALT
        }
    }

    companion object {
        private const val PREFS_NAME = "PinCodePrefs"
        private const val PIN_CODE_KEY = "pin_code"
        private const val DEFAULT_PASSWORD_SALT = "7xn7@c$"
        private const val KEY_LENGTH = 256
        private const val PASSWORD_SALT_PREFERENCE_KEY = "PASSWORD_SALT_PREFERENCE_KEY"
        private const val PASSWORD_ALGORITHM_PREFERENCE_KEY = "ALGORITHM"
    }
}
