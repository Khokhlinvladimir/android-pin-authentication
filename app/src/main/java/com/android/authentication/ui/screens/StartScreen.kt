package com.android.authentication.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.authentication.R
import com.android.pinlibrary.utils.enums.PinCodeScenario

@Composable
fun PinCodeStartScreen(isPinCodeCreated: Boolean, onButtonClick: (PinCodeScenario) -> Unit) {
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { contentVisible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(
            targetState = isPinCodeCreated,
            transitionSpec = {
                (fadeIn(tween(240)) + scaleIn(initialScale = 0.92f)) togetherWith
                    (fadeOut(tween(160)) + scaleOut(targetScale = 1.06f))
            },
            label = "demo-pin-status"
        ) { created ->
            Text(
                text = stringResource(
                    id = if (created) R.string.pin_code_created else R.string.pin_code_not_created
                ),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(tween(280, delayMillis = 80)) +
                slideInVertically(tween(320, delayMillis = 80)) { it / 3 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (!isPinCodeCreated) {
                    CreatePinCodeButton(
                        PinCodeScenario.CREATION,
                        R.string.create_pin_code,
                        onButtonClick
                    )
                } else {
                    CreatePinCodeButton(
                        PinCodeScenario.VALIDATION,
                        R.string.validate_pin_code,
                        onButtonClick
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    CreatePinCodeButton(
                        PinCodeScenario.CHANGE,
                        R.string.change_pin_code,
                        onButtonClick
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    CreatePinCodeButton(
                        PinCodeScenario.DELETION,
                        R.string.delete_pin_code,
                        onButtonClick
                    )
                }
            }
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
            .height(54.dp),
        shape = MaterialTheme.shapes.large,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 0.dp
        )
    ) {
        Text(text = stringResource(id = stringResId))
    }
}

