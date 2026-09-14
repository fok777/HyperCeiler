package com.hchen.hooktool;

import com.sevtinge.hyperceiler.compat.StartupParam;
import com.sevtinge.hyperceiler.compat.XC_LoadPackage;

/**
 * HChenX HookTool 的 HCInit 兼容实现（API 102）。
 */
public final class HCInit {

    private HCInit() {
    }

    public static class BasicData {
        public BasicData setModulePackageName(String name) {
            return this;
        }

        public BasicData setLogLevel(int level) {
            return this;
        }

        public BasicData setTag(String tag) {
            return this;
        }

        public BasicData setPrefsName(String name) {
            return this;
        }

        public BasicData setAutoReload(boolean autoReload) {
            return this;
        }

        public BasicData setLogExpandPath(String path) {
            return this;
        }
    }

    public static void initBasicData(BasicData data) {
        // API 102 下由模块入口统一初始化。
    }

    public static void initBasicData() {
        initBasicData(new BasicData());
    }

    public static void initStartupParam(StartupParam startupParam) {
        // 兼容占位：模块路径由入口提供。
    }

    public static void initLoadPackageParam(XC_LoadPackage.LoadPackageParam lpparam) {
        // 兼容占位：Class loader 由 EzHookTool 运行时提供。
    }
}
