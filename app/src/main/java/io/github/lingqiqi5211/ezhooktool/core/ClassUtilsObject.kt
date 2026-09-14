package io.github.lingqiqi5211.ezhooktool.core

import io.github.lingqiqi5211.ezhooktool.core.java.Fields

/** Java 侧使用的静态入口。 */
object ClassUtils {

    @JvmStatic
    fun loadClass(name: String): Class<*> =
        io.github.lingqiqi5211.ezhooktool.core.loadClass(name, EzReflect.classLoader)

    @JvmStatic
    fun loadClass(name: String, classLoader: ClassLoader): Class<*> =
        io.github.lingqiqi5211.ezhooktool.core.loadClass(name, classLoader)

    @JvmStatic
    fun loadClassOrNull(name: String): Class<*>? =
        io.github.lingqiqi5211.ezhooktool.core.loadClassOrNull(name, EzReflect.classLoader)

    @JvmStatic
    fun loadClassOrNull(name: String, classLoader: ClassLoader): Class<*>? =
        io.github.lingqiqi5211.ezhooktool.core.loadClassOrNull(name, classLoader)

    @JvmStatic
    fun loadFirstClass(vararg names: String): Class<*> =
        loadClassFirst(names = names, classLoader = EzReflect.classLoader)

    @JvmStatic
    fun getStaticObjectOrNull(clazz: Class<*>, fieldName: String): Any? =
        runCatching { Fields.getStaticObjectField(clazz, fieldName) }.getOrNull()
}
