package com.android.pinlibrary.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.pinlibrary.R
import com.android.pinlibrary.ui.systemdesign.dialog.ShowDialog
import com.android.pinlibrary.utils.state.PinCodeStateManager

@Composable
fun PinCodeScreenForgot(
    text: String,
    pinCodeStateManager: PinCodeStateManager
) {
    var showDialog by remember { mutableStateOf(false) }

    Text(
        text = text,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .wrapContentSize()
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .clickable {
                showDialog = true
            }
    )

    ShowDialog(
        title = stringResource(id = R.string.forgot_pin),
        description = stringResource(id = R.string.forgot_pin_instruction),
        showDialog = showDialog,
        pinCodeStateManager = pinCodeStateManager,
        onDismiss = {
            showDialog = false
        }
    )
}
