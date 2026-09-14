package com.hchen.hooktool.log;

import com.sevtinge.hyperceiler.compat.XposedBridge;

/**
 * HChenX HookTool 的 XposedLog 兼容实现（API 102）。
 */
public final class XposedLog {

    private XposedLog() {
    }

    public static void logI(String tag, Object message) {
        XposedBridge.log("[HyperCeiler][I/" + tag + "]: " + message);
    }

    public static void logE(String tag, Object message) {
        XposedBridge.log("[HyperCeiler][E/" + tag + "]: " + message);
    }

    public static void logD(String tag, Object message) {
        XposedBridge.log("[HyperCeiler][D/" + tag + "]: " + message);
    }

    public static void logW(String tag, Object message) {
        XposedBridge.log("[HyperCeiler][W/" + tag + "]: " + message);
    }
}
