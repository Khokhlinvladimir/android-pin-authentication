package com.android.pinlibrary.ui.systemdesign.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.android.pinlibrary.R
import com.android.pinlibrary.utils.state.PinCodeStateManager

@Composable
fun ShowDialog(
    title: String,
    description: String,
    showDialog: Boolean,
    pinCodeStateManager: PinCodeStateManager,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(text = title)
            },
            text = {
                Text(text = description)
            },
            confirmButton = {
                Button(
                    onClick = {
                        pinCodeStateManager.setResetPassword()
                        onDismiss()
                    }
                ) {
                    Text(text = stringResource(id = R.string.ok_button))
                }
            }
        )
    }
}
