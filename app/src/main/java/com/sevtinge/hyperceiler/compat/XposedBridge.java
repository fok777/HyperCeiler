package com.sevtinge.hyperceiler.compat;

import android.util.Log;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import io.github.libxposed.api.XposedInterface;
import io.github.lingqiqi5211.ezhooktool.xposed.java.Hooks;

/**
 * legacy XposedBridge 的 API 102 兼容影子。
 */
public final class XposedBridge {

    private static final String TAG = "HyperCeiler";

    private XposedBridge() {
    }

    public static void log(String text) {
        XposedInterface xposed = HookRuntime.xposed();
        if (xposed != null) {
            xposed.log(Log.INFO, TAG, text);
        } else {
            Log.i(TAG, text);
        }
    }

    public static void log(Throwable t) {
        log(Log.getStackTraceString(t));
    }

    public static int getXposedVersion() {
        XposedInterface xposed = HookRuntime.xposed();
        return xposed != null ? xposed.getApiVersion() : XposedInterface.API_102;
    }

    public static XC_MethodHook.Unhook hookMethod(Member member, XC_MethodHook callback) {
        if (member instanceof Method method) {
            return new XC_MethodHook.Unhook(Hooks.createHook(method, callback.asHook()));
        }
        if (member instanceof java.lang.reflect.Constructor<?> constructor) {
            return new XC_MethodHook.Unhook(Hooks.createHook(constructor, callback.asHook()));
        }
        throw new IllegalArgumentException("Unsupported member: " + member);
    }

    public static Set<XC_MethodHook.Unhook> hookAllMethods(Class<?> hookClass, String methodName, XC_MethodHook callback) {
        Set<XC_MethodHook.Unhook> unhooks = new LinkedHashSet<>();
        for (Method method : MethodsCompat.findAllMethods(hookClass, methodName)) {
            unhooks.add(new XC_MethodHook.Unhook(Hooks.createHook(method, callback.asHook())));
        }
        return unhooks;
    }

    public static Set<XC_MethodHook.Unhook> hookAllConstructors(Class<?> hookClass, XC_MethodHook callback) {
        Set<XC_MethodHook.Unhook> unhooks = new LinkedHashSet<>();
        for (java.lang.reflect.Constructor<?> constructor : hookClass.getDeclaredConstructors()) {
            unhooks.add(new XC_MethodHook.Unhook(Hooks.createHook(constructor, callback.asHook())));
        }
        return unhooks;
    }

    public static Object invokeOriginalMethod(Member method, Object thisObject, Object... args) {
        if (method instanceof Method m) {
            m.setAccessible(true);
            try {
                return m.invoke(thisObject, args);
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw e.getCause();
            }
        }
        throw new IllegalArgumentException("Unsupported member: " + method);
    }
}
