package com.sevtinge.hyperceiler.compat;

/**
 * legacy IXposedHookZygoteInit 的 API 102 兼容影子。
 */
public interface IXposedHookZygoteInit {
    void initZygote(StartupParam startupParam) throws Throwable;

    /** 兼容 legacy 的嵌套命名。 */
    class StartupParam extends com.sevtinge.hyperceiler.compat.StartupParam {
        public StartupParam() {
            super();
        }

        public StartupParam(String modulePath) {
            super(modulePath);
        }
    }
}
