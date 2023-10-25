package com.android.pinlibrary.utils.state.deletepin

sealed class DeletePinScreenIntent {

    object InitialState : DeletePinScreenIntent()
    data class EnterPin(val pin: String) : DeletePinScreenIntent()
    object DeletePin : DeletePinScreenIntent()
}