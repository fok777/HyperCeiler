package com.sevtinge.hyperceiler.compat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import io.github.lingqiqi5211.ezhooktool.core.java.Methods;

/**
 * 方法查找的小工具，供兼容层内部使用。
 */
public final class MethodsCompat {

    private MethodsCompat() {
    }

    public static Method[] findAllMethods(Class<?> clazz, String methodName) {
        List<Method> list = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (methodName == null || methodName.equals(method.getName())) {
                    boolean exists = false;
                    for (Method added : list) {
                        if (added.getName().equals(method.getName())
                            && java.util.Arrays.equals(added.getParameterTypes(), method.getParameterTypes())) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) list.add(method);
                }
            }
            current = current.getSuperclass();
        }
        try {
            return Methods.find(clazz).filterByName(methodName).toList().toArray(new Method[0]);
        } catch (Throwable t) {
            return list.toArray(new Method[0]);
        }
    }
}
