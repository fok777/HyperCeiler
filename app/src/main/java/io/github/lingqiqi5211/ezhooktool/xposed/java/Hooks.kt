package io.github.lingqiqi5211.ezhooktool.xposed.java

import com.sevtinge.hyperceiler.compat.HookRuntime
import io.github.libxposed.api.XposedInterface
import io.github.lingqiqi5211.ezhooktool.core.EzClassLoader
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

    @JvmField
    val sOk = java.util.concurrent.atomic.AtomicInteger()

    @JvmField
    val sFail = java.util.concurrent.atomic.AtomicInteger()

    internal class EmulatedHooker(
        private val beforeBlock: ((HookParam) -> Unit)?,
        private val afterBlock: ((HookParam) -> Unit)?,
        private val replaceBlock: ((HookParam) -> Any?)?
    ) : XposedInterface.Hooker {

        override fun intercept(chain: XposedInterface.Chain): Any? {
            markHit(chain.executable)
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
            if (param.isSkipped) {
                val pending = param.throwable
                if (pending != null) throw pending
                return param.result
            }

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

    /**
     * 记录 hook 被真实触发的成员，用于区分“hook 未安装”与“hook 生效但未达预期”。
     * 每个成员只打印一次，避免刷屏。
     */
    private val sHits = java.util.Collections.newSetFromMap(
        java.util.concurrent.ConcurrentHashMap<String, Boolean>())

    private fun markHit(executable: Executable) {
        if (!sHits.add(executable.toString())) return
        val msg = "HyperCeiler: HOOKHIT " + executable
        android.util.Log.i(TAG, msg)
        com.sevtinge.hyperceiler.compat.XposedBridge.log(msg)
    }

    @JvmStatic
    fun hook(origin: Executable, hooker: XposedInterface.Hooker): XposedInterface.HookHandle? {
        val xposed = HookRuntime.xposed()
        if (xposed == null) {
            sFail.incrementAndGet()
            val msg = "HyperCeiler: XposedInterface not ready, skip hook: $origin"
            android.util.Log.e(TAG, msg)
            com.sevtinge.hyperceiler.compat.XposedBridge.log(msg)
            return null
        }
        return try {
            val handle = xposed.hook(origin)
                .setPriority(XposedInterface.PRIORITY_DEFAULT)
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(hooker)
            sOk.incrementAndGet()
            handle
        } catch (t: Throwable) {
            sFail.incrementAndGet()
            val msg = "HyperCeiler: hook failed: $origin -> $t"
            android.util.Log.e(TAG, msg, t)
            com.sevtinge.hyperceiler.compat.XposedBridge.log(msg)
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
    fun findAndHookMethod(
        className: String,
        classLoader: ClassLoader?,
        methodName: String,
        vararg parameterTypesAndCallback: Any?
    ): XposedInterface.HookHandle? {
        val clazz = runCatching {
            Class.forName(className, false, classLoader ?: EzClassLoader.current())
        }.getOrNull() ?: return null
        return findAndHookMethod(clazz, methodName, *parameterTypesAndCallback)
    }

    @JvmStatic
    fun findAndHookConstructor(
        className: String,
        classLoader: ClassLoader?,
        vararg parameterTypesAndCallback: Any?
    ): XposedInterface.HookHandle? {
        val clazz = runCatching {
            Class.forName(className, false, classLoader ?: EzClassLoader.current())
        }.getOrNull() ?: return null
        return findAndHookConstructor(clazz, *parameterTypesAndCallback)
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

    /** 供 Java 侧按成员分发（整体替换语义）。 */
    @JvmStatic
    fun createHook(member: Member, callback: IReplaceHook): XposedInterface.HookHandle? =
        when (member) {
            is Method -> createHook(member, callback)
            is Constructor<*> -> createHook(member, callback)
            else -> null
        }
}
