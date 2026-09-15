package com.sevtinge.hyperceiler.utils.prefs;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 本地偏好与 libxposed RemotePreferences 的桥接。
 *
 * <p>libxposed API 102 移除了 XSharedPreferences，hook 进程无法再直接读取模块
 * 私有目录下的 XML，只能通过框架的 RemotePreferences 获取配置。而模块 App 侧仍
 * 写本地 SharedPreferences，两者不通的话 hook 进程读到的永远是空配置，
 * 表现为“所有开关都不生效”。</p>
 *
 * <p>这里在服务绑定后把本地配置全量同步到远端，并监听后续改动逐条转发。</p>
 */
public final class RemotePrefsBridge {

    private static final String TAG = "HyperCeiler";

    @Nullable
    private static volatile SharedPreferences sRemote;

    @Nullable
    private static SharedPreferences sLocal;

    private static final AtomicBoolean sSyncing = new AtomicBoolean(false);

    private static final SharedPreferences.OnSharedPreferenceChangeListener sListener =
        (prefs, key) -> {
            if (sSyncing.get()) return;
            if (key == null) {
                syncAll();
                return;
            }
            putToRemote(key, null);
        };

    private RemotePrefsBridge() {
    }

    /**
     * 服务绑定后注入远端偏好句柄。
     *
     * @param remote XposedService 提供的 RemotePreferences
     * @param local  模块 App 的本地 SharedPreferences
     */
    public static void attach(@Nullable SharedPreferences remote, @Nullable SharedPreferences local) {
        sLocal = local;
        sRemote = remote;
        if (remote == null) {
            Log.w(TAG, "RemotePrefsBridge: remote prefs unavailable, hooks may read empty config.");
            return;
        }
        syncAll();
        if (local != null) {
            try {
                local.unregisterOnSharedPreferenceChangeListener(sListener);
            } catch (Throwable ignored) {
            }
            local.registerOnSharedPreferenceChangeListener(sListener);
        }
        Log.i(TAG, "RemotePrefsBridge attached, synced keys: " + remote.getAll().size());
    }

    public static void detach() {
        SharedPreferences remote = sRemote;
        sRemote = null;
        if (sLocal != null) {
            try {
                sLocal.unregisterOnSharedPreferenceChangeListener(sListener);
            } catch (Throwable ignored) {
            }
        }
        if (remote != null) Log.i(TAG, "RemotePrefsBridge detached.");
    }

    /** 把本地配置全量推送到远端。 */
    public static void syncAll() {
        SharedPreferences remote = sRemote;
        SharedPreferences local = sLocal;
        if (remote == null || local == null) return;
        sSyncing.set(true);
        try {
            Map<String, ?> all = local.getAll();
            SharedPreferences.Editor editor = remote.edit();
            for (Map.Entry<String, ?> entry : all.entrySet()) {
                putValue(editor, entry.getKey(), entry.getValue());
            }
            editor.commit();
            Log.i(TAG, "RemotePrefsBridge: synced " + all.size() + " keys to remote.");
        } catch (Throwable t) {
            Log.e(TAG, "RemotePrefsBridge: sync failed.", t);
        } finally {
            sSyncing.set(false);
        }
    }

    /** 转发单个键（value 为 null 时从本地读取）。 */
    public static void putToRemote(@NonNull String key, @Nullable Object value) {
        SharedPreferences remote = sRemote;
        SharedPreferences local = sLocal;
        if (remote == null) return;
        sSyncing.set(true);
        try {
            Object resolved = value;
            if (resolved == null && local != null) {
                resolved = local.getAll().get(key);
            }
            SharedPreferences.Editor editor = remote.edit();
            if (resolved == null) {
                editor.remove(key);
            } else {
                putValue(editor, key, resolved);
            }
            editor.commit();
        } catch (Throwable t) {
            Log.e(TAG, "RemotePrefsBridge: put " + key + " failed.", t);
        } finally {
            sSyncing.set(false);
        }
    }

    private static void putValue(@NonNull SharedPreferences.Editor editor, String key, @Nullable Object value) {
        if (value == null) {
            editor.remove(key);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Set) {
            Set<String> copy = new LinkedHashSet<>();
            for (Object item : (Set<?>) value) {
                if (item instanceof String) copy.add((String) item);
            }
            editor.putStringSet(key, copy);
        }
    }

    /** hook 进程回写状态，供 App 侧调试信息展示。 */
    public static void markHookSeen(String pkg, String value) {
        SharedPreferences remote = sRemote;
        if (remote == null) return;
        try {
            remote.edit().putString("__hc_seen_" + pkg, value).commit();
        } catch (Throwable ignored) {
        }
    }

    /** 汇总远端里所有 hook 侧诊断项。 */
    @NonNull
    public static String collectDiagnostics() {
        SharedPreferences remote = sRemote;
        if (remote == null) return "remote=null";
        StringBuilder sb = new StringBuilder();
        try {
            for (Map.Entry<String, ?> e : remote.getAll().entrySet()) {
                String k = e.getKey();
                if (k != null && (k.startsWith("__hc_diag_") || k.startsWith("__hc_seen_"))) {
                    if (sb.length() > 0) sb.append(" | ");
                    sb.append(k.replace("__hc_diag_", "").replace("__hc_seen_", ""))
                        .append("=").append(e.getValue());
                }
            }
        } catch (Throwable t) {
            return "err:" + t;
        }
        return sb.length() == 0 ? "none" : sb.toString();
    }

    /** 远端偏好键数量（排除诊断键）。 */
    public static int remoteKeyCount() {
        SharedPreferences remote = sRemote;
        if (remote == null) return -1;
        try {
            int n = 0;
            for (String k : remote.getAll().keySet()) {
                if (k == null || (!k.startsWith("__hc_"))) n++;
            }
            return n;
        } catch (Throwable t) {
            return -2;
        }
    }

    public static boolean isReady() {
        return sRemote != null;
    }

    @Nullable
    public static SharedPreferences remote() {
        return sRemote;
    }
}
