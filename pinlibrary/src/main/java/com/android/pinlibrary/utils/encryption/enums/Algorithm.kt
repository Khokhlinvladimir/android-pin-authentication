package com.android.pinlibrary.utils.encryption.enums

enum class Algorithm(val value: String) {
    SHA1("1"), SHA256("2");

    companion object {
        @JvmStatic
        fun getFromText(text: String): Algorithm {
            for (algorithm in values()) {
                if (algorithm.value == text) {
                    return algorithm
                }
            }
            return SHA1
        }
    }
}