package com.android.pinlibrary

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.android.pinlibrary.utils.encryption.Encryptor
import com.android.pinlibrary.utils.encryption.enums.Algorithm
import com.android.pinlibrary.utils.preferences.AttemptCounter
import com.android.pinlibrary.utils.preferences.PinCodeManager
import com.android.pinlibrary.utils.preferences.SettingsManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class PinCodeManagerInstrumentedTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        clearPreferences()
    }

    @After
    fun tearDown() {
        clearPreferences()
    }

    @Test
    fun saveVerifyAndDeletePin() {
        val manager = PinCodeManager(context)

        manager.savePinCode("1234")

        assertTrue(manager.isPinCodeCorrect("1234"))
        assertFalse(manager.isPinCodeCorrect("0000"))
        manager.clearPinCode()
        assertNull(manager.loadPinCode())
    }

    @Test
    fun successfulLegacyVerificationMigratesTheVerifier() {
        val salt = "legacy-salt"
        val legacyVerifier = Encryptor().getSHA(salt + "1234" + salt, Algorithm.SHA256)
        context.getSharedPreferences("PinCodePrefs", Context.MODE_PRIVATE).edit()
            .putString("pin_code", legacyVerifier)
            .putString("PASSWORD_SALT_PREFERENCE_KEY", salt)
            .putString("ALGORITHM", Algorithm.SHA256.value)
            .commit()
        val manager = PinCodeManager(context)

        assertTrue(manager.isPinCodeCorrect("1234"))
        assertTrue(manager.loadPinCode()?.startsWith("pin-v2:") == true)
    }

    @Test
    fun exhaustedAttemptsRemainLockedAcrossCounterInstances() {
        SettingsManager(context).setMaxPinAttempts(2)
        val counter = AttemptCounter(context)
        counter.resetAttempts()

        counter.decrementAttempts()
        counter.decrementAttempts()
        counter.decrementAttempts()

        assertEquals(0, AttemptCounter(context).getAttempts())
        assertFalse(counter.hasAttemptsRemaining())
    }

    private fun clearPreferences() {
        listOf("PinCodePrefs", "AttemptPrefs", "AppSettings").forEach { name ->
            context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().commit()
        }
    }
}
