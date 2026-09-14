package io.github.lingqiqi5211.ezhooktool.core

/** 默认 ClassLoader 读取。 */
object EzClassLoader {

    @JvmStatic
    fun current(): ClassLoader = com.sevtinge.hyperceiler.compat.HookRuntime.classLoader()
}
