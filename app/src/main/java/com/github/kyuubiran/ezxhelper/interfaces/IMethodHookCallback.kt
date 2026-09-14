package com.github.kyuubiran.ezxhelper.interfaces

import androidx.annotation.NonNull
import com.sevtinge.hyperceiler.compat.XC_MethodHook

/**
 * EzXHelper IMethodHookCallback 的 API 102 兼容实现。
 *
 * <p>参数类型使用 legacy [XC_MethodHook.MethodHookParam]，这样 Java 侧无论声明父类还是
 * 子类参数都能正确覆盖。</p>
 */
fun interface IMethodHookCallback {
    fun onMethodHooked(@NonNull param: XC_MethodHook.MethodHookParam)
}
