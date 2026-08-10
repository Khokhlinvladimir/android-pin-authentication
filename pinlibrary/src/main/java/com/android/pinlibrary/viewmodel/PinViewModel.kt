package com.android.pinlibrary.viewmodel

import android.os.Looper
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
                _changePinScreenState.setValueSafely(ChangePinScreenState.InitialState)
            }

            is ChangePinScreenIntent.EnterCurrentPin -> {
                _changePinScreenState.setValueSafely(
                    ChangePinScreenState.EnteringCurrentPin(intent.currentPin)
                )
            }

            is ChangePinScreenIntent.EnterNewPin -> {
                _changePinScreenState.setValueSafely(
                    ChangePinScreenState.EnteringNewPin(intent.newPin)
                )
            }

            is ChangePinScreenIntent.ConfirmNewPin -> {
                _changePinScreenState.setValueSafely(
                    ChangePinScreenState.ConfirmingNewPin(intent.newPin)
                )
            }

            is ChangePinScreenIntent.ChangePin -> {
                _changePinScreenState.setValueSafely(ChangePinScreenState.PinChangedSuccess)
            }
        }
    }

    fun processIntent(intent: CreatePinScreenIntent) {
        when (intent) {
            is CreatePinScreenIntent.InitialState -> {
                _createPinScreenState.setValueSafely(CreatePinScreenState.InitialState)
            }

            is CreatePinScreenIntent.EnterPin -> {
                _createPinScreenState.setValueSafely(
                    CreatePinScreenState.EnteringPinState(intent.pin)
                )
            }

            is CreatePinScreenIntent.ConfirmPin -> {
                _createPinScreenState.setValueSafely(
                    CreatePinScreenState.ConfirmingPinState(intent.pin)
                )
            }

            is CreatePinScreenIntent.CreatePin -> {
                _createPinScreenState.setValueSafely(CreatePinScreenState.PinCreatedState)
            }
        }
    }

    fun processIntent(intent: DeletePinScreenIntent) {
        when (intent) {
            is DeletePinScreenIntent.InitialState -> {
                _deletePinScreenState.setValueSafely(DeletePinScreenState.InitialState)
            }

            is DeletePinScreenIntent.EnterPin -> {
                _deletePinScreenState.setValueSafely(
                    DeletePinScreenState.EnteringPinState(intent.pin)
                )
            }

            is DeletePinScreenIntent.DeletePin -> {
                _deletePinScreenState.setValueSafely(DeletePinScreenState.PinDeletedState)
            }
        }
    }

    fun processIntent(intent: ValidationPinScreenIntent) {
        when (intent) {
            is ValidationPinScreenIntent.InitialState -> {
                _validationPinScreenState.setValueSafely(ValidationPinScreenState.InitialState)
            }

            is ValidationPinScreenIntent.EnterPin -> {
                _validationPinScreenState.setValueSafely(
                    ValidationPinScreenState.EnteringPinState(intent.pin)
                )
            }

            is ValidationPinScreenIntent.ValidatePin -> {
                _validationPinScreenState.setValueSafely(ValidationPinScreenState.PinValidatedState)
            }
        }
    }

    private fun <T> MutableLiveData<T>.setValueSafely(value: T) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            this.value = value
        } else {
            postValue(value)
        }
    }
}
