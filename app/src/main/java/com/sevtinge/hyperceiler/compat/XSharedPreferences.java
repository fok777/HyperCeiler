package com.sevtinge.hyperceiler.compat;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;

import com.sevtinge.hyperceiler.utils.api.ProjectApi;

/**
 * legacy XSharedPreferences 的 API 102 兼容实现。
 *
 * <p>优先使用 libxposed 的 RemotePreferences；不可用时回退到模块自身进程的
 * SharedPreferences 或直接解析模块私有文件。</p>
 */
public class XSharedPreferences {

    private final String mGroupName;
    private final File mFile;
    @Nullable
    private SharedPreferences mPrefs;

    public XSharedPreferences(String packageName, String prefsName) {
        mGroupName = prefsName;
        mFile = null;
        reload();
    }

    public XSharedPreferences(File prefFile) {
        mFile = prefFile;
        mGroupName = prefFile != null ? prefFile.getName().replace(".xml", "") : null;
        reload();
    }

    public void reload() {
        mPrefs = null;
        if (mGroupName != null) {
            SharedPreferences remote = HookRuntime.remotePreferences(mGroupName);
            if (remote != null) {
                mPrefs = remote;
                return;
            }
        }
        // 仅在模块自身进程中回退读取本地文件；hook 进程里切不可读宿主的 SharedPreferences，
        // 否则会拿到宿主同名的空配置，表现为所有开关失效。
        Context context = HookRuntime.appContext();
        if (context != null && mGroupName != null
            && ProjectApi.mAppModulePkg.equals(context.getPackageName())) {
            try {
                mPrefs = context.getSharedPreferences(mGroupName, Context.MODE_PRIVATE);
            } catch (Throwable ignored) {
            }
        }
    }

    public void makeWorldReadable() {
        // API 102 使用 RemotePreferences，不再需要放宽文件权限。
    }

    @Nullable
    private SharedPreferences prefs() {
        if (mPrefs == null) reload();
        return mPrefs;
    }

    public Map<String, ?> getAll() {
        SharedPreferences prefs = prefs();
        if (prefs == null) return new HashMap<>();
        try {
            return new HashMap<>(prefs.getAll());
        } catch (Throwable t) {
            return new HashMap<>();
        }
    }

    public boolean getBoolean(String key, boolean defValue) {
        SharedPreferences prefs = prefs();
        return prefs == null ? defValue : prefs.getBoolean(key, defValue);
    }

    public int getInt(String key, int defValue) {
        SharedPreferences prefs = prefs();
        return prefs == null ? defValue : prefs.getInt(key, defValue);
    }

    public long getLong(String key, long defValue) {
        SharedPreferences prefs = prefs();
        return prefs == null ? defValue : prefs.getLong(key, defValue);
    }

    public float getFloat(String key, float defValue) {
        SharedPreferences prefs = prefs();
        return prefs == null ? defValue : prefs.getFloat(key, defValue);
    }

    public String getString(String key, String defValue) {
        SharedPreferences prefs = prefs();
        return prefs == null ? defValue : prefs.getString(key, defValue);
    }

    public Set<String> getStringSet(String key, Set<String> defValue) {
        SharedPreferences prefs = prefs();
        return prefs == null ? defValue : prefs.getStringSet(key, defValue);
    }

    public boolean contains(String key) {
        SharedPreferences prefs = prefs();
        return prefs != null && prefs.contains(key);
    }

    public boolean hasFileChanged() {
        return false;
    }
}
