package com.android.pinlibrary.utils.helpers

import com.android.pinlibrary.utils.keyboard.KeyboardButtonEnum

fun fillArrayWithButtons(
    button: KeyboardButtonEnum,
    array: MutableList<Int>,
    maxLength: Int,
    callback: (List<Int>) -> Unit
) {

    if (button == KeyboardButtonEnum.BUTTON_FINGERPRINT) {
        return
    }

    if (button == KeyboardButtonEnum.BUTTON_CLEAR) {
        /** If the "Clear" button was pressed, remove the last element from the array **/
        if (array.isNotEmpty()) {
            array.removeAt(array.size - 1)
        }
    } else {
        /** If there is no "Clear" button, add the button value to the array **/
        if (array.size < maxLength) {
            array.add(button.buttonValue)
        }
    }

    /** Call callback to notify about array changes **/
    callback(array)
}
