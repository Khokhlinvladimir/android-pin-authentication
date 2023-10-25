package com.android.pinlibrary.utils.state.createpin

sealed class CreatePinScreenIntent {

    object InitialState : CreatePinScreenIntent()
    data class EnterPin(val pin: String) : CreatePinScreenIntent()
    data class ConfirmPin(val pin: String) : CreatePinScreenIntent()
    object CreatePin : CreatePinScreenIntent()
}
