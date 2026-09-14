@file:Suppress("unused", "UNCHECKED_CAST")

package com.github.kyuubiran.ezxhelper

import io.github.lingqiqi5211.ezhooktool.core.findMethodBestMatch as coreFindMethodBestMatch
import io.github.lingqiqi5211.ezhooktool.core.loadClass as coreLoadClass
import io.github.lingqiqi5211.ezhooktool.core.loadClassFirst as coreLoadClassFirst
import io.github.lingqiqi5211.ezhooktool.core.loadClassFirstOrNull as coreLoadClassFirstOrNull
import io.github.lingqiqi5211.ezhooktool.core.loadClassOrNull as coreLoadClassOrNull
import io.github.lingqiqi5211.ezhooktool.core.java.Fields
import io.github.lingqiqi5211.ezhooktool.core.java.Methods

/**
 * EzXHelper ClassUtils 的 API 102 兼容实现。
 */
object ClassUtils {

    @JvmStatic
    @JvmOverloads
    fun loadClass(name: String, classLoader: ClassLoader = EzXHelper.classLoader): Class<*> =
        coreLoadClass(name, classLoader)

    @JvmStatic
    @JvmOverloads
    fun loadClassOrNull(name: String, classLoader: ClassLoader = EzXHelper.classLoader): Class<*>? =
        coreLoadClassOrNull(name, classLoader)

    @JvmStatic
    fun loadFirstClass(vararg names: String): Class<*> =
        coreLoadClassFirst(names = names, classLoader = EzXHelper.classLoader)

    @JvmStatic
    fun loadFirstClass(classLoader: ClassLoader, vararg names: String): Class<*> =
        coreLoadClassFirst(names = names, classLoader = classLoader)

    @JvmStatic
    fun loadFirstClassOrNull(vararg names: String): Class<*>? =
        coreLoadClassFirstOrNull(names = names, classLoader = EzXHelper.classLoader)

    @JvmStatic
    fun setStaticObject(clazz: Class<*>, fieldName: String, value: Any?) =
        Fields.setStaticObjectField(clazz, fieldName, value)

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
    ): java.lang.reflect.Method = coreFindMethodBestMatch(clazz, methodName, *parameterTypes)
}
