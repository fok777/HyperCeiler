package io.github.lingqiqi5211.ezhooktool.xposed

import android.content.Context
import com.sevtinge.hyperceiler.compat.HookRuntime
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModuleInterface

/**
 * 运行时状态持有者。
 *
 * <p>本项目直接在 libxposed API 102 之上实现，不再依赖外部 EzHookTool。所有状态最终落到
 * [HookRuntime]，由 [com.sevtinge.hyperceiler.XposedInitEntry] 写入。</p>
 */
object EzXposed {

    @JvmStatic
    val frameworkApiVersion: Int
        get() = 102

    @JvmStatic
    val classLoader: ClassLoader
        get() = HookRuntime.classLoader()

    @JvmStatic
    val safeClassLoader: ClassLoader
        get() = HookRuntime.classLoader()

    @JvmStatic
    fun getClassLoader(): ClassLoader = classLoader

    @JvmStatic
    fun getSafeClassLoader(): ClassLoader = safeClassLoader

    @JvmStatic
    val appContext: Context
        get() = HookRuntime.appContext()
            ?: throw IllegalStateException("Application context not ready yet.")

    @JvmStatic
    val appContextOrNull: Context?
        get() = HookRuntime.appContext()

    @JvmStatic
    fun getAppContext(): Context = appContext

    @JvmStatic
    fun getAppContextOrNull(): Context? = appContextOrNull

    @JvmStatic
    var packageName: String
        get() = HookRuntime.packageName()
        set(value) = HookRuntime.attachPackage(value, HookRuntime.classLoader())

    @JvmStatic
    fun getPackageName(): String = packageName

    @JvmStatic
    var processName: String
        get() = HookRuntime.processName()
        set(value) = HookRuntime.attach(HookRuntime.xposed(), HookRuntime.modulePath(), value)

    @JvmStatic
    fun getProcessName(): String = processName

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
        HookRuntime.attachPackage(param.packageName, param.defaultClassLoader)
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
        HookRuntime.attach(base, HookRuntime.modulePath(), HookRuntime.processName())
        targetReady.run()
    }
}
