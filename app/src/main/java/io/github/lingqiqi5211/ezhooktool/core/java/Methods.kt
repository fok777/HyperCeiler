package io.github.lingqiqi5211.ezhooktool.core.java

import io.github.lingqiqi5211.ezhooktool.core.findMethodBestMatch
import java.lang.reflect.Method

/** 方法调用。 */
object Methods {

    @JvmStatic
    fun callMethod(obj: Any, methodName: String, vararg args: Any?): Any? =
        findMethodBestMatch(obj.javaClass, methodName, *args).invoke(obj, *args)

    @JvmStatic
    fun <T> callMethodAs(obj: Any, methodName: String, vararg args: Any?): T =
        callMethod(obj, methodName, *args) as T

    @JvmStatic
    fun callStaticMethod(clazz: Class<*>, methodName: String, vararg args: Any?): Any? =
        findMethodBestMatch(clazz, methodName, *args).invoke(null, *args)

    @JvmStatic
    fun <T> callStaticMethodAs(clazz: Class<*>, methodName: String, vararg args: Any?): T =
        callStaticMethod(clazz, methodName, *args) as T

    @JvmStatic
    fun find(clazz: Class<*>): MethodSearch = MethodSearch(clazz)

    class MethodSearch(private val clazz: Class<*>) {
        private val filters = mutableListOf<Method.() -> Boolean>()

        fun filter(block: Method.() -> Boolean): MethodSearch {
            filters += block
            return this
        }

        fun filterByName(value: String): MethodSearch = filter { name == value }

        fun filterByParamTypes(vararg types: Class<*>): MethodSearch =
            filter { parameterTypes.contentEquals(types) }

        fun filterByParamCount(count: Int): MethodSearch = filter { parameterCount == count }

        fun first(): Method = toList().firstOrNull()
            ?: throw NoSuchMethodError("Method not found in ${clazz.name}")

        fun firstOrNull(): Method? = toList().firstOrNull()

        fun single(): Method = toList().singleOrNull()
            ?: throw NoSuchMethodError("Method not found or not single in ${clazz.name}")

        fun toList(): List<Method> {
            val result = LinkedHashMap<String, Method>()
            var current: Class<*>? = clazz
            while (current != null) {
                current.declaredMethods.forEach { m ->
                    result.putIfAbsent(m.name + m.parameterTypes.joinToString { it.name }, m)
                }
                current = current.superclass
            }
            return result.values.filter { m -> filters.all { m.it() } }
        }
    }
}
