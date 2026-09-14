package com.hchen.hooktool.tool;

import java.lang.reflect.Method;
import io.github.libxposed.api.XposedInterface;

/**
 * HChenX HookTool 的 ParamTool 兼容实现（API 102）。
 *
 * <p>当前调用的 hook 参数通过 ThreadLocal 传递，因此 {@code IHook} 实现类可以
 * 直接在 before / after 中调用 getArgs、setResult 等方法。</p>
 */
public interface ParamTool {

    ThreadLocal<HookState> STATE = new ThreadLocal<>();

    final class HookState {
        public Object[] args;
        public Object thisObject;
        public Object result;
        public Throwable throwable;
        public Method method;
        public XposedInterface.HookHandle handle;
    }

    static HookState state() {
        HookState state = STATE.get();
        if (state == null) throw new IllegalStateException("No hook param in current thread.");
        return state;
    }

    default Object[] getArgs() {
        return state().args;
    }

    default Object getArgs(int index) {
        return state().args[index];
    }

    default void setArgs(int index, Object value) {
        state().args[index] = value;
    }

    /** 当前实例别名。 */
    default Object thisObject() {
        return state().thisObject;
    }

    default <T> T thisObjectAs() {
        return (T) state().thisObject;
    }

    default Object getThisObject() {
        return state().thisObject;
    }

    default <T> T getResultAs() {
        return (T) state().result;
    }

    default Object getResult() {
        return state().result;
    }

    default void setResult(Object result) {
        HookState state = state();
        state.result = result;
        state.throwable = null;
    }

    default void returnNull() {
        setResult(null);
    }

    default void returnResult(Object result) {
        setResult(result);
    }

    default void setThrowable(Throwable throwable) {
        HookState state = state();
        state.throwable = throwable;
        state.result = null;
    }

    default Object getField(String fieldName) {
        return io.github.lingqiqi5211.ezhooktool.core.java.Fields.getObjectField(getThisObject(), fieldName);
    }

    /** 读取指定对象的字段。 */
    default Object getField(Object obj, String fieldName) {
        return io.github.lingqiqi5211.ezhooktool.core.java.Fields.getObjectField(obj, fieldName);
    }

    /** 按 Field 对象读取。 */
    default Object getField(Object obj, java.lang.reflect.Field field) {
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (Throwable t) {
            return null;
        }
    }

    default void setField(Object obj, java.lang.reflect.Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Throwable ignored) {
        }
    }

    /** 读取 this 对象字段并强制转型。 */
    default <T> T getThisField(String fieldName) {
        return (T) io.github.lingqiqi5211.ezhooktool.core.java.Fields.getObjectField(getThisObject(), fieldName);
    }

    /** 读取指定对象字段并强制转型。 */
    default <T> T getFieldAs(Object obj, String fieldName) {
        return (T) io.github.lingqiqi5211.ezhooktool.core.java.Fields.getObjectField(obj, fieldName);
    }

    default void setField(Object obj, String fieldName, Object value) {
        io.github.lingqiqi5211.ezhooktool.core.java.Fields.setObjectField(obj, fieldName, value);
    }

    default Object callMethod(Object obj, String methodName, Object... args) {
        return io.github.lingqiqi5211.ezhooktool.core.java.Methods.callMethod(obj, methodName, args);
    }

    default <T> T callMethodAs(Object obj, String methodName, Object... args) {
        return (T) io.github.lingqiqi5211.ezhooktool.core.java.Methods.callMethod(obj, methodName, args);
    }

    default Object callStaticMethod(Class<?> clazz, String methodName, Object... args) {
        return io.github.lingqiqi5211.ezhooktool.core.java.Methods.callStaticMethod(clazz, methodName, args);
    }

    /** 按类名调用静态方法。 */
    default Object callStaticMethod(String className, String methodName, Object... args) {
        Class<?> clazz = io.github.lingqiqi5211.ezhooktool.core.ClassUtils.loadClassOrNull(
            className, com.sevtinge.hyperceiler.compat.HookRuntime.classLoader());
        return clazz == null ? null
            : io.github.lingqiqi5211.ezhooktool.core.java.Methods.callStaticMethod(clazz, methodName, args);
    }

    /** 空操作回调。 */
    default com.hchen.hooktool.hook.IHook doNothing() {
        return new com.hchen.hooktool.hook.IHook() {
        };
    }

    default void setField(String fieldName, Object value) {
        io.github.lingqiqi5211.ezhooktool.core.java.Fields.setObjectField(getThisObject(), fieldName, value);
    }

    default Object callMethod(String methodName, Object... args) {
        return io.github.lingqiqi5211.ezhooktool.core.java.Methods.callMethod(getThisObject(), methodName, args);
    }

    default void logI(String tag, Object message) {
        com.sevtinge.hyperceiler.compat.XposedBridge.log("[HyperCeiler][I/" + tag + "]: " + message);
    }

    default void logE(String tag, Object message) {
        com.sevtinge.hyperceiler.compat.XposedBridge.log("[HyperCeiler][E/" + tag + "]: " + message);
    }
}
