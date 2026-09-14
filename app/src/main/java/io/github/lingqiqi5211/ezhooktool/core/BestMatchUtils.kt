package io.github.lingqiqi5211.ezhooktool.core

import java.lang.reflect.Constructor
import java.lang.reflect.Method

/** Java 侧使用的静态入口。 */
object BestMatchUtils {

    @JvmStatic
    fun findMethodBestMatch(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>): Method =
        io.github.lingqiqi5211.ezhooktool.core.findMethodBestMatch(clazz, methodName, *parameterTypes)

    @JvmStatic
    fun findMethodBestMatch(clazz: Class<*>, methodName: String, vararg args: Any?): Method =
        io.github.lingqiqi5211.ezhooktool.core.findMethodBestMatch(clazz, methodName, *args)

    @JvmStatic
    fun findConstructorBestMatch(clazz: Class<*>, vararg parameterTypes: Class<*>): Constructor<*> =
        io.github.lingqiqi5211.ezhooktool.core.findConstructorBestMatch(clazz, *parameterTypes)
}
