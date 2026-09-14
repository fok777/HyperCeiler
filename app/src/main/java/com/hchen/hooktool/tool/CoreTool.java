package com.hchen.hooktool.tool;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.github.lingqiqi5211.ezhooktool.core.BestMatchUtils;
import io.github.lingqiqi5211.ezhooktool.core.ClassUtils;
import io.github.lingqiqi5211.ezhooktool.core.java.Fields;
import io.github.lingqiqi5211.ezhooktool.core.java.Methods;

/**
 * HChenX HookTool 的 CoreTool 兼容实现（API 102）。
 */
public final class CoreTool {

    private CoreTool() {
    }

    public static Object callStaticMethod(Class<?> clazz, String methodName, Object... args) {
        return Methods.callStaticMethod(clazz, methodName, args);
    }

    public static Object callStaticMethod(String className, String methodName, Object... args) {
        Class<?> clazz = ClassUtils.loadClassOrNull(className, com.sevtinge.hyperceiler.compat.HookRuntime.classLoader());
        return clazz == null ? null : Methods.callStaticMethod(clazz, methodName, args);
    }

    public static Object callMethod(Object obj, String methodName, Object... args) {
        return Methods.callMethod(obj, methodName, args);
    }

    public static Object getStaticField(Class<?> clazz, String fieldName) {
        return Fields.getStaticObjectField(clazz, fieldName);
    }

    public static void setStaticField(Class<?> clazz, String fieldName, Object value) {
        Fields.setStaticObjectField(clazz, fieldName, value);
    }

    public static Class<?> findClass(String className, ClassLoader classLoader) {
        return ClassUtils.loadClass(className, classLoader);
    }

    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return BestMatchUtils.findMethodBestMatch(clazz, methodName, parameterTypes);
    }

    public static Field findField(Class<?> clazz, String fieldName) {
        return Fields.find(clazz).filterByName(fieldName).first();
    }

    public static Object getField(Object obj, String fieldName) {
        return Fields.getObjectField(obj, fieldName);
    }

    /** 设置字段；成功返回 true。 */
    public static boolean setField(Object obj, String fieldName, Object value) {
        try {
            Fields.setObjectField(obj, fieldName, value);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static Object getStaticField(String className, ClassLoader classLoader, String fieldName) {
        Class<?> clazz = ClassUtils.loadClassOrNull(className, classLoader);
        return clazz == null ? null : Fields.getStaticObjectField(clazz, fieldName);
    }

    public static boolean setStaticField(String className, ClassLoader classLoader, String fieldName, Object value) {
        Class<?> clazz = ClassUtils.loadClassOrNull(className, classLoader);
        if (clazz == null) return false;
        try {
            Fields.setStaticObjectField(clazz, fieldName, value);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static Object getStaticField(Object obj, String fieldName) {
        Class<?> clazz = (obj instanceof Class<?>) ? (Class<?>) obj : obj.getClass();
        return Fields.getStaticObjectField(clazz, fieldName);
    }

    public static boolean setStaticField(Object obj, String fieldName, Object value) {
        Class<?> clazz = (obj instanceof Class<?>) ? (Class<?>) obj : obj.getClass();
        try {
            Fields.setStaticObjectField(clazz, fieldName, value);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static Object newInstance(String className, ClassLoader classLoader, Object... args) {
        Class<?> clazz = ClassUtils.loadClassOrNull(className, classLoader);
        if (clazz == null) return null;
        return io.github.lingqiqi5211.ezhooktool.core.java.Constructors.newInstance(clazz, args);
    }

    public static Object newInstance(Class<?> clazz, Object... args) {
        return io.github.lingqiqi5211.ezhooktool.core.java.Constructors.newInstance(clazz, args);
    }

    public static Class<?> findClass(String className) {
        return ClassUtils.loadClassOrNull(className, io.github.lingqiqi5211.ezhooktool.core.EzClassLoader.current());
    }

    public static Object callMethod(String className, String methodName, Object... args) {
        return Methods.callStaticMethod(findClass(className), methodName, args);
    }
}
