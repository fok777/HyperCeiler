package com.sevtinge.hyperceiler.compat;

import android.content.Context;

import io.github.lingqiqi5211.ezhooktool.xposed.EzXposed;

/**
 * legacy EzXHelper 的 API 102 兼容影子。
 *
 * <p>只保留模块在用的基础能力；hook 相关的 EzXHelper DSL 请改用 EzHookTool。</p>
 */
public final class EzXHelper {

    private static String sLogTag = "HyperCeiler";
    private static String sToastTag = "HyperCeiler";

    private EzXHelper() {
    }

    public static void initZygote(StartupParam startupParam) {
        // API 102 下模块路径由入口直接提供，这里只做兼容占位。
    }

    public static void initHandleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        HookRuntime.attachPackage(lpparam.packageName, lpparam.classLoader);
    }

    public static void setLogTag(String tag) {
        sLogTag = tag;
    }

    public static String getLogTag() {
        return sLogTag;
    }

    public static void setToastTag(String tag) {
        sToastTag = tag;
    }

    public static String getToastTag() {
        return sToastTag;
    }

    public static ClassLoader getClassLoader() {
        return HookRuntime.classLoader();
    }

    public static Context getAppContext() {
        Context context = HookRuntime.appContext();
        if (context != null) return context;
        return EzXposed.getAppContextOrNull();
    }

    public static String getHostPackageName() {
        return EzXposed.getPackageName();
    }
}
