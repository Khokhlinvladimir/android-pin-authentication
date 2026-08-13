package com.android.pinlibrary.ui.customization

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class PinAuthCustomizationTest {

    @Test
    fun acceptsIndependentKeypadAndMaskStyles() {
        val customization = PinAuthCustomization(
            keypadStyle = PinKeypadStyle(keySize = 64.dp),
            maskStyle = PinMaskStyle(dotSize = 32.dp, dotRadius = 12.dp)
        )

        assertEquals(64.dp, customization.keypadStyle.keySize)
        assertEquals(32.dp, customization.maskStyle.dotSize)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsMaskRadiusThatDoesNotFit() {
        PinMaskStyle(dotSize = 20.dp, dotRadius = 11.dp)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidDisabledAlpha() {
        PinKeypadStyle(disabledAlpha = 1.2f)
    }
}
