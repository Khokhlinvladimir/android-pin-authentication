package com.android.pinlibrary.utils.state.changepin

sealed class ChangePinScreenIntent {

    object InitialState : ChangePinScreenIntent()
    data class EnterCurrentPin(val currentPin: String) : ChangePinScreenIntent()
    data class EnterNewPin(val newPin: String) : ChangePinScreenIntent()
    data class ConfirmNewPin(val newPin: String) : ChangePinScreenIntent()
    object ChangePin : ChangePinScreenIntent()
}