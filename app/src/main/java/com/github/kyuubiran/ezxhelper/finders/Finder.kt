@file:Suppress("unused", "UNCHECKED_CAST")

package com.github.kyuubiran.ezxhelper.finders

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * EzXHelper MethodFinder 的 API 102 兼容实现。
 */
class MethodFinder internal constructor(private val clazz: Class<*>) {

    private var searchSuper = true
    private val filters = mutableListOf<(Method) -> Boolean>()

    fun filter(condition: (Method) -> Boolean): MethodFinder {
        filters += condition
        return this
    }

    fun filterByName(name: String): MethodFinder = filter { it.name == name }

    fun filterByNameContains(value: String, ignoreCase: Boolean = false): MethodFinder =
        filter { it.name.contains(value, ignoreCase) }

    fun filterByParamCount(count: Int): MethodFinder = filter { it.parameterCount == count }

    fun filterByParamCount(range: IntRange): MethodFinder = filter { it.parameterCount in range }

    fun filterByParamTypes(condition: (Array<Class<*>>) -> Boolean): MethodFinder =
        filter { condition(it.parameterTypes) }

    fun filterByParamTypes(vararg types: Class<*>): MethodFinder =
        filter { it.parameterTypes.contentEquals(types) }

    fun filterByAssignableParamTypes(vararg types: Class<*>): MethodFinder =
        filter { method ->
            if (method.parameterCount != types.size) return@filter false
            types.withIndex().all { (index, type) -> type.isAssignableFrom(method.parameterTypes[index]) }
        }

    fun filterByReturnType(type: Class<*>): MethodFinder = filter { it.returnType == type }

    fun filterByReturnType(condition: (Class<*>) -> Boolean): MethodFinder =
        filter { condition(it.returnType) }

    fun filterStatic(): MethodFinder = filter { Modifier.isStatic(it.modifiers) }

    fun filterNonStatic(): MethodFinder = filter { !Modifier.isStatic(it.modifiers) }

    fun filterNonAbstract(): MethodFinder = filter { !Modifier.isAbstract(it.modifiers) }

    fun filterAbstract(): MethodFinder = filter { Modifier.isAbstract(it.modifiers) }

    fun filterFinal(): MethodFinder = filter { Modifier.isFinal(it.modifiers) }

    fun filterNonFinal(): MethodFinder = filter { !Modifier.isFinal(it.modifiers) }

    fun filterPublic(): MethodFinder = filter { Modifier.isPublic(it.modifiers) }

    fun filterNonPublic(): MethodFinder = filter { !Modifier.isPublic(it.modifiers) }

    fun filterPrivate(): MethodFinder = filter { Modifier.isPrivate(it.modifiers) }

    fun filterProtected(): MethodFinder = filter { Modifier.isProtected(it.modifiers) }

    /** 只在当前类查找，不向上查找父类。 */
    fun onlySelf(): MethodFinder {
        searchSuper = false
        return this
    }

    private fun candidates(): List<Method> {
        val result = LinkedHashMap<String, Method>()
        var current: Class<*>? = clazz
        while (current != null) {
            for (method in current.declaredMethods) {
                val key = method.name + method.parameterTypes.joinToString { it.name }
                if (!result.containsKey(key)) result[key] = method
            }
            if (!searchSuper) break
            current = current.superclass
        }
        return result.values.filter { method -> filters.all { it(method) } }
    }

    fun toList(): List<Method> = candidates()

    fun first(): Method = toList().firstOrNull()
        ?: throw NoSuchMethodError("Method not found in ${clazz.name}")

    fun firstOrNull(): Method? = toList().firstOrNull()

    fun single(): Method = toList().singleOrNull()
        ?: throw NoSuchMethodError("Method not found or not single in ${clazz.name}")

    fun singleOrNull(): Method? = toList().singleOrNull()

    fun last(): Method = toList().lastOrNull()
        ?: throw NoSuchMethodError("Method not found in ${clazz.name}")

    fun lastOrNull(): Method? = toList().lastOrNull()

    fun count(): Int = toList().size

    @JvmName("-Static")
    companion object {
        fun Class<*>.methodFinder(): MethodFinder = MethodFinder(this)
    }
}

/**
 * EzXHelper ConstructorFinder 的 API 102 兼容实现。
 */
class ConstructorFinder internal constructor(private val clazz: Class<*>) {

    private val filters = mutableListOf<(Constructor<*>) -> Boolean>()

    fun filter(condition: (Constructor<*>) -> Boolean): ConstructorFinder {
        filters += condition
        return this
    }

    fun filterByParamCount(count: Int): ConstructorFinder = filter { it.parameterCount == count }

    fun filterByParamTypes(condition: (Array<Class<*>>) -> Boolean): ConstructorFinder =
        filter { condition(it.parameterTypes) }

    fun filterByParamTypes(vararg types: Class<*>): ConstructorFinder =
        filter { it.parameterTypes.contentEquals(types) }

    fun filterEmptyParam(): ConstructorFinder = filter { it.parameterCount == 0 }

    fun filterNotEmptyParam(): ConstructorFinder = filter { it.parameterCount != 0 }

    fun filterPublic(): ConstructorFinder = filter { Modifier.isPublic(it.modifiers) }

    private fun candidates(): List<Constructor<*>> =
        clazz.declaredConstructors.filter { c -> filters.all { it(c) } }

    fun toList(): List<Constructor<*>> = candidates()

    fun first(): Constructor<*> = toList().firstOrNull()
        ?: throw NoSuchMethodError("Constructor not found in ${clazz.name}")

    fun firstOrNull(): Constructor<*>? = toList().firstOrNull()

    fun single(): Constructor<*> = toList().singleOrNull()
        ?: throw NoSuchMethodError("Constructor not found or not single in ${clazz.name}")

    fun singleOrNull(): Constructor<*>? = toList().singleOrNull()

    fun last(): Constructor<*> = toList().lastOrNull()
        ?: throw NoSuchMethodError("Constructor not found in ${clazz.name}")

    @JvmName("-Static")
    companion object {
        fun Class<*>.constructorFinder(): ConstructorFinder = ConstructorFinder(this)
    }
}

/**
 * EzXHelper FieldFinder 的 API 102 兼容实现。
 */
class FieldFinder internal constructor(private val clazz: Class<*>) {

    private var searchSuper = true
    private val filters = mutableListOf<(Field) -> Boolean>()

    fun filter(condition: (Field) -> Boolean): FieldFinder {
        filters += condition
        return this
    }

    fun filterByName(name: String): FieldFinder = filter { it.name == name }

    fun filterByType(type: Class<*>): FieldFinder = filter { it.type == type }

    fun filterByAssignableType(type: Class<*>): FieldFinder = filter { type.isAssignableFrom(it.type) }

    fun filterStatic(): FieldFinder = filter { Modifier.isStatic(it.modifiers) }

    fun filterNonStatic(): FieldFinder = filter { !Modifier.isStatic(it.modifiers) }

    fun filterFinal(): FieldFinder = filter { Modifier.isFinal(it.modifiers) }

    fun onlySelf(): FieldFinder {
        searchSuper = false
        return this
    }

    private fun candidates(): List<Field> {
        val result = LinkedHashMap<String, Field>()
        var current: Class<*>? = clazz
        while (current != null) {
            for (field in current.declaredFields) {
                if (!result.containsKey(field.name)) result[field.name] = field
            }
            if (!searchSuper) break
            current = current.superclass
        }
        return result.values.filter { f -> filters.all { it(f) } }
    }

    fun toList(): List<Field> = candidates()

    fun first(): Field = toList().firstOrNull()
        ?: throw NoSuchFieldError("Field not found in ${clazz.name}")

    fun firstOrNull(): Field? = toList().firstOrNull()

    fun single(): Field = toList().singleOrNull()
        ?: throw NoSuchFieldError("Field not found or not single in ${clazz.name}")

    fun singleOrNull(): Field? = toList().singleOrNull()

    @JvmName("-Static")
    companion object {
        fun Class<*>.fieldFinder(): FieldFinder = FieldFinder(this)
    }
}

