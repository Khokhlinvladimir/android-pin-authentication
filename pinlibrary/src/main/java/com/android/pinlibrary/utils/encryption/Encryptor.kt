package com.android.pinlibrary.utils.encryption

import com.android.pinlibrary.utils.encryption.enums.Algorithm
import java.security.MessageDigest
import java.util.Locale

class Encryptor {

    fun getSHA(text: String?, algorithm: Algorithm): String {
        val sha = ""
        if (text.isNullOrEmpty()) {
            return sha
        }

        val shaDigest = getShaDigest(algorithm)

        if (shaDigest != null) {
            val textBytes = text.toByteArray(Charsets.UTF_8)
            shaDigest.update(textBytes, 0, textBytes.size)
            val shaHash = shaDigest.digest()
            return bytesToHex(shaHash)
        }

        return ""
    }

    private companion object {

        private fun getShaDigest(algorithm: Algorithm): MessageDigest? {
            return try {
                when (algorithm) {
                    Algorithm.SHA256 -> MessageDigest.getInstance("SHA-256")
                    else -> MessageDigest.getInstance("SHA-1")
                }
            } catch (e: Exception) {
                null
            }
        }

        private fun bytesToHex(bytes: ByteArray): String {
            val stringBuilder = StringBuilder()
            var stringTemp: String
            for (aByte in bytes) {
                stringTemp = (Integer.toHexString(aByte.toInt() and 0xFF))
                if (stringTemp.length == 1) {
                    stringBuilder.append("0").append(stringTemp)
                } else {
                    stringBuilder.append(stringTemp)
                }
            }
            return stringBuilder.toString().lowercase(Locale.ENGLISH)
        }
    }
}