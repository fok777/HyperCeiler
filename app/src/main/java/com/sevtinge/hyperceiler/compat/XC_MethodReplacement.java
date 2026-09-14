package com.sevtinge.hyperceiler.compat;

import io.github.lingqiqi5211.ezhooktool.xposed.common.HookParam;
import io.github.lingqiqi5211.ezhooktool.xposed.java.IReplaceHook;

/**
 * legacy XC_MethodReplacement 的 API 102 兼容影子。
 */
public abstract class XC_MethodReplacement extends XC_MethodHook {

    public XC_MethodReplacement() {
        super();
    }

    public XC_MethodReplacement(int priority) {
        super(priority);
    }

    protected abstract Object replaceHookedMethod(MethodHookParam param) throws Throwable;

    @Override
    protected final void before(MethodHookParam param) throws Throwable {
        try {
            param.setResult(replaceHookedMethod(param));
        } catch (Throwable t) {
            param.setThrowable(t);
        }
    }

    public IReplaceHook asReplaceHook() {
        return new IReplaceHook() {
            @Override
            public Object replace(HookParam hookParam) {
                MethodHookParam param = new MethodHookParam(hookParam);
                try {
                    return replaceHookedMethod(param);
                } catch (Throwable t) {
                    HookBridgeCompat.logHookError("replace", t);
                    return null;
                }
            }
        };
    }

    public static XC_MethodReplacement returnConstant(final Object result) {
        return new XC_MethodReplacement(PRIORITY_DEFAULT) {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) {
                return result;
            }
        };
    }

    public static final XC_MethodReplacement DO_NOTHING = new XC_MethodReplacement(PRIORITY_HIGHEST) {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) {
            return null;
        }
    };
}
