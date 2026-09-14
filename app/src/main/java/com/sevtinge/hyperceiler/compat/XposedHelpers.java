package com.sevtinge.hyperceiler.compat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import io.github.lingqiqi5211.ezhooktool.core.BestMatchUtils;
import io.github.lingqiqi5211.ezhooktool.core.ClassUtils;
import io.github.lingqiqi5211.ezhooktool.core.java.Constructors;
import io.github.lingqiqi5211.ezhooktool.core.java.Fields;
import io.github.lingqiqi5211.ezhooktool.core.java.Methods;
import io.github.lingqiqi5211.ezhooktool.xposed.java.ExtraFields;
import io.github.lingqiqi5211.ezhooktool.xposed.java.Hooks;

/**
 * legacy XposedHelpers 的 API 102 兼容影子。
 *
 * <p>方法签名尽量与 legacy 保持一致，内部转发到 EzHookTool / 标准反射，
 * 这样业务代码只需替换 import。</p>
 */
public final class XposedHelpers {

    private XposedHelpers() {
    }

    /** 类查找失败时抛出，语义与 legacy 一致。 */
    public static class ClassNotFoundError extends Error {
        public ClassNotFoundError(String message) {
            super(message);
        }

        public ClassNotFoundError(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // ==================== 类查找 ====================

    public static Class<?> findClass(String className, ClassLoader classLoader) {
        try {
            return ClassUtils.loadClass(className, classLoader != null ? classLoader : HookRuntime.classLoader());
        } catch (Throwable t) {
            throw new ClassNotFoundError("Class not found: " + className, t);
        }
    }

    public static Class<?> findClass(String className) {
        return findClass(className, HookRuntime.classLoader());
    }

    public static Class<?> findClassIfExists(String className, ClassLoader classLoader) {
        try {
            return ClassUtils.loadClassOrNull(className, classLoader != null ? classLoader : HookRuntime.classLoader());
        } catch (Throwable t) {
            return null;
        }
    }

    public static Class<?> findClassIfExists(String className) {
        return findClassIfExists(className, HookRuntime.classLoader());
    }

    // ==================== 字段读写 ====================

    public static Object getObjectField(Object obj, String fieldName) {
        return Fields.getObjectField(obj, fieldName);
    }

    public static void setObjectField(Object obj, String fieldName, Object value) {
        Fields.setObjectField(obj, fieldName, value);
    }

    public static Object getStaticObjectField(Class<?> clazz, String fieldName) {
        return Fields.getStaticObjectField(clazz, fieldName);
    }

    public static void setStaticObjectField(Class<?> clazz, String fieldName, Object value) {
        Fields.setStaticObjectField(clazz, fieldName, value);
    }

    public static boolean getBooleanField(Object obj, String fieldName) {
        return Fields.getBooleanField(obj, fieldName);
    }

    public static void setBooleanField(Object obj, String fieldName, boolean value) {
        Fields.setBooleanField(obj, fieldName, value);
    }

    public static int getIntField(Object obj, String fieldName) {
        return Fields.getIntField(obj, fieldName);
    }

    public static void setIntField(Object obj, String fieldName, int value) {
        Fields.setIntField(obj, fieldName, value);
    }

    public static long getLongField(Object obj, String fieldName) {
        return Fields.getLongField(obj, fieldName);
    }

    public static void setLongField(Object obj, String fieldName, long value) {
        Fields.setLongField(obj, fieldName, value);
    }

    public static float getFloatField(Object obj, String fieldName) {
        return Fields.getFloatField(obj, fieldName);
    }

    public static void setFloatField(Object obj, String fieldName, float value) {
        Fields.setFloatField(obj, fieldName, value);
    }

    public static boolean getStaticBooleanField(Class<?> clazz, String fieldName) {
        return Fields.getStaticBooleanField(clazz, fieldName);
    }

    public static void setStaticBooleanField(Class<?> clazz, String fieldName, boolean value) {
        Fields.setStaticBooleanField(clazz, fieldName, value);
    }

    public static int getStaticIntField(Class<?> clazz, String fieldName) {
        return Fields.getStaticIntField(clazz, fieldName);
    }

    public static void setStaticIntField(Class<?> clazz, String fieldName, int value) {
        Fields.setStaticIntField(clazz, fieldName, value);
    }

    public static long getStaticLongField(Class<?> clazz, String fieldName) {
        return Fields.getStaticLongField(clazz, fieldName);
    }

    public static void setStaticLongField(Class<?> clazz, String fieldName, long value) {
        Fields.setStaticLongField(clazz, fieldName, value);
    }

    public static float getStaticFloatField(Class<?> clazz, String fieldName) {
        return Fields.getStaticFloatField(clazz, fieldName);
    }

    public static void setStaticFloatField(Class<?> clazz, String fieldName, float value) {
        Fields.setStaticFloatField(clazz, fieldName, value);
    }

    public static Field findField(Class<?> clazz, String fieldName) {
        return Fields.find(clazz).filterByName(fieldName).first();
    }

    public static Field findFieldIfExists(Class<?> clazz, String fieldName) {
        return Fields.find(clazz).filterByName(fieldName).firstOrNull();
    }

    public static Field findFirstFieldByExactType(Class<?> clazz, Class<?> type) {
        return Fields.find(clazz).filterByType(type).first();
    }

    public static Object getSurroundingThis(Object obj) {
        return getObjectField(obj, "this$0");
    }

    // ==================== 附加字段 ====================

    public static Object setAdditionalInstanceField(Object obj, String key, Object value) {
        return ExtraFields.setInstanceField(obj, key, value);
    }

    public static Object getAdditionalInstanceField(Object obj, String key) {
        return ExtraFields.getInstanceField(obj, key);
    }

    public static Object removeAdditionalInstanceField(Object obj, String key) {
        return ExtraFields.removeInstanceField(obj, key);
    }

    public static Object setAdditionalStaticField(Class<?> clazz, String key, Object value) {
        return ExtraFields.setStaticField(clazz, key, value);
    }

    public static Object getAdditionalStaticField(Object object, String key) {
        if (object == null) return null;
        if (object instanceof Class) return getAdditionalStaticField((Class<?>) object, key);
        return getAdditionalStaticField(object.getClass(), key);
    }

    public static Object getAdditionalStaticField(Class<?> clazz, String key) {
        return ExtraFields.getStaticField(clazz, key);
    }

    public static Object removeAdditionalStaticField(Class<?> clazz, String key) {
        return ExtraFields.removeStaticField(clazz, key);
    }

    // ==================== 方法调用 ====================

    public static Object callMethod(Object obj, String methodName, Object... args) {
        return Methods.callMethod(obj, methodName, args);
    }

    public static Object callMethod(Object obj, String methodName, Class<?>[] parameterTypes, Object... args) {
        Method method = BestMatchUtils.findMethodBestMatch(obj.getClass(), methodName, parameterTypes);
        try {
            method.setAccessible(true);
            return method.invoke(obj, args);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new IllegalStateException(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Object callStaticMethod(Class<?> clazz, String methodName, Object... args) {
        return Methods.callStaticMethod(clazz, methodName, args);
    }

    public static Object callStaticMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object... args) {
        Method method = BestMatchUtils.findMethodBestMatch(clazz, methodName, parameterTypes);
        try {
            method.setAccessible(true);
            return method.invoke(null, args);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new IllegalStateException(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Object newInstance(Class<?> clazz, Object... args) {
        return Constructors.newInstance(clazz, args);
    }

    // ==================== 方法查找 ====================

    public static Method findMethodBestMatch(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return BestMatchUtils.findMethodBestMatch(clazz, methodName, parameterTypes);
    }

    public static Method findMethodBestMatch(Class<?> clazz, String methodName, Object... args) {
        return BestMatchUtils.findMethodBestMatch(clazz, methodName, args);
    }

    public static Method findMethodExactIfExists(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return Methods.find(clazz).filterByName(methodName).filterByParamTypes(parameterTypes).firstOrNull();
    }

    public static Method findMethodExactIfExists(Class<?> clazz, String methodName, Object... parameterTypes) {
        return findMethodExactIfExists(clazz, methodName, resolveTypes(clazz, parameterTypes));
    }

    public static Method findMethodExact(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return Methods.find(clazz).filterByName(methodName).filterByParamTypes(parameterTypes).first();
    }

    public static Method findMethodExact(Class<?> clazz, String methodName, Object... parameterTypes) {
        return findMethodExact(clazz, methodName, resolveTypes(clazz, parameterTypes));
    }

    public static Method[] findMethodsByExactParameters(Class<?> clazz, Class<?> returnType, Class<?>... parameterTypes) {
        List<Method> result = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (returnType != null && method.getReturnType() != returnType) continue;
                if (!java.util.Arrays.equals(method.getParameterTypes(), parameterTypes)) continue;
                result.add(method);
            }
            current = current.getSuperclass();
        }
        return result.toArray(new Method[0]);
    }

    public static Constructor<?> findConstructorExact(Class<?> clazz, Class<?>... parameterTypes) {
        return Constructors.find(clazz).filterByParamTypes(parameterTypes).first();
    }

    public static Constructor<?> findConstructorExactIfExists(Class<?> clazz, Class<?>... parameterTypes) {
        return Constructors.find(clazz).filterByParamTypes(parameterTypes).firstOrNull();
    }

    private static Class<?>[] resolveTypes(Class<?> owner, Object[] types) {
        Class<?>[] resolved = new Class<?>[types.length];
        ClassLoader classLoader = owner.getClassLoader() != null ? owner.getClassLoader() : HookRuntime.classLoader();
        for (int i = 0; i < types.length; i++) {
            Object type = types[i];
            if (type instanceof Class<?> clazz) {
                resolved[i] = clazz;
            } else if (type instanceof String className) {
                resolved[i] = ClassUtils.loadClass(className, classLoader);
            } else {
                throw new IllegalArgumentException("Parameter type must be Class or class name String: " + type);
            }
        }
        return resolved;
    }

    // ==================== Hook ====================

    public static XC_MethodHook.Unhook findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        Object[] args = HookBridgeCompat.unwrapArgs(parameterTypesAndCallback);
        return new XC_MethodHook.Unhook(Hooks.findAndHookMethod(clazz, methodName, args));
    }

    public static XC_MethodHook.Unhook findAndHookMethod(String className, ClassLoader classLoader, String methodName,
                                                        Object... parameterTypesAndCallback) {
        Object[] args = HookBridgeCompat.unwrapArgs(parameterTypesAndCallback);
        return new XC_MethodHook.Unhook(
            Hooks.findAndHookMethod(className, classLoader, methodName, args));
    }

    public static XC_MethodHook.Unhook findAndHookConstructor(Class<?> clazz, Object... parameterTypesAndCallback) {
        Object[] args = HookBridgeCompat.unwrapArgs(parameterTypesAndCallback);
        return new XC_MethodHook.Unhook(Hooks.findAndHookConstructor(clazz, args));
    }

    public static XC_MethodHook.Unhook findAndHookConstructor(String className, ClassLoader classLoader,
                                                             Object... parameterTypesAndCallback) {
        Object[] args = HookBridgeCompat.unwrapArgs(parameterTypesAndCallback);
        return new XC_MethodHook.Unhook(Hooks.findAndHookConstructor(className, classLoader, args));
    }
}
