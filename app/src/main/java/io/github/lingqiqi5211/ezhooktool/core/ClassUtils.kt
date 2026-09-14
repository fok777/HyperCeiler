@file:JvmName("ClassUtilsKt")

package io.github.lingqiqi5211.ezhooktool.core

import java.lang.reflect.Constructor
import java.lang.reflect.Method

/** 当前默认 ClassLoader，由模块入口写入。 */
object EzReflect {

    @JvmStatic
    var classLoader: ClassLoader = HookRuntimeHolder.classLoader()

    @JvmStatic
    val safeClassLoader: ClassLoader
        get() = runCatching { classLoader }.getOrNull() ?: ClassLoader.getSystemClassLoader()
}

/** 避免与 compat 包循环依赖的内部持有者。 */
internal object HookRuntimeHolder {
    fun classLoader(): ClassLoader = com.sevtinge.hyperceiler.compat.HookRuntime.classLoader()
}

class ClassNotFoundError(message: String) : Error(message)

fun loadClass(name: String, classLoader: ClassLoader = EzReflect.classLoader): Class<*> =
    loadClassOrNull(name, classLoader) ?: throw ClassNotFoundError(name)

fun loadClassOrNull(name: String, classLoader: ClassLoader = EzReflect.classLoader): Class<*>? =
    runCatching { Class.forName(name, false, classLoader) }.getOrNull()

fun loadClassFirst(vararg names: String, classLoader: ClassLoader = EzReflect.classLoader): Class<*> =
    loadClassFirstOrNull(names = names, classLoader = classLoader)
        ?: throw ClassNotFoundError(names.firstOrNull() ?: "<empty>")

fun loadClassFirstOrNull(
    vararg names: String,
    classLoader: ClassLoader = EzReflect.classLoader
): Class<*>? {
    for (name in names) {
        loadClassOrNull(name, classLoader)?.let { return it }
    }
    return null
}

fun String.toClass(classLoader: ClassLoader = EzReflect.classLoader): Class<*> =
    loadClass(this, classLoader)

fun String.toClassOrNull(classLoader: ClassLoader = EzReflect.classLoader): Class<*>? =
    loadClassOrNull(this, classLoader)

fun findMethodBestMatch(
    clz: Class<*>,
    methodName: String,
    vararg parameterTypes: Class<*>
): Method {
    exactMethod(clz, methodName, parameterTypes)?.let { return it }
    val candidates = allMethods(clz).filter { it.name == methodName }
    if (parameterTypes.isEmpty()) {
        candidates.firstOrNull()?.let { it.isAccessible = true; return it }
    }
    candidates.firstOrNull { m ->
        m.parameterCount == parameterTypes.size &&
            parameterTypes.withIndex().all { (i, t) -> isAssignable(t, m.parameterTypes[i]) }
    }?.let { it.isAccessible = true; return it }
    throw NoSuchMethodError("$methodName not found in ${clz.name}")
}

fun findMethodBestMatch(
    clz: Class<*>,
    methodName: String,
    vararg args: Any?
): Method = findMethodBestMatch(clz, methodName, *args.map { it?.javaClass ?: Any::class.java }.toTypedArray())

fun findConstructorBestMatch(clz: Class<*>, vararg parameterTypes: Class<*>): Constructor<*> {
    runCatching { clz.getDeclaredConstructor(*parameterTypes) }.getOrNull()
        ?.let { it.isAccessible = true; return it }
    clz.declaredConstructors.firstOrNull { c ->
        c.parameterCount == parameterTypes.size &&
            parameterTypes.withIndex().all { (i, t) -> isAssignable(t, c.parameterTypes[i]) }
    }?.let { it.isAccessible = true; return it }
    throw NoSuchMethodError("constructor not found in ${clz.name}")
}

internal fun exactMethod(clz: Class<*>, name: String, types: Array<out Class<*>>): Method? =
    runCatching { clz.getDeclaredMethod(name, *types) }.getOrNull()
        ?.also { it.isAccessible = true }
        ?: runCatching { clz.getMethod(name, *types) }.getOrNull()

internal fun allMethods(clz: Class<*>): List<Method> {
    val result = LinkedHashMap<String, Method>()
    var current: Class<*>? = clz
    while (current != null) {
        current.declaredMethods.forEach { m ->
            result.putIfAbsent(m.name + m.parameterTypes.joinToString { it.name }, m)
        }
        current = current.superclass
    }
    return result.values.toList()
}

internal fun isAssignable(expected: Class<*>, actual: Class<*>): Boolean =
    expected == actual || expected.isAssignableFrom(actual) ||
        (expected.isPrimitive && primitiveOf(actual) == expected) ||
        (!expected.isPrimitive && primitiveOf(expected) == actual)

internal fun primitiveOf(clazz: Class<*>): Class<*>? = when (clazz) {
    java.lang.Boolean::class.java -> Boolean::class.javaPrimitiveType
    java.lang.Byte::class.java -> Byte::class.javaPrimitiveType
    java.lang.Short::class.java -> Short::class.javaPrimitiveType
    java.lang.Integer::class.java -> Int::class.javaPrimitiveType
    java.lang.Long::class.java -> Long::class.javaPrimitiveType
    java.lang.Float::class.java -> Float::class.javaPrimitiveType
    java.lang.Double::class.java -> Double::class.javaPrimitiveType
    java.lang.Character::class.java -> Char::class.javaPrimitiveType
    else -> null
}
