package com.android.pinlibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.pinlibrary.utils.state.changepin.ChangePinScreenIntent
import com.android.pinlibrary.utils.state.changepin.ChangePinScreenState
import com.android.pinlibrary.utils.state.createpin.CreatePinScreenIntent
import com.android.pinlibrary.utils.state.createpin.CreatePinScreenState
import com.android.pinlibrary.utils.state.deletepin.DeletePinScreenIntent
import com.android.pinlibrary.utils.state.deletepin.DeletePinScreenState
import com.android.pinlibrary.utils.state.validationpin.ValidationPinScreenIntent
import com.android.pinlibrary.utils.state.validationpin.ValidationPinScreenState

class PinViewModel : ViewModel() {

    private val _changePinScreenState = MutableLiveData<ChangePinScreenState>()
    val changePinScreenState: LiveData<ChangePinScreenState>
        get() = _changePinScreenState

    private val _createPinScreenState = MutableLiveData<CreatePinScreenState>()
    val createPinScreenState: LiveData<CreatePinScreenState>
        get() = _createPinScreenState

    private val _deletePinScreenState = MutableLiveData<DeletePinScreenState>()
    val deletePinScreenState: LiveData<DeletePinScreenState>
        get() = _deletePinScreenState

    private val _validationPinScreenState = MutableLiveData<ValidationPinScreenState>()
    val validationPinScreenState: LiveData<ValidationPinScreenState>
        get() = _validationPinScreenState

    fun processIntent(intent: ChangePinScreenIntent) {
        when (intent) {
            is ChangePinScreenIntent.InitialState -> {
                _changePinScreenState.value = ChangePinScreenState.InitialState
            }

            is ChangePinScreenIntent.EnterCurrentPin -> {
                _changePinScreenState.value =
                    ChangePinScreenState.EnteringCurrentPin(intent.currentPin)
            }

            is ChangePinScreenIntent.EnterNewPin -> {
                _changePinScreenState.value = ChangePinScreenState.EnteringNewPin(intent.newPin)
            }

            is ChangePinScreenIntent.ConfirmNewPin -> {
                _changePinScreenState.value = ChangePinScreenState.ConfirmingNewPin(intent.newPin)
            }

            is ChangePinScreenIntent.ChangePin -> {
                _changePinScreenState.value = ChangePinScreenState.PinChangedSuccess
            }
        }
    }

    fun processIntent(intent: CreatePinScreenIntent) {
        when (intent) {
            is CreatePinScreenIntent.InitialState -> {
                _createPinScreenState.value = CreatePinScreenState.InitialState
            }

            is CreatePinScreenIntent.EnterPin -> {
                _createPinScreenState.value = CreatePinScreenState.EnteringPinState(intent.pin)
            }

            is CreatePinScreenIntent.ConfirmPin -> {
                _createPinScreenState.value = CreatePinScreenState.ConfirmingPinState(intent.pin)
            }

            is CreatePinScreenIntent.CreatePin -> {
                _createPinScreenState.value = CreatePinScreenState.PinCreatedState
            }
        }
    }

    fun processIntent(intent: DeletePinScreenIntent) {
        when (intent) {
            is DeletePinScreenIntent.InitialState -> {
                _deletePinScreenState.value = DeletePinScreenState.InitialState
            }

            is DeletePinScreenIntent.EnterPin -> {
                _deletePinScreenState.value = DeletePinScreenState.EnteringPinState(intent.pin)
            }

            is DeletePinScreenIntent.DeletePin -> {
                _deletePinScreenState.value = DeletePinScreenState.PinDeletedState
            }
        }
    }

    fun processIntent(intent: ValidationPinScreenIntent) {
        when (intent) {
            is ValidationPinScreenIntent.InitialState -> {
                _validationPinScreenState.value = ValidationPinScreenState.InitialState
            }

            is ValidationPinScreenIntent.EnterPin -> {
                _validationPinScreenState.value =
                    ValidationPinScreenState.EnteringPinState(intent.pin)
            }

            is ValidationPinScreenIntent.ValidatePin -> {
                _validationPinScreenState.value = ValidationPinScreenState.PinValidatedState
            }
        }
    }
}
