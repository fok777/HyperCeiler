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

import com.sevtinge.hyperceiler.module.base.BaseHook;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.sevtinge.hyperceiler.compat.XC_MethodHook;
import com.sevtinge.hyperceiler.compat.XC_MethodHook.MethodHookParam;
import com.sevtinge.hyperceiler.compat.XC_MethodReplacement;
import com.sevtinge.hyperceiler.compat.XposedHelpers;

/**
 * 阻止下载管理创建 .xlDownload 目录。
 *
 * <p>该目录由下载管理内置的迅雷加速 SDK 创建，并不是系统下载流程本身需要的目录。
 * 与 main 分支（RemoveXlDownload）保持一致的处理方式：
 * 一是让迅雷 SDK 的调试配置方法直接返回 null，二是拦截 FileUtil.createFile 中
 * 路径包含 ".xlDownload" 的调用。</p>
 *
 * <p>注意：hook 的是下载管理自己的 FileUtil.createFile，而不是 java.io.File.mkdirs。
 * 后者会误伤所有进程，且拦不到通过其他方式创建的目录。</p>
 *
 * @see <a href="https://github.com/YifePlayte/WOMMO">WOMMO</a>
 */
public class FuckXlDownload extends BaseHook {
    private static final String TAG_XL = "FuckXlDownload";

    private static final String CLASS_XL_CONFIG =
        "com.android.providers.downloads.config.XLConfig";
    private static final String CLASS_FILE_UTIL =
        "com.android.providers.downloads.util.FileUtil";

    @Override
    public void init() {
        disableXlConfig();
        blockCreateFile();
    }

    /** 让迅雷加速 SDK 的调试配置方法直接返回 null。 */
    private void disableXlConfig() {
        try {
            Class<?> clazz = XposedHelpers.findClass(CLASS_XL_CONFIG, lpparam.classLoader);
            for (Method method : clazz.getDeclaredMethods()) {
                String name = method.getName();
                if (!("setDebug".equals(name) || "setSoDebug".equals(name))) continue;
                if (Modifier.isAbstract(method.getModifiers())) continue;
                method.setAccessible(true);
                XposedHelpers.hookMethod(method, XC_MethodReplacement.returnConstant(null));
                logI(TAG_XL, lpparam.packageName, "disabled " + name);
            }
        } catch (Throwable t) {
            logE(TAG_XL, lpparam.packageName, "XLConfig not found, skip", t);
        }
    }

    /** 拦截路径包含 .xlDownload 的文件创建。 */
    private void blockCreateFile() {
        try {
            Class<?> clazz = XposedHelpers.findClass(CLASS_FILE_UTIL, lpparam.classLoader);
            Method method = XposedHelpers.findMethodExactIfExists(clazz, "createFile", (Object[]) new Class<?>[]{String.class});
            if (method == null) {
                // 不同版本签名可能带额外参数，退化为按名字查找第一个 createFile。
                for (Method candidate : clazz.getDeclaredMethods()) {
                    if ("createFile".equals(candidate.getName()) && candidate.getParameterCount() > 0) {
                        method = candidate;
                        break;
                    }
                }
            }
            if (method == null) {
                logE(TAG_XL, lpparam.packageName, "FileUtil.createFile not found", new Throwable());
                return;
            }
            method.setAccessible(true);
            XposedHelpers.hookMethod(method, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    Object arg = param.args != null && param.args.length > 0 ? param.args[0] : null;
                    if (!(arg instanceof String)) return;
                    String path = (String) arg;
                    if (!path.contains(".xlDownload")) return;
                    logI(TAG_XL, lpparam.packageName, "blocked createFile -> " + path);
                    param.setThrowable(new IOException(".xlDownload is blocked"));
                }
            });
            logI(TAG_XL, lpparam.packageName, "createFile hooked");
        } catch (Throwable t) {
            logE(TAG_XL, lpparam.packageName, "blockCreateFile failed", t);
        }
    }
}
