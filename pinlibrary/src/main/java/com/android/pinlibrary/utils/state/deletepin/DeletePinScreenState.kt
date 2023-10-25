package com.android.pinlibrary.utils.state.deletepin

sealed class DeletePinScreenState {

    object InitialState : DeletePinScreenState()
    data class EnteringPinState(val pin: String) : DeletePinScreenState()
    object PinDeletedState : DeletePinScreenState()
    data class ErrorState(val errorMessage: String) : DeletePinScreenState()
}