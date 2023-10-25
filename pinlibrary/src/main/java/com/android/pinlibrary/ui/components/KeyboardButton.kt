package com.android.pinlibrary.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.pinlibrary.ui.style.ripple.RippleView
import com.android.pinlibrary.utils.keyboard.KeyboardButtonEnum

@Composable
fun TextButton(
    number: String,
    keyboardEnum: KeyboardButtonEnum
) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(vertical = 10.dp, horizontal = 23.dp)
    ) {
        RippleView(
            modifier = Modifier.size(50.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .clickable {
                        setNumberClickListener(
                            onNumberClickListener = onNumberClickListener,
                            keyboardEnum = keyboardEnum
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    style = TextStyle(fontSize = 25.sp)
                )
            }
        }
    }
}

@Composable
fun ImageButton(
    resourceId: Int,
    keyboardEnum: KeyboardButtonEnum
) {
    val painter = painterResource(id = resourceId)
    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(vertical = 10.dp, horizontal = 23.dp)
    ) {
        RippleView(
            modifier = Modifier.size(50.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .clickable {
                        setNumberClickListener(
                            onNumberClickListener = onNumberClickListener,
                            keyboardEnum = keyboardEnum
                        )
                    },
                contentAlignment = Alignment.Center
            ) {

                Image(
                    painter = painter,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun ImageButtonStub(
    resourceId: Int
) {
    val painter = painterResource(id = resourceId)
    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(vertical = 10.dp, horizontal = 23.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painter,
                contentDescription = null
            )
        }
    }
}



