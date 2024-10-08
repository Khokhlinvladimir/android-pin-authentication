package com.android.pinlibrary.utils.state.changepin

sealed class ChangePinScreenState {
    data object InitialState : ChangePinScreenState()
    data class EnteringCurrentPin(val currentPin: String) : ChangePinScreenState()
    data class EnteringNewPin(val newPin: String) : ChangePinScreenState()
    data class ConfirmingNewPin(val newPin: String) : ChangePinScreenState()
    data object PinChangedSuccess : ChangePinScreenState()
    data class ErrorState(val errorMessage: String) : ChangePinScreenState()
}