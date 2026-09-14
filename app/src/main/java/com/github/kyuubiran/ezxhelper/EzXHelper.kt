@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.github.kyuubiran.ezxhelper

import android.content.Context
import io.github.lingqiqi5211.ezhooktool.xposed.EzXposed

/**
 * EzXHelper 的 API 102 兼容实现。
 */
object EzXHelper {

    @set:JvmStatic
    var logTag: String = "HyperCeiler"

    @set:JvmStatic
    var toastTag: String = "HyperCeiler"

    @get:JvmStatic
    val appContext: Context
        get() = EzXposed.appContext

    @get:JvmStatic
    val classLoader: ClassLoader
        get() = EzXposed.safeClassLoader

    @get:JvmStatic
    val hostPackageName: String
        get() = EzXposed.packageName

    @get:JvmStatic
    val processName: String
        get() = EzXposed.processName

    @get:JvmStatic
    val isHostPackageNameInited: Boolean
        get() = EzXposed.packageName.isNotEmpty()

    @JvmStatic
    fun initAppContext(context: Context?) {
        if (context != null) runCatching { EzXposed.initAppContext(context) }
    }

    /** 兼容旧签名：第二参数原用于是否强制刷新，API 102 下忽略。 */
    @JvmStatic
    fun initAppContext(context: Context?, force: Boolean) {
        initAppContext(context)
    }

    /** API 102 下由模块入口统一初始化，保留空实现以兼容旧调用。 */
    @JvmStatic
    fun initZygote(startupParam: Any?) = Unit

    /** API 102 下 ClassLoader 由运行时管理，保留空实现以兼容旧调用。 */
    @JvmStatic
    fun initHandleLoadPackage(lpparam: Any?) = Unit
}
