package io.github.lingqiqi5211.ezhooktool.core.java

import io.github.lingqiqi5211.ezhooktool.core.findConstructorBestMatch
import java.lang.reflect.Constructor

/** 构造调用（兼容 EzHookTool 的静态方法命名）。 */
object Constructors {

    @JvmStatic
    fun newInstance(clazz: Class<*>, vararg args: Any?): Any =
        findConstructorBestMatch(clazz, *args.map { it?.javaClass ?: Any::class.java }.toTypedArray())
            .newInstance(*args)

    @JvmStatic
    fun find(clazz: Class<*>): ConstructorSearch = ConstructorSearch(clazz)

    class ConstructorSearch(private val clazz: Class<*>) {

        private val filters = mutableListOf<Constructor<*>.() -> Boolean>()

        fun filter(block: Constructor<*>.() -> Boolean): ConstructorSearch {
            filters += block
            return this
        }

        fun filterByParamCount(count: Int): ConstructorSearch = filter { parameterCount == count }

        fun filterByParamTypes(vararg types: Class<*>): ConstructorSearch =
            filter { parameterTypes.contentEquals(types) }

        fun first(): Constructor<*> = toList().firstOrNull()
            ?: throw NoSuchMethodError("Constructor not found in ${clazz.name}")

        fun firstOrNull(): Constructor<*>? = toList().firstOrNull()

        fun single(): Constructor<*> = toList().singleOrNull()
            ?: throw NoSuchMethodError("Constructor not found or not single in ${clazz.name}")

        fun toList(): List<Constructor<*>> =
            clazz.declaredConstructors.filter { c -> filters.all { condition -> c.condition() } }
    }
}
