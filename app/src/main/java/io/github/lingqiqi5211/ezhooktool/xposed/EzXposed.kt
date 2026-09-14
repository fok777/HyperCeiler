package io.github.lingqiqi5211.ezhooktool.xposed

import android.content.Context
import com.sevtinge.hyperceiler.compat.HookRuntime
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModuleInterface

/**
 * 运行时状态持有者（本项目自研实现，替代外部 EzHookTool）。
 *
 * <p>所有状态最终落到 [HookRuntime]，由 [com.sevtinge.hyperceiler.XposedInitEntry] 写入。</p>
 */
object EzXposed {

    @JvmStatic
    val frameworkApiVersion: Int
        get() = 102

    @get:JvmStatic
    val classLoader: ClassLoader
        get() = HookRuntime.classLoader()

    @get:JvmStatic
    val safeClassLoader: ClassLoader
        get() = HookRuntime.classLoader()

    @get:JvmStatic
    val appContext: Context
        get() = HookRuntime.appContext()
            ?: throw IllegalStateException("Application context not ready yet.")

    @get:JvmStatic
    val appContextOrNull: Context?
        get() = HookRuntime.appContext()

    @get:JvmStatic
    var packageName: String
        get() = HookRuntime.packageName()
        set(value) = HookRuntime.attachPackage(value, HookRuntime.classLoader())

    @get:JvmStatic
    var processName: String
        get() = HookRuntime.processName()
        set(value) = HookRuntime.attach(HookRuntime.xposed(), null, value)

    @JvmStatic
    fun getModulePath(): String = HookRuntime.modulePath() ?: ""

    @JvmStatic
    fun isSystemServer(): Boolean = HookRuntime.isSystemServer()

    @JvmStatic
    fun initAppContext(context: Context?) {
        if (context != null) HookRuntime.attachContext(context.applicationContext ?: context)
    }

    @JvmStatic
    fun initAppContext(context: Context?, force: Boolean) {
        initAppContext(context)
    }

    @JvmStatic
    fun initOnModuleLoaded(base: XposedInterface, param: XposedModuleInterface.ModuleLoadedParam) {
        HookRuntime.attach(base, null, param.processName)
        HookRuntime.attachPackage(param.processName ?: "", HookRuntime.classLoader())
    }

    @JvmStatic
    fun initOnPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        HookRuntime.attachPackage(param.packageName, param.defaultClassLoader)
    }

    @JvmStatic
    fun initOnPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        HookRuntime.attachPackage(param.packageName, param.classLoader)
    }

    @JvmStatic
    fun initOnSystemServerStarting(param: XposedModuleInterface.SystemServerStartingParam) {
        HookRuntime.setSystemServer(true)
        HookRuntime.attachPackage("android", param.classLoader)
    }

    /** 目标就绪回调：本实现直接在当前线程执行。 */
    @JvmStatic
    fun onTargetReady(callback: Runnable) {
        callback.run()
    }

    @JvmStatic
    fun handleHotReloading(param: XposedModuleInterface.HotReloadingParam): Boolean = true

    @JvmStatic
    fun handleHotReloadedWithTargetReady(
        base: XposedInterface,
        param: XposedModuleInterface.HotReloadedParam,
        targetReady: Runnable
    ) {
        HookRuntime.attach(base, null, HookRuntime.processName())
        targetReady.run()
    }
}
