@file:Suppress("unused", "UNCHECKED_CAST")

package com.github.kyuubiran.ezxhelper

import io.github.lingqiqi5211.ezhooktool.core.findMethodBestMatch as coreFindMethodBestMatch
import io.github.lingqiqi5211.ezhooktool.core.java.Fields
import io.github.lingqiqi5211.ezhooktool.core.java.Methods
import java.lang.reflect.Field

/**
 * EzXHelper ObjectHelper 的 API 102 兼容实现。
 */
class ObjectHelper(private val obj: Any?) {

    fun getObject(fieldName: String): Any? = Fields.getObjectField(require(obj), fieldName)

    fun getObjectOrNull(fieldName: String): Any? =
        runCatching { Fields.getObjectField(require(obj), fieldName) }.getOrNull()

    fun <T> getObjectOrNullAs(fieldName: String): T? = getObjectOrNull(fieldName) as? T

    fun <T> getObjectAs(fieldName: String): T = Fields.getObjectField(require(obj), fieldName) as T

    fun setObject(fieldName: String, value: Any?) = Fields.setObjectField(require(obj), fieldName, value)

    fun getObjectUntilSuperclass(fieldName: String): Any? = findFieldUntilSuperclass(fieldName)?.let {
        runCatching { it.get(obj) }.getOrNull()
    }

    fun getObjectOrNullUntilSuperclass(fieldName: String): Any? = getObjectUntilSuperclass(fieldName)

    fun <T> getObjectOrNullUntilSuperclassAs(fieldName: String): T? =
        getObjectUntilSuperclass(fieldName) as? T

    fun setObjectUntilSuperclass(fieldName: String, value: Any?) {
        val field = findFieldUntilSuperclass(fieldName) ?: return
        runCatching { field.set(obj, value) }
    }

    fun invokeMethodBestMatch(methodName: String, vararg args: Any?): Any? =
        Methods.callMethod(require(obj), methodName, *args)

    fun invokeMethod(methodName: String, vararg args: Any?): Any? =
        Methods.callMethod(require(obj), methodName, *args)

    fun newInstance(vararg args: Any?): Any? {
        val clazz = obj as? Class<*> ?: return null
        return io.github.lingqiqi5211.ezhooktool.core.java.Constructors.newInstance(clazz, *args)
    }

    private fun findFieldUntilSuperclass(fieldName: String): Field? {
        var current: Class<*>? = obj?.javaClass
        while (current != null) {
            val field = runCatching { current!!.getDeclaredField(fieldName) }.getOrNull()
            if (field != null) {
                field.isAccessible = true
                return field
            }
            current = current.superclass
        }
        return null
    }

    private fun require(value: Any?): Any =
        value ?: throw NullPointerException("ObjectHelper: target object is null.")

    companion object {
        fun Any?.objectHelper(): ObjectHelper = ObjectHelper(this)
    }
}

/**
 * EzXHelper ObjectUtils 的 API 102 兼容实现。
 */
object ObjectUtils {

    @JvmStatic
    fun getObject(obj: Any?, fieldName: String): Any? =
        Fields.getObjectField(obj ?: return null, fieldName)

    @JvmStatic
    fun getObjectOrNull(obj: Any?, fieldName: String): Any? =
        if (obj == null) null else runCatching { Fields.getObjectField(obj, fieldName) }.getOrNull()

    @JvmStatic
    fun <T> getObjectOrNullAs(obj: Any?, fieldName: String): T? = getObjectOrNull(obj, fieldName) as? T

    @JvmStatic
    fun <T> getObjectAs(obj: Any?, fieldName: String): T =
        Fields.getObjectField(obj ?: throw NullPointerException("obj is null"), fieldName) as T

    @JvmStatic
    fun setObject(obj: Any?, fieldName: String, value: Any?) {
        if (obj == null) return
        Fields.setObjectField(obj, fieldName, value)
    }

    @JvmStatic
    fun invokeMethodBestMatch(obj: Any?, methodName: String, vararg args: Any?): Any? =
        Methods.callMethod(obj ?: throw NullPointerException("obj is null"), methodName, *args)

    @JvmStatic
    fun invokeMethodBestMatch(clazz: Class<*>, methodName: String, vararg args: Any?): Any? =
        Methods.callStaticMethod(clazz, methodName, *args)

    @JvmStatic
    fun findMethodBestMatch(
        clazz: Class<*>,
        methodName: String,
        vararg parameterTypes: Class<*>
    ): java.lang.reflect.Method = coreFindMethodBestMatch(clazz, methodName, *parameterTypes)
}
