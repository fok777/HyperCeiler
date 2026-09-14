@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.github.kyuubiran.ezxhelper

import android.content.Context
import io.github.lingqiqi5211.ezhooktool.xposed.EzXposed

/**
 * EzXHelper 的 API 102 兼容实现。
 */
object EzXHelper {

    /** 当前日志 tag。 */
    var logTag: String = "HyperCeiler"

    /** 当前 toast tag。 */
    var toastTag: String = "HyperCeiler"

    /** 目标进程 application context；尚未就绪时为 null。 */
    val appContext: Context?
        get() = EzXposed.appContextOrNull

    @JvmStatic
    fun getAppContext(): Context? = appContext

    /** 当前目标 ClassLoader。 */
    val classLoader: ClassLoader
        get() = EzXposed.safeClassLoader

    @JvmStatic
    fun getClassLoader(): ClassLoader = classLoader

    /** 当前宿主包名。 */
    val hostPackageName: String
        get() = EzXposed.packageName

    @JvmStatic
    fun getHostPackageName(): String = hostPackageName

    /** 当前进程名。 */
    val processName: String
        get() = EzXposed.processName

    @JvmStatic
    fun setLogTag(tag: String) {
        logTag = tag
    }

    @JvmStatic
    fun setToastTag(tag: String) {
        toastTag = tag
    }

    @JvmStatic
    fun initAppContext(context: Context?) {
        if (context != null) {
            runCatching { EzXposed.initAppContext(context) }
        }
    }

    @JvmStatic
    fun initZygote(startupParam: Any?) {
        // API 102 下由模块入口统一初始化。
    }

    @JvmStatic
    fun initHandleLoadPackage(lpparam: Any?) {
        // API 102 下由 EzHookTool 运行时管理 ClassLoader。
    }
}
