package com.sevtinge.hyperceiler.compat;

/**
 * legacy IXposedHookZygoteInit 的 API 102 兼容影子。
 */
public interface IXposedHookZygoteInit {
    void initZygote(StartupParam startupParam) throws Throwable;
}
