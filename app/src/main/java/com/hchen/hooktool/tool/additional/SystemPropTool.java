package com.hchen.hooktool.tool.additional;

import java.lang.reflect.Method;

/**
 * HChenX HookTool 的 SystemPropTool 兼容实现（API 102）。
 */
public final class SystemPropTool {

    private SystemPropTool() {
    }

    public static String getProp(String key, String defaultValue) {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method get = clazz.getDeclaredMethod("get", String.class, String.class);
            return (String) get.invoke(null, key, defaultValue);
        } catch (Throwable t) {
            return defaultValue;
        }
    }

    public static String getProp(String key) {
        return getProp(key, "");
    }

    public static int getProp(String key, int defaultValue) {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method get = clazz.getDeclaredMethod("getInt", String.class, int.class);
            return (Integer) get.invoke(null, key, defaultValue);
        } catch (Throwable t) {
            return defaultValue;
        }
    }

    public static boolean getProp(String key, boolean defaultValue) {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method get = clazz.getDeclaredMethod("getBoolean", String.class, boolean.class);
            return (Boolean) get.invoke(null, key, defaultValue);
        } catch (Throwable t) {
            return defaultValue;
        }
    }

    public static void setProp(String key, String value) {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method set = clazz.getDeclaredMethod("set", String.class, String.class);
            set.invoke(null, key, value);
        } catch (Throwable ignored) {
        }
    }
}
