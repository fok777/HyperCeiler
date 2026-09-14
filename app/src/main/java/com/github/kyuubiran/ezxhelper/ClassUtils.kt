@file:Suppress("unused", "UNCHECKED_CAST")

package com.github.kyuubiran.ezxhelper

import io.github.lingqiqi5211.ezhooktool.core.BestMatchUtils
import io.github.lingqiqi5211.ezhooktool.core.java.Fields
import io.github.lingqiqi5211.ezhooktool.core.java.Methods
import java.lang.reflect.Method

/**
 * EzXHelper ClassUtils 的 API 102 兼容实现。
 */
object ClassUtils {

    internal fun loader(): ClassLoader = EzXHelper.classLoader

    @JvmStatic
    @JvmOverloads
    fun loadClass(name: String, classLoader: ClassLoader = loader()): Class<*> =
        io.github.lingqiqi5211.ezhooktool.core.ClassUtils.loadClass(name, classLoader)

    @JvmStatic
    @JvmOverloads
    fun loadClassOrNull(name: String, classLoader: ClassLoader = loader()): Class<*>? =
        io.github.lingqiqi5211.ezhooktool.core.ClassUtils.loadClassOrNull(name, classLoader)

    @JvmStatic
    fun loadFirstClass(vararg names: String): Class<*> =
        io.github.lingqiqi5211.ezhooktool.core.ClassUtils.loadClassFirst(*names)

    @JvmStatic
    fun loadFirstClass(classLoader: ClassLoader, vararg names: String): Class<*> =
        io.github.lingqiqi5211.ezhooktool.core.ClassUtils.loadClassFirst(classLoader, *names)

    @JvmStatic
    fun loadFirstClassOrNull(vararg names: String): Class<*>? =
        io.github.lingqiqi5211.ezhooktool.core.ClassUtils.loadClassFirstOrNull(*names)

    @JvmStatic
    fun setStaticObject(clazz: Class<*>, fieldName: String, value: Any?) {
        Fields.setStaticObjectField(clazz, fieldName, value)
    }

    @JvmStatic
    fun getStaticObject(clazz: Class<*>, fieldName: String): Any? =
        Fields.getStaticObjectField(clazz, fieldName)

    @JvmStatic
    fun getStaticObjectOrNull(clazz: Class<*>, fieldName: String): Any? =
        runCatching { Fields.getStaticObjectField(clazz, fieldName) }.getOrNull()

    @JvmStatic
    fun <T> getStaticObjectOrNullAs(clazz: Class<*>, fieldName: String): T? =
        getStaticObjectOrNull(clazz, fieldName) as? T

    @JvmStatic
    fun <T> getStaticObjectAs(clazz: Class<*>, fieldName: String): T =
        Fields.getStaticObjectField(clazz, fieldName) as T

    @JvmStatic
    fun invokeStaticMethodBestMatch(clazz: Class<*>, methodName: String, vararg args: Any?): Any? =
        Methods.callStaticMethod(clazz, methodName, *args)

    @JvmStatic
    fun findStaticMethodBestMatch(
        clazz: Class<*>,
        methodName: String,
        vararg parameterTypes: Class<*>
    ): Method = BestMatchUtils.findMethodBestMatch(clazz, methodName, parameterTypes)
}
