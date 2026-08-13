package com.android.pinlibrary.ui.motion

import androidx.compose.runtime.Immutable

/** Motion and haptic configuration shared by the PIN authentication UI. */
@Immutable
data class PinAuthMotionSpec(
    val enabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val ambientBackgroundEnabled: Boolean = true,
    val pressScale: Float = 0.9f,
    val stepDurationMillis: Int = 280,
    val feedbackDurationMillis: Int = 420,
    val processingPulseMillis: Int = 760,
    val completionHoldMillis: Long = 460L
) {
    init {
        require(pressScale in 0.75f..1f) { "Press scale must be between 0.75 and 1" }
        require(stepDurationMillis >= 0) { "Step duration cannot be negative" }
        require(feedbackDurationMillis >= 0) { "Feedback duration cannot be negative" }
        require(processingPulseMillis > 0) { "Processing pulse duration must be positive" }
        require(completionHoldMillis >= 0) { "Completion hold cannot be negative" }
    }

    companion object {
        @JvmField
        val Premium = PinAuthMotionSpec()

        @JvmField
        val None = PinAuthMotionSpec(
            enabled = false,
            hapticsEnabled = false,
            ambientBackgroundEnabled = false,
            pressScale = 1f,
            stepDurationMillis = 0,
            feedbackDurationMillis = 0,
            processingPulseMillis = 1,
            completionHoldMillis = 0
        )
    }
}
