package com.android.pinlibrary.utils.encryption

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey

/** Fast PIN verification backed by a non-exportable Android Keystore HMAC key. */
internal class PinCodeProtector {

    fun create(pinCode: String): String {
        validate(pinCode)
        return "$RECORD_VERSION$SEPARATOR${sign(pinCode).encode()}"
    }

    fun verify(pinCode: String, record: String): Boolean {
        if (!isVersionedRecord(record)) return false
        val expected = record.substringAfter(SEPARATOR).decodeOrNull() ?: return false
        val actual = runCatching { sign(pinCode) }.getOrNull() ?: return false
        return MessageDigest.isEqual(expected, actual)
    }

    fun isVersionedRecord(record: String): Boolean =
        record.startsWith("$RECORD_VERSION$SEPARATOR")

    fun deleteKey() {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) keyStore.deleteEntry(KEY_ALIAS)
    }

    private fun sign(pinCode: String): ByteArray {
        validate(pinCode)
        val bytes = pinCode.toByteArray(Charsets.UTF_8)
        return try {
            Mac.getInstance(HMAC_ALGORITHM).run {
                init(getOrCreateKey())
                doFinal(bytes)
            }
        } finally {
            bytes.fill(0)
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_HMAC_SHA256,
            KEYSTORE_PROVIDER
        )
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setDigests(KeyProperties.DIGEST_SHA256)
                .build()
        )
        return generator.generateKey()
    }

    private fun validate(pinCode: String) {
        require(pinCode.isNotEmpty()) { "PIN code must not be empty" }
        require(pinCode.all(Char::isDigit)) { "PIN code must contain digits only" }
    }

    private fun ByteArray.encode(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    private fun String.decodeOrNull(): ByteArray? = runCatching {
        Base64.decode(this, Base64.NO_WRAP)
    }.getOrNull()

    private companion object {
        const val RECORD_VERSION = "pin-v3"
        const val SEPARATOR = ":"
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val KEY_ALIAS = "com.android.pinlibrary.pin-hmac-v1"
        const val HMAC_ALGORITHM = "HmacSHA256"
    }
}
