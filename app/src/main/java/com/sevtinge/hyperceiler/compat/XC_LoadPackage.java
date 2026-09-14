package com.sevtinge.hyperceiler.compat;

import android.content.pm.ApplicationInfo;

/**
 * legacy XC_LoadPackage 的 API 102 兼容影子。
 */
public class XC_LoadPackage {

    public static class LoadPackageParam {
        public String packageName;
        public String processName;
        public ClassLoader classLoader;
        public ApplicationInfo appInfo;
        public boolean isFirstApplication;

        public LoadPackageParam() {
        }

        public LoadPackageParam(String packageName, String processName, ClassLoader classLoader,
                                ApplicationInfo appInfo, boolean isFirstApplication) {
            this.packageName = packageName;
            this.processName = processName;
            this.classLoader = classLoader;
            this.appInfo = appInfo;
            this.isFirstApplication = isFirstApplication;
        }
    }
}
