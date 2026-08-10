package com.android.pinlibrary.utils.encryption

import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PinCodeHasherTest {

    private val hasher = PinCodeHasher(
        algorithm = "PBKDF2WithHmacSHA256",
        iterations = 10_000,
        saltGenerator = { ByteArray(16) { index -> index.toByte() } }
    )

    @Test
    fun createdVerifierAcceptsOnlyTheOriginalPin() {
        val verifier = hasher.create("1234")

        assertTrue(hasher.verify("1234", verifier))
        assertFalse(hasher.verify("1235", verifier))
        assertFalse(verifier.contains("1234"))
    }

    @Test
    fun malformedOrUnsupportedVerifierIsRejected() {
        assertFalse(hasher.verify("1234", ""))
        assertFalse(hasher.verify("1234", "pin-v2:unknown:10000:00112233445566778899aabbccddeeff:00"))
        assertFalse(hasher.verify("1234", "pin-v2:PBKDF2WithHmacSHA256:999999999:00:00"))
    }

    @Test
    fun onlyNonEmptyNumericPinsCanBeStored() {
        assertThrows(IllegalArgumentException::class.java) { hasher.create("") }
        assertThrows(IllegalArgumentException::class.java) { hasher.create("12a4") }
    }
}
