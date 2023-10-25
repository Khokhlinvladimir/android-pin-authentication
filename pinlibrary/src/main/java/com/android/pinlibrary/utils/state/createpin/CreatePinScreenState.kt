package com.android.pinlibrary.utils.state.createpin

sealed class CreatePinScreenState {

    object InitialState : CreatePinScreenState()
    data class EnteringPinState(val pin: String) : CreatePinScreenState()
    data class ConfirmingPinState(val pin: String) : CreatePinScreenState()
    object PinCreatedState : CreatePinScreenState()
    data class ErrorState(val errorMessage: String) : CreatePinScreenState()
}
