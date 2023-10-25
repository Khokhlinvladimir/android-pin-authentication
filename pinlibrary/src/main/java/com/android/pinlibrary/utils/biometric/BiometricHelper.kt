package com.android.pinlibrary.utils.biometric

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.android.pinlibrary.R
import java.util.concurrent.Executor

/**
 * Field requires API level 28 (current min is 24): android
 */
@RequiresApi(Build.VERSION_CODES.P)
class BiometricHelper(private val context: Context) {

    private var executor: Executor? = null

    private fun isBiometricSupported(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun startAuthentication(authenticationCallback: BiometricPrompt.AuthenticationCallback) {
        executor = ContextCompat.getMainExecutor(context)
        val cancellationSignal = CancellationSignal()
        val biometricPrompt = executor?.let {
            BiometricPrompt.Builder(context)
                .setTitle(context.getString(R.string.authorization))
                .setSubtitle(context.getString(R.string.use_biometry))
                .setDescription(context.getString(R.string.confirm_identity))
                .setNegativeButton(context.getString(R.string.cancel_action), it) { _, _ -> }
                .build()
        }

        executor?.let {
            biometricPrompt?.authenticate(
                cancellationSignal,
                it,
                authenticationCallback
            )
        }
    }

    fun openBiometricScanner(authenticationCallback: BiometricPrompt.AuthenticationCallback) {

        if (isBiometricSupported()) {
            startAuthentication(authenticationCallback)
        } else {
            // Биометрическая аутентификация не поддерживается
        }
    }
}

/**
 * Field requires API level 28 (current min is 24): android
 */
@Composable
@RequiresApi(Build.VERSION_CODES.P)
fun BiometricScannerScreen(authenticationCallback: BiometricPrompt.AuthenticationCallback) {
    val context = LocalContext.current
    val biometricHelper = BiometricHelper(context)

    /**
     * The process of creating the requestPermissionLauncher that is used to request permission
     */
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            biometricHelper.openBiometricScanner(authenticationCallback)
        } else {
            /**
             * Perform actions if not granted
             */
        }
    }

    when (ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.USE_BIOMETRIC
    )) {
        PackageManager.PERMISSION_GRANTED -> {
            biometricHelper.openBiometricScanner(authenticationCallback)
        }

        else -> {
            /**
             * Request permission if not granted
             */
            requestPermissionLauncher.launch(Manifest.permission.USE_BIOMETRIC)
        }
    }
}




