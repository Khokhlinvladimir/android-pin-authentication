package com.android.pinlibrary.utils.state.deletepin

sealed class DeletePinScreenState {

    data object InitialState : DeletePinScreenState()
    data class EnteringPinState(val pin: String) : DeletePinScreenState()
    data object PinDeletedState : DeletePinScreenState()
    data class ErrorState(val errorMessage: String) : DeletePinScreenState()
}