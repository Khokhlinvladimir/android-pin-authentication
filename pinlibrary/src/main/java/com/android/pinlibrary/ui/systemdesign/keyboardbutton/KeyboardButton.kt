package com.android.pinlibrary.ui.systemdesign.keyboardbutton

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
import com.android.pinlibrary.ui.systemdesign.ripple.RippleView
import com.android.pinlibrary.ui.systemdesign.theme.Dimens
import com.android.pinlibrary.utils.keyboard.KeyboardButtonEnum
import com.android.pinlibrary.utils.listeners.NumberListener

@Composable
fun NumberButton(
    number: String,
    keyboardEnum: KeyboardButtonEnum
) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(
                vertical = Dimens.verticalKeyboardButtonPadding,
                horizontal = Dimens.horizontalKeyboardButtonPadding
            )
    ) {
        RippleView(
            modifier = Modifier.size(Dimens.keyBoardButtonSize)
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.keyBoardButtonSize)
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
                    style = TextStyle(fontSize = Dimens.keyBoardButtonFontSize)
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
            .padding(
                vertical = Dimens.verticalKeyboardButtonPadding,
                horizontal = Dimens.horizontalKeyboardButtonPadding
            )
    ) {
        RippleView(
            modifier = Modifier.size(Dimens.keyBoardButtonSize)
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.keyBoardButtonSize)
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
            .padding(
                vertical = Dimens.verticalKeyboardButtonPadding,
                horizontal = Dimens.horizontalKeyboardButtonPadding
            )
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.keyBoardButtonSize)
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

internal var onNumberClickListener: NumberListener? = null
internal fun setNumberClickListener(
    onNumberClickListener: NumberListener?,
    keyboardEnum: KeyboardButtonEnum
) {
    onNumberClickListener?.onNumberTriggered(keyboardEnum)
}


