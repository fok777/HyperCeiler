/*
 * This file is part of HyperCeiler.

 * HyperCeiler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2025 HyperCeiler Contributions
 */
package com.sevtinge.hyperceiler;

import static com.sevtinge.hyperceiler.module.base.tool.AppsTool.getPackageVersionCode;
import static com.sevtinge.hyperceiler.module.base.tool.AppsTool.getPackageVersionName;
import static com.sevtinge.hyperceiler.utils.devicesdk.MiDeviceAppUtilsKt.isPad;
import static com.sevtinge.hyperceiler.utils.devicesdk.SystemSDKKt.getAndroidVersion;
import static com.sevtinge.hyperceiler.utils.devicesdk.SystemSDKKt.getHyperOSVersion;
import static com.sevtinge.hyperceiler.utils.devicesdk.SystemSDKKt.getMiuiVersion;
import static com.sevtinge.hyperceiler.utils.devicesdk.SystemSDKKt.isAndroidVersion;
import static com.sevtinge.hyperceiler.utils.devicesdk.SystemSDKKt.isHyperOSVersion;
import static com.sevtinge.hyperceiler.utils.devicesdk.SystemSDKKt.isMiuiVersion;
import static com.sevtinge.hyperceiler.utils.log.LogManager.logLevelDesc;
import static com.sevtinge.hyperceiler.utils.log.XposedLogUtils.logE;
import static com.sevtinge.hyperceiler.utils.log.XposedLogUtils.logI;
import static com.sevtinge.hyperceiler.utils.prefs.PrefsUtils.mPrefsMap;

import android.os.Process;

import com.github.kyuubiran.ezxhelper.EzXHelper;
import com.hchen.hooktool.HCInit;
import com.sevtinge.hyperceiler.module.app.VariousThirdApps;
import com.sevtinge.hyperceiler.module.base.BaseModule;
import com.sevtinge.hyperceiler.module.base.DataBase;
import com.sevtinge.hyperceiler.module.base.tool.ResourcesTool;
import com.sevtinge.hyperceiler.module.hook.systemframework.AllowManageAllNotifications;
import com.sevtinge.hyperceiler.module.hook.systemframework.AllowUninstall;
import com.sevtinge.hyperceiler.module.hook.systemframework.BackgroundBlurDrawable;
import com.sevtinge.hyperceiler.module.hook.systemframework.CleanOpenMenu;
import com.sevtinge.hyperceiler.module.hook.systemframework.CleanShareMenu;
import com.sevtinge.hyperceiler.module.hook.systemframework.ScreenRotation;
import com.sevtinge.hyperceiler.module.hook.systemframework.ToastBlur;
import com.sevtinge.hyperceiler.module.hook.systemframework.UnlockAlwaysOnDisplay;
import com.sevtinge.hyperceiler.module.hook.systemsettings.VolumeSeparateControlForSettings;
import com.sevtinge.hyperceiler.module.skip.SystemFrameworkForCorePatch;
import com.sevtinge.hyperceiler.safe.CrashHook;
import com.sevtinge.hyperceiler.utils.api.ProjectApi;
import com.sevtinge.hyperceiler.utils.log.LogManager;
import com.sevtinge.hyperceiler.utils.prefs.PrefsUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import com.sevtinge.hyperceiler.compat.IXposedHookLoadPackage;
import com.sevtinge.hyperceiler.compat.IXposedHookZygoteInit;
import com.sevtinge.hyperceiler.compat.XSharedPreferences;
import com.sevtinge.hyperceiler.compat.XposedBridge;
import com.sevtinge.hyperceiler.compat.XposedHelpers;
import com.sevtinge.hyperceiler.compat.StartupParam;
import com.sevtinge.hyperceiler.compat.XC_LoadPackage;

public class XposedInit {
    private static final String TAG = "HyperCeiler";
    public static String mModulePath = null;
    public static ResourcesTool mResHook;

    // public static XmlTool mXmlTool;
    public final VariousThirdApps mVariousThirdApps = new VariousThirdApps();

    public void initZygote(StartupParam startupParam) throws Throwable {
        // load ResourcesTool
        mResHook = new ResourcesTool(startupParam.modulePath);
        mModulePath = startupParam.modulePath;
        // mXmlTool = new XmlTool(startupParam);

        // 尽早加载配置：后续任一初始化抛异常都不会影响模块设置读取。
        setXSharedPrefs();

        // load EzXHelper and set log tag
        EzXHelper.initZygote(startupParam);
        EzXHelper.setLogTag(TAG);
        EzXHelper.setToastTag(TAG);

        // load HCInit
        HCInit.initBasicData(new HCInit.BasicData()
                .setModulePackageName(BuildConfig.APPLICATION_ID)
                .setLogLevel(LogManager.getLogLevel())
                .setTag("HyperCeiler")
        );
        HCInit.initStartupParam(startupParam);

        // load CorePatch
        new SystemFrameworkForCorePatch().initZygote(startupParam);
        // load ZygoteHook
        loadZygoteHook(startupParam);
    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (isInSafeMode(lpparam.packageName)) return;

        // 必须最先加载：包级 hook 全部依赖 mPrefsMap，不能依赖 initZygote 是否成功。
        // 方法内部有 isEmpty 保护，重复调用无副作用。
        setXSharedPrefs();

        // load EzXHelper and set log tag
        EzXHelper.initHandleLoadPackage(lpparam);
        EzXHelper.setLogTag(TAG);
        EzXHelper.setToastTag(TAG);

        // load CorePatch
        new SystemFrameworkForCorePatch().handleLoadPackage(lpparam);
        // load Module hook apps
        init(lpparam);
    }

    private static void loadZygoteHook(StartupParam startupParam) throws Throwable {
        if (mPrefsMap.getBoolean("system_framework_screen_all_rotations")) ScreenRotation.initRes();
        if (mPrefsMap.getBoolean("system_framework_clean_share_menu")) CleanShareMenu.initRes();
        if (mPrefsMap.getBoolean("system_framework_clean_open_menu")) CleanOpenMenu.initRes();
        if (mPrefsMap.getBoolean("system_framework_volume_separate_control"))
            VolumeSeparateControlForSettings.initRes();

        if (startupParam != null) {
            new BackgroundBlurDrawable().initZygote(startupParam);

            if (mPrefsMap.getBoolean("system_framework_allow_uninstall"))
                new AllowUninstall().initZygote(startupParam);
            if (mPrefsMap.getBoolean("system_framework_allow_manage_all_notifications"))
                new AllowManageAllNotifications().initZygote(startupParam);
            if (mPrefsMap.getBoolean("system_framework_background_blur_toast"))
                new ToastBlur().initZygote(startupParam);
            if (mPrefsMap.getBoolean("aod_unlock_always_on_display_hyper"))
                new UnlockAlwaysOnDisplay().initZygote(startupParam);
        }
    }

    private void setXSharedPrefs() {
        if (mPrefsMap.isEmpty()) {
            XSharedPreferences mXSharedPreferences;
            try {
                mXSharedPreferences = new XSharedPreferences(ProjectApi.mAppModulePkg, PrefsUtils.mPrefsName);
                mXSharedPreferences.makeWorldReadable();
                Map<String, ?> allPrefs = mXSharedPreferences.getAll();

                if (allPrefs != null && !allPrefs.isEmpty()) {
                    mPrefsMap.putAll(allPrefs);
                } else {
                    mXSharedPreferences = new XSharedPreferences(new File(PrefsUtils.mPrefsFile));
                    mXSharedPreferences.makeWorldReadable();
                    allPrefs = mXSharedPreferences.getAll();

                    if (allPrefs != null && !allPrefs.isEmpty()) {
                        mPrefsMap.putAll(allPrefs);
                    } else {
                        logE("[UID" + Process.myUid() + "]", "Cannot read SharedPreferences, some mods might not work!");
                    }
                }
            } catch (Throwable t) {
                logE("setXSharedPrefs", t);
            }
        }
        logLoadedPrefs();
        reportHookDiagnostics();
    }

    /**
     * 输出 hook 进程实际读到的配置。
     *
     * <p>判断功能是否可能生效的关键：若这里打印的 keys 很少、或开关值为 false，
     * 说明 hook 进程没有读到用户在界面上做的设置，功能自然不会生效。</p>
     */
    private void logLoadedPrefs() {
        try {
            int size = mPrefsMap.size();
            StringBuilder sb = new StringBuilder();
            sb.append("[HyperCeiler][I][PREFS] pkg=").append(sCurrentPkg)
                .append(" keys=").append(size);
            int shown = 0;
            for (String key : mPrefsMap.keySet()) {
                Object value = mPrefsMap.get(key);
                if (Boolean.TRUE.equals(value)) {
                    sb.append(" | ON:").append(key);
                    if (++shown >= 8) break;
                }
            }
            if (shown == 0) sb.append(" | (no switch is ON)");
            sb.append(" | XLDOWNLOAD=").append(mPrefsMap.getBoolean("various_fuck_xlDownload"));
            XposedBridge.log(sb.toString());
        } catch (Throwable t) {
            logE("logLoadedPrefs", t);
        }
    }

    private static volatile String sCurrentPkg = "null";

    /**
     * 把 hook 侧的关键状态写回远程偏好，便于在模块 App 的调试信息页查看。
     * hook 进程与 App 进程唯一可靠的共享通道就是 RemotePreferences。
     */
    private void reportHookDiagnostics() {
        String lp = sCurrentPkg;
        int keys = mPrefsMap.size();
        int ok = io.github.lingqiqi5211.ezhooktool.xposed.java.Hooks.sOk.get();
        int fail = io.github.lingqiqi5211.ezhooktool.xposed.java.Hooks.sFail.get();
        String value = "keys=" + keys + ",hookOk=" + ok + ",hookFail=" + fail;
        logI(TAG, "HyperCeilerDiag[" + lp + "] " + value);
        try {
            android.content.SharedPreferences remote =
                com.sevtinge.hyperceiler.compat.HookRuntime.remotePreferences(PrefsUtils.mPrefsName);
            if (remote != null) {
                remote.edit().putString("__hc_diag_" + lp, value).commit();
            }
        } catch (Throwable t) {
            logE("reportHookDiagnostics", t);
        }
        com.sevtinge.hyperceiler.utils.prefs.RemotePrefsBridge.markHookSeen(lp, value);
    }

    public void init(XC_LoadPackage.LoadPackageParam lpparam) {
        String packageName = lpparam.packageName;
        if (Objects.equals(packageName, "android"))
            logI(packageName, "androidVersion = " + getAndroidVersion() + ", miuiVersion = " + getMiuiVersion() + ", hyperosVersion = " + getHyperOSVersion());
        else
            logI(packageName, "versionName = " + getPackageVersionName(lpparam) + ", versionCode = " + getPackageVersionCode(lpparam));

        invokeInit(lpparam);
        androidCrashEventHook(lpparam);
    }

    private void invokeInit(XC_LoadPackage.LoadPackageParam lpparam) {
        String mPkgName = lpparam.packageName;
        if (mPkgName == null) return;
        sCurrentPkg = mPkgName;

        if (ProjectApi.mAppModulePkg.equals(mPkgName)) {
            moduleActiveHook(lpparam);
            return;
        }

        if (isOtherRestrictions(mPkgName)) return;

        HashMap<String, DataBase> dataMap = DataBase.get();
        if (dataMap.values().stream().noneMatch(dataBase -> dataBase.mTargetPackage.equals(mPkgName))) {
            mVariousThirdApps.init(lpparam);
            return;
        }

        dataMap.forEach(new BiConsumer<>() {
            @Override
            public void accept(String s, DataBase dataBase) {
                if (!mPkgName.equals(dataBase.mTargetPackage))
                    return;
                if (!(dataBase.mTargetSdk == -1) && !isAndroidVersion(dataBase.mTargetSdk))
                    return;
                if (!(dataBase.mTargetOSVersion == -1F) && !(isHyperOSVersion(dataBase.mTargetOSVersion) || isMiuiVersion(dataBase.mTargetOSVersion)))
                    return;
                if ((dataBase.isPad == 1 && !isPad()) || (dataBase.isPad == 2 && isPad()))
                    return;

                try {
                    Class<?> clazz = Objects.requireNonNull(getClass().getClassLoader()).loadClass(s);
                    BaseModule module = (BaseModule) clazz.getDeclaredConstructor().newInstance();
                    module.init(lpparam);
                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                         InstantiationException | InvocationTargetException e) {
                    logE(TAG, e);
                }
            }
        });
    }

    private void androidCrashEventHook(XC_LoadPackage.LoadPackageParam lpparam) {
        if ("android".equals(lpparam.packageName)) {
            XposedBridge.log("[HyperCeiler][I]: Log level is " + logLevelDesc());
            try {
                new CrashHook(lpparam);
            } catch (Exception e) {
                logE(TAG, e);
            }
        }
    }

    private boolean isInSafeMode(String pkg) {
        switch (pkg) {
            case "com.android.systemui" -> {
                return isSystemUIModuleEnable();
            }
            case "com.miui.home" -> {
                return isHomeModuleEnable();
            }
            case "com.miui.securitycenter" -> {
                return isSecurityCenterModuleEnable();
            }
        }
        return false;
    }

    private boolean isOtherRestrictions(String pkg) {
        switch (pkg) {
            case "com.google.android.webview", "com.miui.contentcatcher",
                 "com.miui.catcherpatch" -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public void moduleActiveHook(XC_LoadPackage.LoadPackageParam lpparam) {
        Class<?> AppsTool = XposedHelpers.findClassIfExists(ProjectApi.mAppModulePkg + ".module.base.tool.AppsTool", lpparam.classLoader);

        if (AppsTool == null) {
            // API 102 不再把模块注入自身进程，激活状态由 Application 通过
            // XposedServiceHelper 回调维护，这里不再需要自 hook。
            logI(TAG, "Module self-hook is unavailable on API 102; activation state is driven by XposedService.");
            return;
        }
        XposedHelpers.setStaticBooleanField(AppsTool, "isModuleActive", true);
        XposedHelpers.setStaticIntField(AppsTool, "XposedVersion", XposedBridge.getXposedVersion());
        XposedBridge.log("[HyperCeiler][I]: Log level is " + logLevelDesc());
    }


    private boolean isSafeModeEnable(String key) {
        return mPrefsMap.getBoolean(key);
    }

    private boolean isSystemUIModuleEnable() {
        return isSafeModeEnable("system_ui_safe_mode_enable");
    }

    private boolean isHomeModuleEnable() {
        return isSafeModeEnable("home_safe_mode_enable");
    }

    private boolean isSecurityCenterModuleEnable() {
        return isSafeModeEnable("security_center_safe_mode_enable");
    }
}
