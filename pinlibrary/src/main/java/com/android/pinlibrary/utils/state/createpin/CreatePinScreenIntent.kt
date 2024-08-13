package com.android.pinlibrary.utils.state.createpin

sealed class CreatePinScreenIntent {

    data object InitialState : CreatePinScreenIntent()
    data class EnterPin(val pin: String) : CreatePinScreenIntent()
    data class ConfirmPin(val pin: String) : CreatePinScreenIntent()
    data object CreatePin : CreatePinScreenIntent()
}
