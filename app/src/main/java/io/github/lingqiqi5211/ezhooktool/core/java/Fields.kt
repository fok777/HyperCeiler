package io.github.lingqiqi5211.ezhooktool.core.java

import java.lang.reflect.Field

/** 字段读写（兼容 EzHookTool 的静态方法命名）。 */
object Fields {

    @JvmStatic
    fun find(clazz: Class<*>): FieldSearch = FieldSearch(clazz)

    @JvmStatic
    fun getObjectField(obj: Any, fieldName: String): Any? = findField(obj.javaClass, fieldName)?.get(obj)

    @JvmStatic
    fun setObjectField(obj: Any, fieldName: String, value: Any?) {
        findField(obj.javaClass, fieldName)?.set(obj, value)
    }

    @JvmStatic
    fun getStaticObjectField(clazz: Class<*>, fieldName: String): Any? = findField(clazz, fieldName)?.get(null)

    @JvmStatic
    fun setStaticObjectField(clazz: Class<*>, fieldName: String, value: Any?) {
        findField(clazz, fieldName)?.set(null, value)
    }

    @JvmStatic
    fun getBooleanField(obj: Any, fieldName: String): Boolean = getObjectField(obj, fieldName) as? Boolean ?: false

    @JvmStatic
    fun setBooleanField(obj: Any, fieldName: String, value: Boolean) = setObjectField(obj, fieldName, value)

    @JvmStatic
    fun getIntField(obj: Any, fieldName: String): Int = getObjectField(obj, fieldName) as? Int ?: 0

    @JvmStatic
    fun setIntField(obj: Any, fieldName: String, value: Int) = setObjectField(obj, fieldName, value)

    @JvmStatic
    fun getLongField(obj: Any, fieldName: String): Long = getObjectField(obj, fieldName) as? Long ?: 0L

    @JvmStatic
    fun setLongField(obj: Any, fieldName: String, value: Long) = setObjectField(obj, fieldName, value)

    @JvmStatic
    fun getFloatField(obj: Any, fieldName: String): Float = getObjectField(obj, fieldName) as? Float ?: 0f

    @JvmStatic
    fun setFloatField(obj: Any, fieldName: String, value: Float) = setObjectField(obj, fieldName, value)

    @JvmStatic
    fun getStaticBooleanField(clazz: Class<*>, fieldName: String): Boolean =
        getStaticObjectField(clazz, fieldName) as? Boolean ?: false

    @JvmStatic
    fun setStaticBooleanField(clazz: Class<*>, fieldName: String, value: Boolean) =
        setStaticObjectField(clazz, fieldName, value)

    @JvmStatic
    fun getStaticIntField(clazz: Class<*>, fieldName: String): Int =
        getStaticObjectField(clazz, fieldName) as? Int ?: 0

    @JvmStatic
    fun setStaticIntField(clazz: Class<*>, fieldName: String, value: Int) =
        setStaticObjectField(clazz, fieldName, value)

    @JvmStatic
    fun getStaticLongField(clazz: Class<*>, fieldName: String): Long =
        getStaticObjectField(clazz, fieldName) as? Long ?: 0L

    @JvmStatic
    fun setStaticLongField(clazz: Class<*>, fieldName: String, value: Long) =
        setStaticObjectField(clazz, fieldName, value)

    @JvmStatic
    fun getStaticFloatField(clazz: Class<*>, fieldName: String): Float =
        getStaticObjectField(clazz, fieldName) as? Float ?: 0f

    @JvmStatic
    fun setStaticFloatField(clazz: Class<*>, fieldName: String, value: Float) =
        setStaticObjectField(clazz, fieldName, value)

    @JvmStatic
    fun getInstanceField(obj: Any, fieldName: String): Any? = getObjectField(obj, fieldName)

    @JvmStatic
    fun setInstanceField(obj: Any, fieldName: String, value: Any?) = setObjectField(obj, fieldName, value)

    @JvmStatic
    fun removeInstanceField(obj: Any, fieldName: String): Any? {
        val old = getObjectField(obj, fieldName)
        setObjectField(obj, fieldName, null)
        return old
    }

    @JvmStatic
    fun getStaticField(clazz: Class<*>, fieldName: String): Any? = getStaticObjectField(clazz, fieldName)

    @JvmStatic
    fun setStaticField(clazz: Class<*>, fieldName: String, value: Any?) = setStaticObjectField(clazz, fieldName, value)

    @JvmStatic
    fun removeStaticField(clazz: Class<*>, fieldName: String): Any? {
        val old = getStaticObjectField(clazz, fieldName)
        setStaticObjectField(clazz, fieldName, null)
        return old
    }

    @JvmStatic
    fun findField(clazz: Class<*>, fieldName: String): Field? {
        var current: Class<*>? = clazz
        while (current != null) {
            runCatching { current!!.getDeclaredField(fieldName) }.getOrNull()
                ?.let { it.isAccessible = true; return it }
            current = current.superclass
        }
        return null
    }

    class FieldSearch(private val clazz: Class<*>) {
        private val filters = mutableListOf<Field.() -> Boolean>()

        fun filter(block: Field.() -> Boolean): FieldSearch {
            filters += block
            return this
        }

        fun filterByName(value: String): FieldSearch = filter { name == value }

        fun filterByType(type: Class<*>): FieldSearch = filter { this.type == type }

        fun first(): Field = toList().firstOrNull()
            ?: throw NoSuchFieldError("Field not found in ${clazz.name}")

        fun firstOrNull(): Field? = toList().firstOrNull()

        fun toList(): List<Field> {
            val result = LinkedHashMap<String, Field>()
            var current: Class<*>? = clazz
            while (current != null) {
                current.declaredFields.forEach { f -> result.putIfAbsent(f.name, f) }
                current = current.superclass
            }
            return result.values.filter { f -> filters.all { f.it() } }
        }
    }
}
