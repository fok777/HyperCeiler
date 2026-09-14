@file:Suppress("unused", "UNCHECKED_CAST")

package com.github.kyuubiran.ezxhelper.finders

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * EzXHelper MethodFinder 的 API 102 兼容实现。
 */
class MethodFinderSeq internal constructor(private val clazz: Class<*>) {

    private var searchSuper = true
    private val filters = mutableListOf<Method.() -> Boolean>()

    fun filter(condition: Method.() -> Boolean): MethodFinderSeq {
        filters += condition
        return this
    }

    fun filterByName(value: String): MethodFinderSeq = filter { name == value }

    fun filterByNameContains(value: String, ignoreCase: Boolean = false): MethodFinderSeq =
        filter { name.contains(value, ignoreCase) }

    fun filterByParamCount(count: Int): MethodFinderSeq = filter { parameterCount == count }

    fun filterByParamCount(range: IntRange): MethodFinderSeq = filter { parameterCount in range }

    fun filterByParamTypes(condition: (Array<Class<*>>) -> Boolean): MethodFinderSeq =
        filter { condition(parameterTypes) }

    fun filterByParamTypes(vararg types: Class<*>): MethodFinderSeq =
        filter { parameterTypes.contentEquals(types) }

    fun filterByAssignableParamTypes(vararg types: Class<*>): MethodFinderSeq =
        filter {
            if (parameterCount != types.size) return@filter false
            types.withIndex().all { (index, type) -> type.isAssignableFrom(parameterTypes[index]) }
        }

    fun filterByReturnType(type: Class<*>): MethodFinderSeq = filter { returnType == type }

    fun filterByReturnType(condition: (Class<*>) -> Boolean): MethodFinderSeq =
        filter { condition(returnType) }

    fun filterStatic(): MethodFinderSeq = filter { Modifier.isStatic(modifiers) }

    fun filterNonStatic(): MethodFinderSeq = filter { !Modifier.isStatic(modifiers) }

    fun filterAbstract(): MethodFinderSeq = filter { Modifier.isAbstract(modifiers) }

    fun filterNonAbstract(): MethodFinderSeq = filter { !Modifier.isAbstract(modifiers) }

    fun filterFinal(): MethodFinderSeq = filter { Modifier.isFinal(modifiers) }

    fun filterNonFinal(): MethodFinderSeq = filter { !Modifier.isFinal(modifiers) }

    fun filterPublic(): MethodFinderSeq = filter { Modifier.isPublic(modifiers) }

    fun filterNonPublic(): MethodFinderSeq = filter { !Modifier.isPublic(modifiers) }

    fun filterPrivate(): MethodFinderSeq = filter { Modifier.isPrivate(modifiers) }

    fun filterProtected(): MethodFinderSeq = filter { Modifier.isProtected(modifiers) }

    /** 只在当前类查找，不向上查找父类。 */
    fun onlySelf(): MethodFinderSeq {
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
        return result.values.filter { method -> filters.all { condition -> method.condition() } }
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
}

/** `Class<*>.methodFinder()` 扩展入口。 */
object MethodFinder {
    object `-Static` {
        fun Class<*>.methodFinder(): MethodFinderSeq = MethodFinderSeq(this)
    }
}

/**
 * EzXHelper ConstructorFinder 的 API 102 兼容实现。
 */
class ConstructorFinderSeq internal constructor(private val clazz: Class<*>) {

    private val filters = mutableListOf<Constructor<*>.() -> Boolean>()

    fun filter(condition: Constructor<*>.() -> Boolean): ConstructorFinderSeq {
        filters += condition
        return this
    }

    fun filterByParamCount(count: Int): ConstructorFinderSeq = filter { parameterCount == count }

    fun filterByParamTypes(condition: (Array<Class<*>>) -> Boolean): ConstructorFinderSeq =
        filter { condition(parameterTypes) }

    fun filterByParamTypes(vararg types: Class<*>): ConstructorFinderSeq =
        filter { parameterTypes.contentEquals(types) }

    fun filterEmptyParam(): ConstructorFinderSeq = filter { parameterCount == 0 }

    fun filterNotEmptyParam(): ConstructorFinderSeq = filter { parameterCount != 0 }

    fun filterPublic(): ConstructorFinderSeq = filter { Modifier.isPublic(modifiers) }

    private fun candidates(): List<Constructor<*>> =
        clazz.declaredConstructors.filter { c -> filters.all { condition -> c.condition() } }

    fun toList(): List<Constructor<*>> = candidates()

    fun first(): Constructor<*> = toList().firstOrNull()
        ?: throw NoSuchMethodError("Constructor not found in ${clazz.name}")

    fun firstOrNull(): Constructor<*>? = toList().firstOrNull()

    fun single(): Constructor<*> = toList().singleOrNull()
        ?: throw NoSuchMethodError("Constructor not found or not single in ${clazz.name}")

    fun singleOrNull(): Constructor<*>? = toList().singleOrNull()

    fun last(): Constructor<*> = toList().lastOrNull()
        ?: throw NoSuchMethodError("Constructor not found in ${clazz.name}")
}

/** `Class<*>.constructorFinder()` 扩展入口。 */
object ConstructorFinder {
    object `-Static` {
        fun Class<*>.constructorFinder(): ConstructorFinderSeq = ConstructorFinderSeq(this)
    }
}

/**
 * EzXHelper FieldFinder 的 API 102 兼容实现。
 */
class FieldFinderSeq internal constructor(private val clazz: Class<*>) {

    private var searchSuper = true
    private val filters = mutableListOf<Field.() -> Boolean>()

    fun filter(condition: Field.() -> Boolean): FieldFinderSeq {
        filters += condition
        return this
    }

    fun filterByName(value: String): FieldFinderSeq = filter { name == value }

    fun filterByType(type: Class<*>): FieldFinderSeq = filter { this.type == type }

    fun filterByAssignableType(type: Class<*>): FieldFinderSeq = filter { type.isAssignableFrom(this.type) }

    fun filterStatic(): FieldFinderSeq = filter { Modifier.isStatic(modifiers) }

    fun filterNonStatic(): FieldFinderSeq = filter { !Modifier.isStatic(modifiers) }

    fun filterFinal(): FieldFinderSeq = filter { Modifier.isFinal(modifiers) }

    fun onlySelf(): FieldFinderSeq {
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
        return result.values.filter { f -> filters.all { condition -> f.condition() } }
    }

    fun toList(): List<Field> = candidates()

    fun first(): Field = toList().firstOrNull()
        ?: throw NoSuchFieldError("Field not found in ${clazz.name}")

    fun firstOrNull(): Field? = toList().firstOrNull()

    fun single(): Field = toList().singleOrNull()
        ?: throw NoSuchFieldError("Field not found or not single in ${clazz.name}")

    fun singleOrNull(): Field? = toList().singleOrNull()
}

/** `Class<*>.fieldFinder()` 扩展入口。 */
object FieldFinder {
    object `-Static` {
        fun Class<*>.fieldFinder(): FieldFinderSeq = FieldFinderSeq(this)
    }
}
