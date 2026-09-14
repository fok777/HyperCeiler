package com.github.kyuubiran.ezxhelper.interfaces

import androidx.annotation.NonNull
import com.github.kyuubiran.ezxhelper.MethodHookParam

/**
 * EzXHelper IMethodHookCallback 的 API 102 兼容实现。
 */
fun interface IMethodHookCallback {
    fun onMethodHooked(@NonNull param: MethodHookParam)
}
