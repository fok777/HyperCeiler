package com.sevtinge.hyperceiler.compat;

/**
 * legacy StartupParam 的 API 102 兼容影子。
 */
public class StartupParam {
    public String modulePath;
    /** legacy：是否启动 SystemServer。 */
    public boolean startsSystemServer = true;
    /** legacy：是否允许其它模块 hook 自身。 */
    public boolean isFirstApplication = true;

    public StartupParam() {
    }

    public StartupParam(String modulePath) {
        this.modulePath = modulePath;
    }
}
