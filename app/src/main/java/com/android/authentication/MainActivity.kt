package com.android.authentication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.authentication.ui.screens.PinCodeStartScreen
import com.android.authentication.ui.theme.PinAuthenticationTheme
import com.android.pinlibrary.ui.screens.PinCodeScreen
import com.android.pinlibrary.utils.state.PinCodeStateManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pinCodeStateManager = PinCodeStateManager.getInstance()

        setContent {
            PinAuthenticationTheme {

                var isPinCodeCreated by remember { mutableStateOf(false) }
                var isPinCodeScreenVisible by remember { mutableStateOf(false) }

                /**
                 * Handler invoked when all login attempts with the PIN code have been exhausted.
                 * This method is responsible for handling the event when all login attempts are used up.
                 */
                pinCodeStateManager.onLoginAttemptsExpended {
                    // Your handling logic here
                    isPinCodeScreenVisible = false
                    isPinCodeCreated = false
                    pinCodeStateManager.clearConfiguration(application = application)
                }

                /**
                 * Handler invoked upon successful biometric authentication.
                 * This method is responsible for handling the event of successful biometric authentication of the user.
                 */
                pinCodeStateManager.onBiometricAuthentication {
                    // Your handling logic here
                    isPinCodeScreenVisible = !it
                }

                /**
                 * Handler invoked upon successful PIN code creation or setting.
                 * This method is responsible for handling the event of successful PIN code creation or setting.
                 */
                pinCodeStateManager.onCreationSuccess {
                    // Your handling logic here
                    isPinCodeScreenVisible = false
                    isPinCodeCreated = true
                }

                /**
                 * Handler invoked upon successful PIN code change.
                 * This method is responsible for handling the event of successful PIN code change.
                 */
                pinCodeStateManager.onChangeSuccess {
                    // Your handling logic here
                    isPinCodeScreenVisible = false
                }

                /**
                 * Handler invoked upon successful PIN code validation.
                 * This method is responsible for handling the event of successful validation of the entered PIN code.
                 */
                pinCodeStateManager.onValidationSuccess {
                    // Your handling logic here
                    isPinCodeScreenVisible = false
                }

                /**
                 * Handler invoked upon successful PIN code deletion.
                 * This method is responsible for handling the event of successful PIN code deletion.
                 */
                pinCodeStateManager.onDeletionSuccess {
                    // Your handling logic here
                    isPinCodeScreenVisible = false
                    isPinCodeCreated = false
                }

                /**
                 * Handler invoked when resetting the password.
                 * This method is responsible for handling the event of password or PIN code reset.
                 */
                pinCodeStateManager.onResetPassword {
                    // Your handling logic here
                    isPinCodeScreenVisible = false
                    isPinCodeCreated = false
                    pinCodeStateManager.clearConfiguration(application = application)
                }

                /**
                 * Indicates whether a PIN code is saved in the application.
                 *
                 * @return True if a PIN code is saved, false otherwise.
                 */
                val isPinCodeSaved = pinCodeStateManager.isPinCodeSaved(
                    application = application
                )
                isPinCodeCreated = isPinCodeSaved

                /**
                 * Sets the maximum number of PIN code attempts.
                 *
                 * @param maxAttempts The maximum number of PIN code attempts allowed.
                 * @param application The application context.
                 */
                val setMaxPinAttempts = pinCodeStateManager.setMaxPinAttempts(
                    maxAttempts = 4,
                    application = application
                )

                /**
                 * Sets the length of the PIN code.
                 *
                 * @param pinLength The desired length for the PIN code.
                 * @param application The application context.
                 */
                val setPinLength =
                    pinCodeStateManager.setPinLength(
                        pinLength = 4,
                        application = application
                    )

                /**
                 * Enables or disables biometric authentication for PIN code.
                 *
                 * @param enabled True to enable biometric authentication, false to disable.
                 * @param application The application context.
                 */
                val setBiometricEnabled = pinCodeStateManager.setBiometricEnabled(
                    enabled = true,
                    application = application
                )

                //TODO: Добавить показывать шторку биометрической аутентификации или нет
                // диалог о включении биометрии
                // исправить баг с вызовом шторки и тапом по экрану который возвращает на стартовый экран

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    if (isPinCodeScreenVisible) {
                        PinCodeScreen()
                    } else {
                        PinCodeStartScreen(isPinCodeCreated) {
                            pinCodeStateManager.setScenario(it)
                            isPinCodeScreenVisible = true
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PinAuthenticationTheme {
        PinCodeScreen()
    }
}
