package com.sevtinge.hyperceiler.compat;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;

/**
 * legacy XSharedPreferences 的 API 102 兼容实现。
 *
 * <p>libxposed API 102 下模块不再注入自身进程，hook 侧拿不到模块 Context，
 * 不能用 [Context.getSharedPreferences] 读取模块设置（那会读到宿主应用的同名 sp）。
 * 这里改为直接解析模块私有目录下的 sp XML 文件，与 legacy 行为一致。</p>
 *
 * <p>优先尝试 libxposed 的 RemotePreferences；不可用时按候选路径读文件。</p>
 */
public class XSharedPreferences {

    private static final String TAG = "HyperCeiler";

    private final String mGroupName;
    private final File[] mCandidates;
    @Nullable
    private SharedPreferences mRemote;
    private volatile Map<String, Object> mFileCache;

    public XSharedPreferences(String packageName, String prefsName) {
        mGroupName = prefsName;
        mCandidates = candidatesFor(packageName, prefsName);
        reload();
    }

    public XSharedPreferences(File prefFile) {
        mGroupName = prefFile != null ? prefFile.getName().replace(".xml", "") : null;
        mCandidates = prefFile != null ? new File[]{prefFile} : new File[0];
        reload();
    }

    private static File[] candidatesFor(String packageName, String prefsName) {
        String name = prefsName.endsWith(".xml") ? prefsName : prefsName + ".xml";
        return new File[]{
            new File("/data/user_de/0/" + packageName + "/shared_prefs/" + name),
            new File("/data/data/" + packageName + "/shared_prefs/" + name),
            new File("/data/user/0/" + packageName + "/shared_prefs/" + name),
        };
    }

    public void reload() {
        mFileCache = null;
        mRemote = null;
        if (mGroupName != null) {
            try {
                SharedPreferences remote = HookRuntime.remotePreferences(mGroupName);
                if (remote != null && !remote.getAll().isEmpty()) {
                    mRemote = remote;
                    return;
                }
            } catch (Throwable t) {
                Log.w(TAG, "getRemotePreferences failed: " + t);
            }
        }
    }

    public void makeWorldReadable() {
        // 放宽自身 sp 文件权限，便于 hook 侧（其他进程）读取。
        for (File f : mCandidates) {
            if (f == null) continue;
            try {
                if (f.exists()) {
                    f.setReadable(true, false);
                    File dir = f.getParentFile();
                    if (dir != null) {
                        dir.setExecutable(true, false);
                        dir.setReadable(true, false);
                    }
                }
            } catch (Throwable ignored) {
            }
        }
    }

    private Map<String, Object> filePrefs() {
        if (mFileCache != null) return mFileCache;
        Map<String, Object> result = new HashMap<>();
        for (File f : mCandidates) {
            if (f == null || !f.exists() || !f.canRead()) continue;
            try (FileInputStream fis = new FileInputStream(f)) {
                parseXml(fis, result);
                if (!result.isEmpty()) {
                    Log.i(TAG, "XSharedPreferences: read " + result.size()
                        + " prefs from " + f.getAbsolutePath());
                    break;
                }
            } catch (Throwable t) {
                Log.w(TAG, "XSharedPreferences: parse failed " + f.getAbsolutePath() + ": " + t);
            }
        }
        mFileCache = result;
        return result;
    }

    private static void parseXml(FileInputStream fis, Map<String, Object> out) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(fis, "UTF-8");
        int event = parser.getEventType();
        String currentTag = null;
        String currentName = null;
        Set<String> currentSet = null;
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG: {
                    String tag = parser.getName();
                    if ("map".equals(tag)) break;
                    if ("set".equals(tag)) {
                        currentTag = tag;
                        currentName = parser.getAttributeValue(null, "name");
                        currentSet = new HashSet<>();
                        break;
                    }
                    String name = parser.getAttributeValue(null, "name");
                    if (name == null) break;
                    if (currentSet != null) {
                        parser.next();
                        if (parser.getEventType() == XmlPullParser.TEXT) {
                            String v = parser.getText();
                            if (v != null) currentSet.add(v);
                        }
                        break;
                    }
                    String value = parser.getAttributeValue(null, "value");
                    switch (tag) {
                        case "boolean":
                            out.put(name, Boolean.valueOf(value));
                            break;
                        case "int":
                            out.put(name, safeInt(value));
                            break;
                        case "long":
                            out.put(name, safeLong(value));
                            break;
                        case "float":
                            out.put(name, safeFloat(value));
                            break;
                        case "string":
                            parser.next();
                            out.put(name,
                                parser.getEventType() == XmlPullParser.TEXT ? parser.getText() : "");
                            break;
                        default:
                            break;
                    }
                    break;
                }
                case XmlPullParser.END_TAG: {
                    if ("set".equals(parser.getName()) && currentSet != null && currentName != null) {
                        out.put(currentName, currentSet);
                        currentSet = null;
                        currentName = null;
                    }
                    break;
                }
                default:
                    break;
            }
            event = parser.next();
        }
    }

    private static Integer safeInt(String v) {
        try {
            return Integer.valueOf(v);
        } catch (Throwable t) {
            return 0;
        }
    }

    private static Long safeLong(String v) {
        try {
            return Long.valueOf(v);
        } catch (Throwable t) {
            return 0L;
        }
    }

    private static Float safeFloat(String v) {
        try {
            return Float.valueOf(v);
        } catch (Throwable t) {
            return 0f;
        }
    }

    /** 合并远程与文件两路数据；远程优先。 */
    public Map<String, ?> getAll() {
        Map<String, Object> result = new HashMap<>(filePrefs());
        if (mRemote != null) {
            try {
                result.putAll(mRemote.getAll());
            } catch (Throwable ignored) {
            }
        }
        return result;
    }

    private Object get(String key) {
        if (mRemote != null) {
            try {
                Map<String, ?> all = mRemote.getAll();
                if (all.containsKey(key)) return all.get(key);
            } catch (Throwable ignored) {
            }
        }
        return filePrefs().get(key);
    }

    public boolean getBoolean(String key, boolean defValue) {
        Object v = get(key);
        return v instanceof Boolean ? (Boolean) v : defValue;
    }

    public int getInt(String key, int defValue) {
        Object v = get(key);
        return v instanceof Integer ? (Integer) v : defValue;
    }

    public long getLong(String key, long defValue) {
        Object v = get(key);
        return v instanceof Long ? (Long) v : defValue;
    }

    public float getFloat(String key, float defValue) {
        Object v = get(key);
        return v instanceof Float ? (Float) v : defValue;
    }

    public String getString(String key, String defValue) {
        Object v = get(key);
        return v instanceof String ? (String) v : defValue;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getStringSet(String key, Set<String> defValue) {
        Object v = get(key);
        return v instanceof Set ? (Set<String>) v : defValue;
    }

    public boolean contains(String key) {
        if (mRemote != null) {
            try {
                if (mRemote.contains(key)) return true;
            } catch (Throwable ignored) {
            }
        }
        return filePrefs().containsKey(key);
    }

    public boolean hasFileChanged() {
        return false;
    }
}
