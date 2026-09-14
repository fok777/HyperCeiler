package com.sevtinge.hyperceiler.compat;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import io.github.libxposed.api.XposedInterface;

/**
 * API 102 运行时持有者。
 *
 * <p>模块入口在 onModuleLoaded 时把 libxposed 接口写进来，兼容层通过它拿到
 * XposedInterface、RemotePreferences 与模块 Context。</p>
 */
public final class HookRuntime {

    private static volatile XposedInterface sXposed;
    private static volatile ClassLoader sClassLoader;
    private static volatile Context sAppContext;
    private static volatile String sPackageName = "";
    private static volatile String sProcessName = "";
    private static volatile String sModulePath;
    private static volatile boolean sSystemServer;

    private HookRuntime() {
    }

    public static void attach(@Nullable XposedInterface xposed, String modulePath, String processName) {
        sXposed = xposed;
        if (modulePath != null && !modulePath.isEmpty()) sModulePath = modulePath;
        if (processName != null) sProcessName = processName;
    }

    public static void setSystemServer(boolean systemServer) {
        sSystemServer = systemServer;
    }

    public static boolean isSystemServer() {
        return sSystemServer;
    }

    public static void attachPackage(String packageName, ClassLoader classLoader) {
        sPackageName = packageName;
        sClassLoader = classLoader;
    }

    public static void attachContext(Context context) {
        sAppContext = context;
    }

    @Nullable
    public static XposedInterface xposed() {
        return sXposed;
    }

    @Nullable
    public static Context appContext() {
        return sAppContext;
    }

    public static String modulePath() {
        return sModulePath;
    }

    public static String processName() {
        return sProcessName;
    }

    public static String packageName() {
        return sPackageName;
    }

    public static ClassLoader classLoader() {
        if (sClassLoader != null) return sClassLoader;
        return HookRuntime.class.getClassLoader();
    }

    @Nullable
    public static SharedPreferences remotePreferences(String group) {
        XposedInterface xposed = sXposed;
        if (xposed == null) return null;
        try {
            return xposed.getRemotePreferences(group);
        } catch (Throwable t) {
            android.util.Log.w("HyperCeiler", "getRemotePreferences failed: " + t);
            return null;
        }
    }
}
