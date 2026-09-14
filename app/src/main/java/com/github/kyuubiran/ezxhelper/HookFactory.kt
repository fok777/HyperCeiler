@file:Suppress("unused", "MemberVisibilityCanBePrivate", "UNCHECKED_CAST")

package com.github.kyuubiran.ezxhelper

import android.util.Log
import com.github.kyuubiran.ezxhelper.interfaces.IMethodHookCallback
import io.github.libxposed.api.XposedInterface
import io.github.lingqiqi5211.ezhooktool.xposed.common.HookParam
import io.github.lingqiqi5211.ezhooktool.xposed.java.ExtraFields
import io.github.lingqiqi5211.ezhooktool.xposed.java.Hooks
import io.github.lingqiqi5211.ezhooktool.xposed.java.IReplaceHook
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.util.function.Consumer
import com.sevtinge.hyperceiler.compat.XC_MethodHook

/**
 * EzXHelper MethodHookParam 的 API 102 兼容实现。
 */
class MethodHookParam internal constructor(internal val raw: HookParam) {

    /** 当前被 hook 的成员。 */
    val method: Member
        get() = raw.executable

    /** 当前实例；静态方法时读取会抛异常（与 EzXHelper 行为一致）。 */
    val thisObject: Any
        get() = raw.thisObject

    /** 当前实例；静态方法返回 null。 */
    val thisObjectOrNull: Any?
        get() = raw.thisObjectOrNull

    /** 当前调用参数。 */
    @Suppress("UNCHECKED_CAST")
    val args: Array<Any>
        get() = raw.args as Array<Any>

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

    val hasThrowable: Boolean
        get() = raw.hasThrowable

    fun getResult(): Any? = raw.result

    fun setResult(value: Any?) {
        raw.result = value
    }

    fun getThrowable(): Throwable? = raw.throwable

    fun setThrowable(value: Throwable?) {
        raw.throwable = value
    }

    fun hasThrowable(): Boolean = raw.hasThrowable

    @Throws(Throwable::class)
    fun getResultOrThrowable(): Any? =
        if (raw.hasThrowable) throw raw.throwable!! else raw.result

    fun args(index: Int): Any? = raw.arg(index)

    fun <T> argsAs(index: Int): T = raw.argAs<T>(index)

    fun setObjectExtra(key: String, value: Any?) =
        ExtraFields.setInstanceField(raw.thisObject, key, value)

    fun getObjectExtra(key: String): Any? = ExtraFields.getInstanceField(raw.thisObject, key)

    fun removeObjectExtra(key: String): Any? = ExtraFields.removeInstanceField(raw.thisObject, key)
}

typealias MethodHookBlock = (MethodHookParam) -> Unit

/**
 * Kotlin DSL 作用域：对应 EzXHelper `createHook { }` 块内的接收者。
 */
class HookScope {

    internal var beforeBlock: MethodHookBlock? = null
    internal var afterBlock: MethodHookBlock? = null
    internal var replaceBlock: ((MethodHookParam) -> Any?)? = null

    var priority: Int = XposedInterface.PRIORITY_DEFAULT

    fun before(block: MethodHookBlock) {
        beforeBlock = block
    }

    fun after(block: MethodHookBlock) {
        afterBlock = block
    }

    fun replace(block: (MethodHookParam) -> Any?) {
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
}

/**
 * EzXHelper HookFactory 的 API 102 兼容实现。
 *
 * - Kotlin DSL：`Method.createHook { before {}; after {}; returnConstant(x) }`
 * - Java DSL：`HookFactory.createMethodHook(method, hookFactory -> hookFactory.before(...))`
 */
object HookFactory {

    private val javaScope = ThreadLocal<HookScope>()

    fun before(callback: IMethodHookCallback) {
        val scope = javaScope.get()
            ?: error("HookFactory.before must be called inside createMethodHook.")
        scope.beforeBlock = { param -> callback.onMethodHooked(param) }
    }

    fun after(callback: IMethodHookCallback) {
        val scope = javaScope.get()
            ?: error("HookFactory.after must be called inside createMethodHook.")
        scope.afterBlock = { param -> callback.onMethodHooked(param) }
    }

    fun returnConstant(value: Any?) {
        val scope = javaScope.get()
            ?: error("HookFactory.returnConstant must be called inside createMethodHook.")
        scope.returnConstant(value)
    }

    @JvmStatic
    fun createMethodHook(method: Method, hook: Consumer<HookFactory>) {
        createHookInternal(method, hook)
    }

    @JvmStatic
    fun createConstructorHook(constructor: Constructor<*>, hook: Consumer<HookFactory>) {
        createHookInternal(constructor, hook)
    }

    private fun createHookInternal(member: Member, hook: Consumer<HookFactory>) {
        val scope = HookScope()
        val previous = javaScope.get()
        javaScope.set(scope)
        try {
            hook.accept(this)
        } finally {
            if (previous == null) javaScope.remove() else javaScope.set(previous)
        }
        install(member, scope)
    }

    /** Kotlin DSL 扩展入口。 */
    object `-Static` {

        fun Method.createHook(block: HookScope.() -> Unit = {}): XC_MethodHook.Unhook =
            install(this, HookScope().apply(block))

        fun Method.createBeforeHook(block: MethodHookBlock): XC_MethodHook.Unhook =
            install(this, HookScope().apply { before(block) })

        fun Method.createAfterHook(block: MethodHookBlock): XC_MethodHook.Unhook =
            install(this, HookScope().apply { after(block) })

        fun Method.createReplaceHook(block: (MethodHookParam) -> Any?): XC_MethodHook.Unhook =
            install(this, HookScope().apply { replace(block) })

        fun Constructor<*>.createHook(block: HookScope.() -> Unit = {}): XC_MethodHook.Unhook =
            install(this, HookScope().apply(block))

        fun Constructor<*>.createBeforeHook(block: MethodHookBlock): XC_MethodHook.Unhook =
            install(this, HookScope().apply { before(block) })

        fun Constructor<*>.createAfterHook(block: MethodHookBlock): XC_MethodHook.Unhook =
            install(this, HookScope().apply { after(block) })

        fun Iterable<Method>.createHooks(
            block: HookScope.() -> Unit = {}
        ): List<XC_MethodHook.Unhook> {
            val scope = HookScope().apply(block)
            return map { install(it, scope) }
        }

        fun Array<Method>.createHooks(
            block: HookScope.() -> Unit = {}
        ): List<XC_MethodHook.Unhook> {
            val scope = HookScope().apply(block)
            return map { install(it, scope) }
        }

        fun Iterable<Constructor<*>>.createHooks(
            block: HookScope.() -> Unit = {}
        ): List<XC_MethodHook.Unhook> {
            val scope = HookScope().apply(block)
            return map { install(it, scope) }
        }

        fun Array<Constructor<*>>.createHooks(
            block: HookScope.() -> Unit = {}
        ): List<XC_MethodHook.Unhook> {
            val scope = HookScope().apply(block)
            return map { install(it, scope) }
        }
    }

    internal fun install(member: Member, scope: HookScope): XC_MethodHook.Unhook {
        val replaceBlock = scope.replaceBlock
        if (replaceBlock != null) {
            val replaceHook = IReplaceHook { param -> replaceBlock.invoke(MethodHookParam(param)) }
            return when (member) {
                is Method -> XC_MethodHook.Unhook(Hooks.createHook(member, replaceHook))
                is Constructor<*> -> XC_MethodHook.Unhook(Hooks.createHook(member, replaceHook))
                else -> error("Unsupported member: $member")
            }
        }

        val beforeBlock = scope.beforeBlock
        val afterBlock = scope.afterBlock
        val methodHook = object : io.github.lingqiqi5211.ezhooktool.xposed.java.IMethodHook {
            override fun before(param: HookParam) {
                val block = beforeBlock ?: return
                runCatching { block.invoke(MethodHookParam(param)) }
                    .onFailure { Log.e("HyperCeiler", "before hook failed", it) }
            }

            override fun after(param: HookParam) {
                val block = afterBlock ?: return
                runCatching { block.invoke(MethodHookParam(param)) }
                    .onFailure { Log.e("HyperCeiler", "after hook failed", it) }
            }
        }

        return when (member) {
            is Method -> XC_MethodHook.Unhook(Hooks.createHook(member, methodHook))
            is Constructor<*> -> XC_MethodHook.Unhook(Hooks.createHook(member, methodHook))
            else -> error("Unsupported member: $member")
        }
    }
}
