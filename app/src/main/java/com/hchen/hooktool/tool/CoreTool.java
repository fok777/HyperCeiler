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
}
