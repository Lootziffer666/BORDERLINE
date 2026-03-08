package de.lootz.borderline.core

import android.util.Log

object BorderlineLogger {
    private const val TAG = "Borderline"

    fun i(message: String) = Log.i(TAG, message)
    fun w(message: String) = Log.w(TAG, message)
    fun e(message: String, throwable: Throwable? = null) = Log.e(TAG, message, throwable)
}
