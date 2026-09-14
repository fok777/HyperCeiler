package io.github.lingqiqi5211.ezhooktool.core.java

import io.github.lingqiqi5211.ezhooktool.core.findConstructorBestMatch

/** 构造调用。 */
object Constructors {

    @JvmStatic
    fun newInstance(clazz: Class<*>, vararg args: Any?): Any =
        findConstructorBestMatch(clazz, *args.map { it?.javaClass ?: Any::class.java }.toTypedArray())
            .newInstance(*args)
}

    @JvmStatic
    fun find(clazz: Class<*>): ConstructorSearch = ConstructorSearch(clazz)

    class ConstructorSearch(private val clazz: Class<*>) {
        private val filters = mutableListOf<java.lang.reflect.Constructor<*>.() -> Boolean>()

        fun filter(block: java.lang.reflect.Constructor<*>.() -> Boolean): ConstructorSearch {
            filters += block
            return this
        }

        fun filterByParamCount(count: Int): ConstructorSearch = filter { parameterCount == count }

        fun first(): java.lang.reflect.Constructor<*> = toList().firstOrNull()
            ?: throw NoSuchMethodError("Constructor not found in ${'$'}{clazz.name}")

        fun firstOrNull(): java.lang.reflect.Constructor<*>? = toList().firstOrNull()

        fun toList(): List<java.lang.reflect.Constructor<*>> =
            clazz.declaredConstructors.filter { c -> filters.all { c.it() } }
    }
