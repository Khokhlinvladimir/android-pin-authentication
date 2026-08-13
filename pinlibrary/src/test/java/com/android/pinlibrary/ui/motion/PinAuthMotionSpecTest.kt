package com.android.pinlibrary.ui.motion

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinAuthMotionSpecTest {

    @Test
    fun premiumEnablesMotionAndHaptics() {
        assertTrue(PinAuthMotionSpec.Premium.enabled)
        assertTrue(PinAuthMotionSpec.Premium.hapticsEnabled)
        assertTrue(PinAuthMotionSpec.Premium.completionHoldMillis > 0)
    }

    @Test
    fun noneDisablesEveryOptionalEffect() {
        assertFalse(PinAuthMotionSpec.None.enabled)
        assertFalse(PinAuthMotionSpec.None.hapticsEnabled)
        assertFalse(PinAuthMotionSpec.None.ambientBackgroundEnabled)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnsafePressScale() {
        PinAuthMotionSpec(pressScale = 0.5f)
    }
}
