package com.android.pinlibrary.utils.state.validationpin

sealed class ValidationPinScreenIntent {

    object InitialState : ValidationPinScreenIntent()
    data class EnterPin(val pin: String) : ValidationPinScreenIntent()
    object ValidatePin : ValidationPinScreenIntent()
}