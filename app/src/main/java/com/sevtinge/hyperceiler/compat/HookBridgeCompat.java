package com.sevtinge.hyperceiler.compat;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import io.github.libxposed.api.XposedInterface;
import io.github.lingqiqi5211.ezhooktool.xposed.java.Hooks;

/**
 * hook 回调转换与日志兜底。
 */
public final class HookBridgeCompat {

    private static final String TAG = "HyperCeiler";

    private HookBridgeCompat() {
    }

    public static void logHookError(String phase, Throwable t) {
        Log.e(TAG, "Hook callback (" + phase + ") failed: " + t, t);
    }

    /** 把 legacy 风格的回调对象转换成 EzHookTool 支持的类型。 */
    public static Object unwrapCallback(Object callback) {
        if (callback instanceof XC_MethodReplacement replacement) {
            return replacement.asReplaceHook();
        }
        if (callback instanceof XC_MethodHook hook) {
            return hook.asHook();
        }
        return callback;
    }

    /** 把参数数组最后一项（回调）转换后返回新数组。 */
    public static Object[] unwrapArgs(Object... args) {
        if (args == null || args.length == 0) return args;
        Object[] copy = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            copy[i] = (i == args.length - 1) ? unwrapCallback(args[i]) : args[i];
        }
        return copy;
    }

    public static XposedInterface.HookHandle hookAllMethods(Class<?> clazz, String methodName, Object callback) {
        Object unwrapped = unwrapCallback(callback);
        Method[] methods = MethodsCompat.findAllMethods(clazz, methodName);
        XposedInterface.HookHandle last = null;
        for (Method method : methods) {
            last = Hooks.createHook(method, (io.github.lingqiqi5211.ezhooktool.xposed.java.IMethodHook) unwrapped);
        }
        return last;
    }

    public static XposedInterface.HookHandle hookAllConstructors(Class<?> clazz, Object callback) {
        Object unwrapped = unwrapCallback(callback);
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        XposedInterface.HookHandle last = null;
        for (Constructor<?> constructor : constructors) {
            last = Hooks.createHook(constructor, (io.github.lingqiqi5211.ezhooktool.xposed.java.IMethodHook) unwrapped);
        }
        return last;
    }
}
