package com.android.pinlibrary.utils.preferences

interface IPinCodeManager {
    fun savePinCode(pinCode: String)
    fun loadPinCode(): String?
    fun clearPinCode()
    fun isPinCodeCorrect(enteredPinCode: String): Boolean
}
