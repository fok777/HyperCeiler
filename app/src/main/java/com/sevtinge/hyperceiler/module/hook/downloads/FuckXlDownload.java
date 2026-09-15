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
package com.sevtinge.hyperceiler.module.hook.downloads;

import android.os.Environment;

import com.sevtinge.hyperceiler.module.base.BaseHook;

import java.io.File;
import java.io.FileNotFoundException;

import com.sevtinge.hyperceiler.compat.XC_MethodHook;
import com.sevtinge.hyperceiler.compat.XC_MethodHook.MethodHookParam;
import com.sevtinge.hyperceiler.compat.XposedHelpers;

public class FuckXlDownload extends BaseHook {
    private static final String TARGET_PACKAGE = "com.android.providers.downloads";
    private static final File TARGET_PATH = new File(Environment.getExternalStorageDirectory(), ".xlDownload").getAbsoluteFile();

    @Override
    public void init() {
        logI(TAG, lpparam.packageName, "xlDownload guard installed, target = " + TARGET_PATH);
        // 同时 hook mkdir 与 mkdirs：两者是彼此独立的 native 调用，
        // 只拦 mkdirs 会漏掉调用 File.mkdir() 的创建方。
        hookCreate("mkdirs");
        hookCreate("mkdir");
    }

    private void hookCreate(String methodName) {
        try {
            XposedHelpers.findAndHookMethod(File.class, methodName, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    Object self = param.thisObject;
                    if (!(self instanceof File)) return;
                    File file = (File) self;
                    if (!file.getAbsoluteFile().equals(TARGET_PATH)) return;
                    String pkg = FuckXlDownload.this.lpparam.packageName;
                    logI(TAG, pkg, "blocked " + methodName + " -> " + file.getAbsolutePath());
                    param.setThrowable(new FileNotFoundException("blocked"));
                }
            });
        } catch (Throwable t) {
            logE(TAG, lpparam.packageName, "hook " + methodName + " failed", t);
        }
    }
}
