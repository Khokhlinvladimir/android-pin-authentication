package com.android.pinlibrary.utils.state.validationpin

sealed class ValidationPinScreenState {

    object InitialState : ValidationPinScreenState()
    data class EnteringPinState(val pin: String) : ValidationPinScreenState()
    data class ErrorState(val errorMessage: String) : ValidationPinScreenState()
    object PinValidatedState : ValidationPinScreenState()
}