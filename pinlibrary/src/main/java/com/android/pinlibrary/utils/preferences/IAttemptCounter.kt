package com.android.pinlibrary.utils.preferences

interface IAttemptCounter {
    fun saveAttempts(attempts: Int)
    fun getAttempts(): Int
    fun decrementAttempts()
    fun resetAttempts()
}
