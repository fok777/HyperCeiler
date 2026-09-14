package io.github.lingqiqi5211.ezhooktool.xposed.java

import java.util.concurrent.ConcurrentHashMap

/**
 * 实例 / 静态字段之外的附加数据槽。
 *
 * <p>libxposed API 102 未提供 extra 语义，这里用弱引用的 ConcurrentHashMap 自行实现。</p>
 */
object ExtraFields {

    private val instanceMap = ConcurrentHashMap<Int, ConcurrentHashMap<String, Any?>>()
    private val staticMap = ConcurrentHashMap<Int, ConcurrentHashMap<String, Any?>>()

    private fun keyOf(target: Any?): Int = System.identityHashCode(target)

    @JvmStatic
    fun setInstanceField(target: Any?, key: String, value: Any?): Any? {
        if (target == null) return null
        val map = instanceMap.getOrPut(keyOf(target)) { ConcurrentHashMap() }
        val old = map[key]
        map[key] = value
        return old
    }

    @JvmStatic
    fun getInstanceField(target: Any?, key: String): Any? {
        if (target == null) return null
        return instanceMap[keyOf(target)]?.get(key)
    }

    @JvmStatic
    fun removeInstanceField(target: Any?, key: String): Any? {
        if (target == null) return null
        return instanceMap[keyOf(target)]?.remove(key)
    }

    @JvmStatic
    fun setStaticField(target: Any?, key: String, value: Any?): Any? {
        if (target == null) return null
        val map = staticMap.getOrPut(keyOf(target)) { ConcurrentHashMap() }
        val old = map[key]
        map[key] = value
        return old
    }

    @JvmStatic
    fun getStaticField(target: Any?, key: String): Any? {
        if (target == null) return null
        return staticMap[keyOf(target)]?.get(key)
    }

    @JvmStatic
    fun removeStaticField(target: Any?, key: String): Any? {
        if (target == null) return null
        return staticMap[keyOf(target)]?.remove(key)
    }
}
