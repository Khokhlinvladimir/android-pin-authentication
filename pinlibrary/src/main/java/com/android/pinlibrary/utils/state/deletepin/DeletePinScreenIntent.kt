package com.android.pinlibrary.utils.state.deletepin

sealed class DeletePinScreenIntent {

    data object InitialState : DeletePinScreenIntent()
    data class EnterPin(val pin: String) : DeletePinScreenIntent()
    data object DeletePin : DeletePinScreenIntent()
}