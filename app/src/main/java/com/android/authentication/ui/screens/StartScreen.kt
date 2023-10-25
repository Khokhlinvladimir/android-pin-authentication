package com.android.authentication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.authentication.R
import com.android.pinlibrary.utils.enums.PinCodeScenario

@Composable
fun PinCodeStartScreen(isPinCodeCreated: Boolean, onButtonClick: (PinCodeScenario) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(
                id = if (isPinCodeCreated) R.string.pin_code_created
                else R.string.pin_code_not_created
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isPinCodeCreated) {
            CreatePinCodeButton(
                PinCodeScenario.CREATION,
                R.string.create_pin_code, onButtonClick
            )
        } else {
            CreatePinCodeButton(
                PinCodeScenario.VALIDATION,
                R.string.validate_pin_code,
                onButtonClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            CreatePinCodeButton(
                PinCodeScenario.CHANGE,
                R.string.change_pin_code, onButtonClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            CreatePinCodeButton(
                PinCodeScenario.DELETION,
                R.string.delete_pin_code, onButtonClick
            )
        }
    }
}

@Composable
fun CreatePinCodeButton(
    scenario: PinCodeScenario,
    stringResId: Int,
    onButtonClick: (PinCodeScenario) -> Unit
) {
    Button(
        onClick = { onButtonClick(scenario) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp)
    ) {
        Text(text = stringResource(id = stringResId))
    }
}

