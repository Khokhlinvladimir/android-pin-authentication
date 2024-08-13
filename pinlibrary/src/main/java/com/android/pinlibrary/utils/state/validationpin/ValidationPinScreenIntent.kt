package com.android.pinlibrary.utils.state.validationpin

sealed class ValidationPinScreenIntent {

    data object InitialState : ValidationPinScreenIntent()
    data class EnterPin(val pin: String) : ValidationPinScreenIntent()
    data object ValidatePin : ValidationPinScreenIntent()
}