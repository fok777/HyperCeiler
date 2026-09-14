package com.sevtinge.hyperceiler.compat;

import java.lang.reflect.Member;

import io.github.libxposed.api.XposedInterface;
import io.github.lingqiqi5211.ezhooktool.xposed.common.HookParam;
import io.github.lingqiqi5211.ezhooktool.xposed.java.ExtraFields;
import io.github.lingqiqi5211.ezhooktool.xposed.java.IMethodHook;

/**
 * legacy XC_MethodHook 的 API 102 兼容影子。
 *
 * <p>保留 before / after 两段式语义，内部转成 EzHookTool 的 {@link IMethodHook}。</p>
 */
public abstract class XC_MethodHook {

    public static final int PRIORITY_DEFAULT = XposedInterface.PRIORITY_DEFAULT;
    public static final int PRIORITY_HIGHEST = XposedInterface.PRIORITY_HIGHEST;
    public static final int PRIORITY_LOWEST = XposedInterface.PRIORITY_LOWEST;

    private final int mPriority;

    public XC_MethodHook() {
        this(PRIORITY_DEFAULT);
    }

    public XC_MethodHook(int priority) {
        mPriority = priority;
    }

    public final int getPriority() {
        return mPriority;
    }

    protected void before(MethodHookParam param) throws Throwable {
    }

    protected void after(MethodHookParam param) throws Throwable {
    }

    /** legacy 命名：默认转发到 {@link #before}。 */
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        before(param);
    }

    /** legacy 命名：默认转发到 {@link #after}。 */
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        after(param);
    }

    /** 转成 EzHookTool 的回调。 */
    public IMethodHook asHook() {
        return new IMethodHook() {
            @Override
            public void before(HookParam hookParam) {
                MethodHookParam param = new MethodHookParam(hookParam);
                try {
                    XC_MethodHook.this.beforeHookedMethod(param);
                } catch (Throwable t) {
                    HookBridgeCompat.logHookError("before", t);
                }
            }

            @Override
            public void after(HookParam hookParam) {
                MethodHookParam param = new MethodHookParam(hookParam);
                try {
                    XC_MethodHook.this.afterHookedMethod(param);
                } catch (Throwable t) {
                    HookBridgeCompat.logHookError("after", t);
                }
            }
        };
    }

    /**
     * legacy XC_MethodHook.MethodHookParam 的兼容实现。
     *
     * <p>字段 thisObject / args 与底层 HookParam 共享同一引用，原地修改参数有效；
     * 整体替换 args 数组不生效（libxposed 102 不支持）。</p>
     */
    public static class MethodHookParam {
        public Member method;
        public Object thisObject;
        public Object[] args;

        private final HookParam mParam;

        public MethodHookParam(HookParam param) {
            mParam = param;
            method = param.getExecutable();
            Object self = null;
            try {
                self = param.getThisObjectOrNull();
            } catch (Throwable ignored) {
            }
            thisObject = self;
            args = param.getArgs();
        }

        public HookParam raw() {
            return mParam;
        }

        public Object getResult() {
            return mParam.getResult();
        }

        public void setResult(Object result) {
            mParam.setResult(result);
        }

        public Throwable getThrowable() {
            return mParam.getThrowable();
        }

        public boolean hasThrowable() {
            return mParam.getHasThrowable();
        }

        public void setThrowable(Throwable throwable) {
            mParam.setThrowable(throwable);
        }

        public Object getResultOrThrowable() throws Throwable {
            if (mParam.getHasThrowable()) throw mParam.getThrowable();
            return mParam.getResult();
        }

        public Object[] getArgs() {
            return mParam.getArgs();
        }

        public void setObjectExtra(String key, Object value) {
            ExtraFields.setInstanceField(thisObject, key, value);
        }

        public Object getObjectExtra(String key) {
            return ExtraFields.getInstanceField(thisObject, key);
        }

        public Object removeObjectExtra(String key) {
            return ExtraFields.removeInstanceField(thisObject, key);
        }
    }

    /** legacy Unhook 占位。 */
    public static class Unhook {
        private final XposedInterface.HookHandle mHandle;

        public Unhook(XposedInterface.HookHandle handle) {
            mHandle = handle;
        }

        public void unhook() {
            if (mHandle != null) mHandle.unhook();
        }

        public XposedInterface.HookHandle getHandle() {
            return mHandle;
        }
    }
}
