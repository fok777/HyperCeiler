package io.github.lingqiqi5211.ezhooktool.xposed.java

import com.sevtinge.hyperceiler.compat.HookRuntime
import io.github.libxposed.api.XposedInterface
import io.github.lingqiqi5211.ezhooktool.xposed.common.HookParam
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Member
import java.lang.reflect.Method

/**
 * hook 安装入口。
 *
 * <p>统一把 before / after / replace 三种语义折叠成 libxposed API 102 的单个
 * [XposedInterface.Hooker]，通过 [XposedInterface.Chain.proceed] 的调用时机来表达。</p>
 */
object Hooks {

    private const val TAG = "HyperCeiler"

    internal class EmulatedHooker(
        private val beforeBlock: ((HookParam) -> Unit)?,
        private val afterBlock: ((HookParam) -> Unit)?,
        private val replaceBlock: ((HookParam) -> Any?)?
    ) : XposedInterface.Hooker {

        override fun intercept(chain: XposedInterface.Chain): Any? {
            val param = HookParam(chain)
            val replace = replaceBlock
            if (replace != null) {
                return runCatching { replace.invoke(param) }
                    .onFailure { android.util.Log.e(TAG, "replace hook failed", it) }
                    .getOrNull()
            }
            val before = beforeBlock
            if (before != null) {
                runCatching { before.invoke(param) }
                    .onFailure { android.util.Log.e(TAG, "before hook failed", it) }
            }
            if (param.isSkipped) return param.result

            val result: Any?
            try {
                result = chain.proceed()
            } catch (t: Throwable) {
                param.setProceedThrowable(t)
                afterBlock?.let {
                    runCatching { it.invoke(param) }
                        .onFailure { e -> android.util.Log.e(TAG, "after hook failed", e) }
                }
                throw t
            }
            param.setProceedResult(result)
            afterBlock?.let {
                runCatching { it.invoke(param) }
                    .onFailure { e -> android.util.Log.e(TAG, "after hook failed", e) }
            }
            return param.result
        }
    }

    @JvmStatic
    fun hook(origin: Executable, hooker: XposedInterface.Hooker): XposedInterface.HookHandle? {
        val xposed = HookRuntime.xposed()
        if (xposed == null) {
            android.util.Log.e(TAG, "XposedInterface not ready, skip hook: $origin")
            return null
        }
        return try {
            xposed.hook(origin)
                .setPriority(XposedInterface.PRIORITY_DEFAULT)
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(hooker)
        } catch (t: Throwable) {
            android.util.Log.e(TAG, "hook failed: $origin", t)
            null
        }
    }

    @JvmStatic
    fun createHook(method: Method, callback: IMethodHook): XposedInterface.HookHandle? =
        hook(method, EmulatedHooker({ callback.before(it) }, { callback.after(it) }, null))

    @JvmStatic
    fun createHook(constructor: Constructor<*>, callback: IMethodHook): XposedInterface.HookHandle? =
        hook(constructor, EmulatedHooker({ callback.before(it) }, { callback.after(it) }, null))

    @JvmStatic
    fun createHook(method: Method, callback: IReplaceHook): XposedInterface.HookHandle? =
        hook(method, EmulatedHooker(null, null) { callback.replace(it) })

    @JvmStatic
    fun createHook(constructor: Constructor<*>, callback: IReplaceHook): XposedInterface.HookHandle? =
        hook(constructor, EmulatedHooker(null, null) { callback.replace(it) })

    @JvmStatic
    fun createHook(method: Method, key: String, callback: IMethodHook): XposedInterface.HookHandle? =
        createHook(method, callback)

    @JvmStatic
    fun createHook(constructor: Constructor<*>, key: String, callback: IMethodHook): XposedInterface.HookHandle? =
        createHook(constructor, callback)

    @JvmStatic
    fun createHook(method: Method, key: String, callback: IReplaceHook): XposedInterface.HookHandle? =
        createHook(method, callback)

    @JvmStatic
    fun findAndHookMethod(
        clazz: Class<*>,
        methodName: String,
        vararg parameterTypesAndCallback: Any?
    ): XposedInterface.HookHandle? {
        val callback = parameterTypesAndCallback.lastOrNull()
        val types = parameterTypesAndCallback.dropLast(1).map { it as Class<*> }.toTypedArray()
        val method = findMethod(clazz, methodName, types) ?: return null
        return when (callback) {
            is IMethodHook -> createHook(method, callback)
            is IReplaceHook -> createHook(method, callback)
            else -> null
        }
    }

    @JvmStatic
    fun findAndHookConstructor(
        clazz: Class<*>,
        vararg parameterTypesAndCallback: Any?
    ): XposedInterface.HookHandle? {
        val callback = parameterTypesAndCallback.lastOrNull()
        val types = parameterTypesAndCallback.dropLast(1).map { it as Class<*> }.toTypedArray()
        val constructor = runCatching { clazz.getDeclaredConstructor(*types) }.getOrNull() ?: return null
        return when (callback) {
            is IMethodHook -> createHook(constructor, callback)
            is IReplaceHook -> createHook(constructor, callback)
            else -> null
        }
    }

    private fun findMethod(clazz: Class<*>, name: String, types: Array<Class<*>>): Method? =
        runCatching { clazz.getDeclaredMethod(name, *types).also { it.isAccessible = true } }.getOrNull()
            ?: runCatching { clazz.getMethod(name, *types) }.getOrNull()

    /** 供 Java 侧按成员分发。 */
    @JvmStatic
    fun createHook(member: Member, callback: IMethodHook): XposedInterface.HookHandle? =
        when (member) {
            is Method -> createHook(member, callback)
            is Constructor<*> -> createHook(member, callback)
            else -> null
        }
}
