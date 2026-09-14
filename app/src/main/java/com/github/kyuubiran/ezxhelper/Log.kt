@file:Suppress("unused")

package com.github.kyuubiran.ezxhelper

import android.util.Log as AndroidLog

/**
 * EzXHelper Log 的 API 102 兼容实现。
 */
object Log {

    private const val TAG = "EzXHelper"

    @JvmStatic
    fun d(msg: Any?) {
        AndroidLog.d(TAG, msg.toString())
    }

    @JvmStatic
    fun d(tag: String, msg: Any?) {
        AndroidLog.d(tag, msg.toString())
    }

    @JvmStatic
    fun i(msg: Any?) {
        AndroidLog.i(TAG, msg.toString())
    }

    @JvmStatic
    fun i(tag: String, msg: Any?) {
        AndroidLog.i(tag, msg.toString())
    }

    @JvmStatic
    fun w(msg: Any?) {
        AndroidLog.w(TAG, msg.toString())
    }

    @JvmStatic
    fun w(tag: String, msg: Any?) {
        AndroidLog.w(tag, msg.toString())
    }

    @JvmStatic
    fun e(msg: Any?) {
        AndroidLog.e(TAG, msg.toString())
    }

    @JvmStatic
    fun e(tag: String, msg: Any?) {
        AndroidLog.e(tag, msg.toString())
    }

    @JvmStatic
    fun ex(tr: Throwable?) {
        AndroidLog.e(TAG, AndroidLog.getStackTraceString(tr))
    }

    @JvmStatic
    fun ex(tag: String, tr: Throwable?) {
        AndroidLog.e(tag, AndroidLog.getStackTraceString(tr))
    }

    @JvmStatic
    fun e(tr: Throwable?) {
        AndroidLog.e(TAG, AndroidLog.getStackTraceString(tr))
    }

    @JvmStatic
    fun e(tag: String, msg: Any?, tr: Throwable?) {
        AndroidLog.e(tag, msg.toString(), tr)
    }
}
