package com.sevtinge.hyperceiler.compat;

/**
 * legacy IXposedHookLoadPackage 的 API 102 兼容影子。
 */
public interface IXposedHookLoadPackage {
    void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable;
}
