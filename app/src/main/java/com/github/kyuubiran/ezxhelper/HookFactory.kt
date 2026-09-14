@file:Suppress("unused", "MemberVisibilityCanBePrivate", "UNCHECKED_CAST")

package com.github.kyuubiran.ezxhelper

import io.github.libxposed.api.XposedInterface
import io.github.lingqiqi5211.ezhooktool.xposed.common.HookParam
import io.github.lingqiqi5211.ezhooktool.xposed.java.ExtraFields
import io.github.lingqiqi5211.ezhooktool.xposed.java.Hooks
import io.github.lingqiqi5211.ezhooktool.xposed.java.IReplaceHook
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.util.function.Consumer

/**
 * EzXHelper MethodHookParam 的 API 102 兼容实现。
 *
 * <p>字段与 EzXHelper 2.x 保持一致，内部转发到 EzHookTool 的 [HookParam]。</p>
 */
class MethodHookParam internal constructor(internal val raw: HookParam) {

    /** 当前被 hook 的成员。 */
    val method: Member
        get() = raw.executable

    /** 当前实例；静态方法时读取会抛异常（与 EzXHelper 行为一致）。 */
    val thisObject: Any
        get() = raw.thisObject

    /** 当前调用参数；原地修改下标即修改实参。 */
    var args: Array<Any?> = raw.args

    /** 当前返回值，可读写。 */
    var result: Any?
        get() = raw.result
        set(value) {
            raw.result = value
        }

    /** 当前异常，可读写。 */
    var throwable: Throwable?
        get() = raw.throwable
        set(value) {
            raw.throwable = value
        }

    fun getResult(): Any? = raw.result

    fun setResult(value: Any?) {
        raw.result = value
    }

    fun setThrowable(value: Throwable?) {
        raw.throwable = value
    }

    fun hasThrowable(): Boolean = raw.hasThrowable

    @Throws(Throwable::class)
    fun getResultOrThrowable(): Any? {
        if (raw.hasThrowable) throw raw.throwable!!
        return raw.result
    }

    fun setObjectExtra(key: String, value: Any?) {
        ExtraFields.setInstanceField(thisObject, key, value)
    }

    fun getObjectExtra(key: String): Any? = ExtraFields.getInstanceField(thisObject, key)

    fun removeObjectExtra(key: String): Any? = ExtraFields.removeInstanceField(thisObject, key)

    /** 按下标读取参数。 */
    fun args(index: Int): Any? = raw.arg(index)

    /** 按下标读取并转型。 */
    fun <T> argsAs(index: Int): T = raw.arg(index) as T
}

typealias MethodHookBlock = MethodHookParam.() -> Unit

/**
 * EzXHelper HookFactory 的 API 102 兼容实现。
 *
 * <p>Kotlin 侧：`Method.createHook { before {}; after {}; returnConstant(x) }`；
 * Java 侧：[createMethodHook] 配合 [Consumer]。</p>
 */
class HookFactory {

    internal var beforeBlock: MethodHookBlock? = null
    internal var afterBlock: MethodHookBlock? = null
    internal var replaceBlock: (MethodHookParam.() -> Any?)? = null

    var priority: Int = XposedInterface.PRIORITY_DEFAULT

    fun before(block: MethodHookBlock) {
        beforeBlock = block
    }

    fun after(block: MethodHookBlock) {
        afterBlock = block
    }

    fun replace(block: MethodHookParam.() -> Any?) {
        replaceBlock = block
    }

    fun replaceConstant(value: Any?) {
        replaceBlock = { value }
    }

    fun returnConstant(value: Any?) {
        replaceBlock = { value }
    }

    fun returnCatching(value: Any?) {
        replaceBlock = { value }
    }

    fun interrupt() {
        replaceBlock = { null }
    }

    // ==================== Java 入口 ====================

    fun before(callback: interfaces.IMethodHookCallback) {
        beforeBlock = { callback.onMethodHooked(this) }
    }

    fun after(callback: interfaces.IMethodHookCallback) {
        afterBlock = { callback.onMethodHooked(this) }
    }

    @JvmName("-Static")
    companion object {

        @JvmStatic
        fun createMethodHook(method: Method, hook: Consumer<HookFactory>) {
            val factory = HookFactory()
            hook.accept(factory)
            install(method, factory)
        }

        @JvmStatic
        fun createConstructorHook(constructor: Constructor<*>, hook: Consumer<HookFactory>) {
            val factory = HookFactory()
            hook.accept(factory)
            install(constructor, factory)
        }

        // ==================== Kotlin 扩展 ====================

        fun Method.createHook(block: HookFactory.() -> Unit = {}): XposedInterface.HookHandle =
            install(this, HookFactory().apply(block))

        fun Method.createBeforeHook(block: MethodHookBlock): XposedInterface.HookHandle =
            install(this, HookFactory().apply { before(block) })

        fun Method.createAfterHook(block: MethodHookBlock): XposedInterface.HookHandle =
            install(this, HookFactory().apply { after(block) })

        fun Method.createReplaceHook(block: MethodHookParam.() -> Any?): XposedInterface.HookHandle =
            install(this, HookFactory().apply { replace(block) })

        fun Constructor<*>.createHook(block: HookFactory.() -> Unit = {}): XposedInterface.HookHandle =
            install(this, HookFactory().apply(block))

        fun Constructor<*>.createBeforeHook(block: MethodHookBlock): XposedInterface.HookHandle =
            install(this, HookFactory().apply { before(block) })

        fun Constructor<*>.createAfterHook(block: MethodHookBlock): XposedInterface.HookHandle =
            install(this, HookFactory().apply { after(block) })

        fun Iterable<Method>.createHooks(
            block: HookFactory.() -> Unit = {}
        ): List<XposedInterface.HookHandle> {
            val factory = HookFactory().apply(block)
            return map { install(it, factory) }
        }

        fun Array<Method>.createHooks(
            block: HookFactory.() -> Unit = {}
        ): List<XposedInterface.HookHandle> {
            val factory = HookFactory().apply(block)
            return map { install(it, factory) }
        }

        internal fun install(member: Member, factory: HookFactory): XposedInterface.HookHandle {
            val replaceBlock = factory.replaceBlock
            if (replaceBlock != null) {
                val replaceHook = IReplaceHook { param -> replaceBlock.invoke(MethodHookParam(param)) }
                return when (member) {
                    is Method -> Hooks.createHook(member, replaceHook)
                    is Constructor<*> -> Hooks.createHook(member, replaceHook)
                    else -> error("Unsupported member: $member")
                }
            }

            val beforeBlock = factory.beforeBlock
            val afterBlock = factory.afterBlock
            val methodHook = object : io.github.lingqiqi5211.ezhooktool.xposed.java.IMethodHook {
                override fun before(param: HookParam) {
                    beforeBlock?.let { block ->
                        runCatching { MethodHookParam(param).block() }
                            .onFailure { android.util.Log.e("HyperCeiler", "before hook failed", it) }
                    }
                }

                override fun after(param: HookParam) {
                    afterBlock?.let { block ->
                        runCatching { MethodHookParam(param).block() }
                            .onFailure { android.util.Log.e("HyperCeiler", "after hook failed", it) }
                    }
                }
            }

            return when (member) {
                is Method -> Hooks.createHook(member, methodHook)
                is Constructor<*> -> Hooks.createHook(member, methodHook)
                else -> error("Unsupported member: $member")
            }
        }
    }
}
