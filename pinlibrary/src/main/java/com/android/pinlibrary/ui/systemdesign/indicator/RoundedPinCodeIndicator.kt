package com.android.pinlibrary.ui.systemdesign.indicator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RoundedBoxesRow(startQuantity: Int, quantity: Int) {
    LazyRow(
        modifier = Modifier
            .wrapContentSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        items(quantity) {
            RoundedBox(isFilled = true)
        }
        items(startQuantity - quantity) {
            RoundedBox(isFilled = false)
        }
    }
}

@Composable
fun RoundedBox(isFilled: Boolean) {
    val color = if (isSystemInDarkTheme()) Color.White else Color.Black
    val boxModifier = Modifier
        .size(35.dp)
        .padding(9.dp)
        .let {
            if (isFilled) it.background(color, shape = CircleShape)
            else it.border(1.dp, color, shape = CircleShape)
        }

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        /** You can add additional content inside the round Box if necessary **/
    }
}



