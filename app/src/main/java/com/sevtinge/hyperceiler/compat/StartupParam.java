package com.sevtinge.hyperceiler.compat;

/**
 * legacy IXposedHookZygoteInit.StartupParam 的 API 102 兼容影子。
 */
public class StartupParam {
    public String modulePath;

    public StartupParam() {
    }

    public StartupParam(String modulePath) {
        this.modulePath = modulePath;
    }
}
