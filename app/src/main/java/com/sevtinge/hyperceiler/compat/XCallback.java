package com.sevtinge.hyperceiler.compat;

/**
 * legacy XCallback 的 API 102 兼容影子（仅保留优先级常量）。
 */
public class XCallback {

    public static final int PRIORITY_DEFAULT = io.github.libxposed.api.XposedInterface.PRIORITY_DEFAULT;
    public static final int PRIORITY_HIGHEST = io.github.libxposed.api.XposedInterface.PRIORITY_HIGHEST;
    public static final int PRIORITY_LOWEST = io.github.libxposed.api.XposedInterface.PRIORITY_LOWEST;

    public XCallback() {
    }
}
