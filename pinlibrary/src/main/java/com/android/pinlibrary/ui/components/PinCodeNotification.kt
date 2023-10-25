package com.android.pinlibrary.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinCodeScreenNotification(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        color = Color.Red,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .wrapContentSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        overflow = TextOverflow.Visible
    )
}
