package com.android.pinlibrary.utils.listeners

import com.android.pinlibrary.utils.keyboard.KeyboardButtonEnum

fun interface NumberListener {
    fun onNumberTriggered(keyboardEnum: KeyboardButtonEnum)
}
