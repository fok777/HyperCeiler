package com.sevtinge.hyperceiler;

import android.content.pm.ApplicationInfo;

import androidx.annotation.NonNull;

import com.sevtinge.hyperceiler.compat.HookRuntime;
import com.sevtinge.hyperceiler.compat.StartupParam;
import com.sevtinge.hyperceiler.compat.XC_LoadPackage;
import com.sevtinge.hyperceiler.module.base.tool.ResourcesTool;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;
import io.github.lingqiqi5211.ezhooktool.xposed.EzXposed;

/**
 * HyperCeiler 模块入口（libxposed API 102）。
 *
 * <p>替代原来的 assets/xposed_init + IXposedHookZygoteInit / IXposedHookLoadPackage 组合：
 * 原来在 zygote 只跑一次的逻辑放进 {@link #onModuleLoaded}，
 * 原来按包分发的逻辑放进 {@link #onPackageReady} 与 {@link #onSystemServerStarting}。</p>
 */
public class XposedInitEntry extends XposedModule {

    private static final String TAG = "HyperCeiler";

    private final XposedInit mXposedInit = new XposedInit();

    public XposedInitEntry() {
        super();
    }

    @Override
    public void onModuleLoaded(@NonNull XposedModuleInterface.ModuleLoadedParam param) {
        diag("boot", param.getProcessName());
        EzXposed.initOnModuleLoaded(this, param);
        HookRuntime.attach(this, EzXposed.getModulePath(), param.getProcessName());

        try {
            mXposedInit.initZygote(new StartupParam(EzXposed.getModulePath()));
        } catch (Throwable t) {
            android.util.Log.e(TAG, "XposedInitEntry: initZygote failed: " + t);
        }

        // 所有包级 hook 统一注册到目标就绪回调，保证首次加载与热重载走同一条路径。
        EzXposed.onTargetReady(this::installCurrentTargetHooks);
    }

    @Override
    public void onSystemServerStarting(@NonNull XposedModuleInterface.SystemServerStartingParam param) {
        diag("syssrv", "android");
        EzXposed.initOnSystemServerStarting(param);
        HookRuntime.setSystemServer(true);
        dispatch("android", param.getClassLoader(), null, "android");
    }

    @Override
    public void onPackageLoaded(@NonNull XposedModuleInterface.PackageLoadedParam param) {
        if (!param.isFirstPackage()) return;
        EzXposed.initOnPackageLoaded(param);
    }

    @Override
    public void onPackageReady(@NonNull XposedModuleInterface.PackageReadyParam param) {
        diag("ready", param.getPackageName() + "|first=" + param.isFirstPackage());
        if (!param.isFirstPackage()) return;
        EzXposed.initOnPackageReady(param);
        dispatch(param.getPackageName(), param.getClassLoader(), null,
            EzXposed.getProcessName());
    }

    @Override
    public boolean onHotReloading(@NonNull XposedModuleInterface.HotReloadingParam param) {
        return EzXposed.handleHotReloading(param);
    }

    @Override
    public void onHotReloaded(@NonNull XposedModuleInterface.HotReloadedParam param) {
        EzXposed.handleHotReloadedWithTargetReady(this, param, this::installCurrentTargetHooks);
    }

    /**
     * 安装当前目标进程的 hook。
     *
     * <p>由 [onTargetReady] 触发，首次加载与热重载共用，因此必须可重复执行。</p>
     */
    private void installCurrentTargetHooks() {
        if (HookRuntime.isSystemServer()) {
            dispatch("android", EzXposed.getClassLoader(), null, EzXposed.getProcessName());
            return;
        }
        String packageName = EzXposed.getPackageName();
        if (packageName == null || packageName.isEmpty()) return;
        dispatch(packageName, EzXposed.getClassLoader(), null, EzXposed.getProcessName());
    }

    /**
     * 早期探针：确认框架是否真的加载了模块并回调入口。
     *
     * <p>写入模块进程的远程偏好，可在“设置 -> 开发者 -> 调试信息”的 HookDiag 中看到。
     * 若 HookDiag 始终为 none，说明框架从未加载本模块（LSPosed 未启用或作用域未勾选）。</p>
     */
    private void diag(String stage, String info) {
        try {
            android.content.SharedPreferences remote =
                com.sevtinge.hyperceiler.compat.HookRuntime.remotePreferences(
                    com.sevtinge.hyperceiler.utils.prefs.PrefsUtils.mPrefsName);
            if (remote != null) {
                remote.edit()
                    .putString("__hc_boot_" + stage, info + "@" + System.currentTimeMillis())
                    .commit();
            }
        } catch (Throwable ignored) {
        }
        android.util.Log.i(TAG, "HyperCeilerBoot[" + stage + "] " + info);
    }

    private void dispatch(String packageName, ClassLoader classLoader,
                          ApplicationInfo appInfo, String processName) {
        if (packageName == null || classLoader == null) return;
        HookRuntime.attachPackage(packageName, classLoader);
        XC_LoadPackage.LoadPackageParam lpparam = new XC_LoadPackage.LoadPackageParam(
            packageName, processName, classLoader, appInfo, true);
        try {
            mXposedInit.handleLoadPackage(lpparam);
        } catch (Throwable t) {
            android.util.Log.e(TAG, "XposedInitEntry: handleLoadPackage failed for " + packageName + ": " + t);
        }
    }

    /** 模块资源工具，供 BaseHook / BaseModule 访问。 */
    public static ResourcesTool resourcesTool() {
        return com.sevtinge.hyperceiler.XposedInit.mResHook;
    }
}
