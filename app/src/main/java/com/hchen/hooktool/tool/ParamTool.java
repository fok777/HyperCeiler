package com.hchen.hooktool.tool;

import java.lang.reflect.Method;

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

    default Object getThisObject() {
        return state().thisObject;
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
