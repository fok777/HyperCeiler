@file:Suppress("unused", "UNCHECKED_CAST")

package com.github.kyuubiran.ezxhelper

import io.github.lingqiqi5211.ezhooktool.core.BestMatchUtils
import io.github.lingqiqi5211.ezhooktool.core.java.Fields
import io.github.lingqiqi5211.ezhooktool.core.java.Methods

/**
 * EzXHelper ObjectHelper 的 API 102 兼容实现。
 */
class ObjectHelper(private val obj: Any?) {

    fun getObject(fieldName: String): Any? =
        Fields.getObjectField(obj, fieldName)

    fun getObjectOrNull(fieldName: String): Any? =
        runCatching { Fields.getObjectField(obj, fieldName) }.getOrNull()

    fun <T> getObjectOrNullAs(fieldName: String): T? =
        getObjectOrNull(fieldName) as? T

    fun <T> getObjectAs(fieldName: String): T =
        Fields.getObjectField(obj, fieldName) as T

    fun setObject(fieldName: String, value: Any?) {
        Fields.setObjectField(obj, fieldName, value)
    }

    fun setObjectUntilSuperclass(fieldName: String, value: Any?) {
        var current: Class<*>? = obj?.javaClass
        while (current != null) {
            val field = runCatching { current!!.getDeclaredField(fieldName) }.getOrNull()
            if (field != null) {
                field.isAccessible = true
                runCatching { field.set(obj, value) }
                return
            }
            current = current.superclass
        }
    }

    fun getObjectUntilSuperclass(fieldName: String): Any? {
        var current: Class<*>? = obj?.javaClass
        while (current != null) {
            val field = runCatching { current!!.getDeclaredField(fieldName) }.getOrNull()
            if (field != null) {
                field.isAccessible = true
                return runCatching { field.get(obj) }.getOrNull()
            }
            current = current.superclass
        }
        return null
    }

    fun invokeMethodBestMatch(methodName: String, vararg args: Any?): Any? =
        Methods.callMethod(obj, methodName, *args)

    fun invokeMethod(methodName: String, vararg args: Any?): Any? =
        Methods.callMethod(obj, methodName, *args)

    fun newInstance(vararg args: Any?): Any? {
        val clazz = obj as? Class<*> ?: return null
        return io.github.lingqiqi5211.ezhooktool.core.java.Constructors.newInstance(clazz, *args)
    }

    companion object {
        fun Any?.objectHelper(): ObjectHelper = ObjectHelper(this)
    }
}

/**
 * EzXHelper ObjectUtils 的 API 102 兼容实现。
 */
object ObjectUtils {

    @JvmStatic
    fun invokeMethodBestMatch(obj: Any?, methodName: String, vararg args: Any?): Any? =
        Methods.callMethod(obj, methodName, *args)

    @JvmStatic
    fun invokeMethodBestMatch(clazz: Class<*>, methodName: String, vararg args: Any?): Any? =
        Methods.callStaticMethod(clazz, methodName, *args)

    @JvmStatic
    fun setObject(obj: Any?, fieldName: String, value: Any?) {
        Fields.setObjectField(obj, fieldName, value)
    }

    @JvmStatic
    fun getObject(obj: Any?, fieldName: String): Any? =
        Fields.getObjectField(obj, fieldName)

    @JvmStatic
    fun getObjectOrNull(obj: Any?, fieldName: String): Any? =
        runCatching { Fields.getObjectField(obj, fieldName) }.getOrNull()

    @JvmStatic
    fun <T> getObjectOrNullAs(obj: Any?, fieldName: String): T? =
        getObjectOrNull(obj, fieldName) as? T

    @JvmStatic
    fun findMethodBestMatch(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>): java.lang.reflect.Method =
        BestMatchUtils.findMethodBestMatch(clazz, methodName, parameterTypes)
}

/** `Any.invokeMethodBestMatch()` 扩展入口。 */
fun Any?.invokeMethodBestMatch(methodName: String, vararg args: Any?): Any? =
    Methods.callMethod(this, methodName, *args)

/** `Any.setObject()` 扩展入口。 */
fun Any?.setObject(fieldName: String, value: Any?) {
    Fields.setObjectField(this, fieldName, value)
}

/** `Any.getObjectOrNull()` 扩展入口。 */
fun Any?.getObjectOrNull(fieldName: String): Any? =
    runCatching { Fields.getObjectField(this, fieldName) }.getOrNull()

/** `Any.getObjectOrNullAs()` 扩展入口。 */
fun <T> Any?.getObjectOrNullAs(fieldName: String): T? =
    getObjectOrNull(fieldName) as? T
