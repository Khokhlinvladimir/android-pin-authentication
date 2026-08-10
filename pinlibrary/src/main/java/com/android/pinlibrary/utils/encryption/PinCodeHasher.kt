package com.android.pinlibrary.utils.encryption

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

internal class PinCodeHasher(
    private val algorithm: String = preferredAlgorithm(),
    private val iterations: Int = defaultIterations(algorithm),
    private val saltGenerator: () -> ByteArray = {
        ByteArray(SALT_LENGTH_BYTES).also(SecureRandom()::nextBytes)
    }
) {

    fun create(pinCode: String): String {
        require(pinCode.isNotEmpty()) { "PIN code must not be empty" }
        require(pinCode.all(Char::isDigit)) { "PIN code must contain digits only" }

        val salt = saltGenerator()
        require(salt.size >= MIN_SALT_LENGTH_BYTES) { "PIN salt is too short" }
        val verifier = derive(pinCode, salt, algorithm, iterations)
        return listOf(
            RECORD_VERSION,
            algorithm,
            iterations.toString(),
            salt.toHex(),
            verifier.toHex()
        ).joinToString(SEPARATOR)
    }

    fun verify(pinCode: String, record: String): Boolean {
        val parts = record.split(SEPARATOR)
        if (parts.size != RECORD_PARTS || parts[0] != RECORD_VERSION) return false

        val storedAlgorithm = parts[1]
        if (storedAlgorithm !in SUPPORTED_ALGORITHMS) return false

        val storedIterations = parts[2].toIntOrNull()
            ?.takeIf { it in MIN_ITERATIONS..MAX_ITERATIONS }
            ?: return false
        val salt = parts[3].hexToBytesOrNull()
            ?.takeIf { it.size >= MIN_SALT_LENGTH_BYTES }
            ?: return false
        val expected = parts[4].hexToBytesOrNull() ?: return false

        val actual = runCatching {
            derive(pinCode, salt, storedAlgorithm, storedIterations)
        }.getOrNull() ?: return false
        return MessageDigest.isEqual(expected, actual)
    }

    fun isVersionedRecord(record: String): Boolean = record.startsWith("$RECORD_VERSION$SEPARATOR")

    private fun derive(
        pinCode: String,
        salt: ByteArray,
        algorithm: String,
        iterations: Int
    ): ByteArray {
        val specification = PBEKeySpec(pinCode.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance(algorithm).generateSecret(specification).encoded
        } finally {
            specification.clearPassword()
        }
    }

    private fun ByteArray.toHex(): String = joinToString(separator = "") { byte ->
        "%02x".format(byte.toInt() and 0xff)
    }

    private fun String.hexToBytesOrNull(): ByteArray? {
        if (length % 2 != 0 || any { it.digitToIntOrNull(16) == null }) return null
        return ByteArray(length / 2) { index ->
            substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }

    companion object {
        private const val RECORD_VERSION = "pin-v2"
        private const val SEPARATOR = ":"
        private const val RECORD_PARTS = 5
        private const val SHA256_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val SHA1_ALGORITHM = "PBKDF2WithHmacSHA1"
        private const val SHA256_ITERATIONS = 600_000
        private const val SHA1_ITERATIONS = 1_300_000
        private const val MIN_ITERATIONS = 10_000
        private const val MAX_ITERATIONS = 2_000_000
        private const val KEY_LENGTH_BITS = 256
        private const val SALT_LENGTH_BYTES = 32
        private const val MIN_SALT_LENGTH_BYTES = 16
        private val SUPPORTED_ALGORITHMS = setOf(SHA256_ALGORITHM, SHA1_ALGORITHM)

        private fun preferredAlgorithm(): String = try {
            SecretKeyFactory.getInstance(SHA256_ALGORITHM)
            SHA256_ALGORITHM
        } catch (_: NoSuchAlgorithmException) {
            SHA1_ALGORITHM
        }

        private fun defaultIterations(algorithm: String): Int =
            if (algorithm == SHA256_ALGORITHM) SHA256_ITERATIONS else SHA1_ITERATIONS
    }
}
