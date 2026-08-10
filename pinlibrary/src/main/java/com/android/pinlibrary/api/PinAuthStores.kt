package com.android.pinlibrary.api

interface PinCredentialStore {
    suspend fun hasPin(): Boolean
    suspend fun save(pin: CharArray)
    suspend fun verify(pin: CharArray): Boolean
    suspend fun delete()
}

interface PinAttemptStore {
    fun remainingAttempts(): Int
    fun decrement(): Int
    fun reset()
}
