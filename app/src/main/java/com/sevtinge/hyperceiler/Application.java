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

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sevtinge.hyperceiler.module.base.tool.AppsTool;
import com.sevtinge.hyperceiler.utils.prefs.PrefsUtils;

import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.XposedServiceHelper;

/**
 * 模块自身进程的 Application。
 *
 * <p>libxposed API 102 不再把模块注入自身进程，因此无法通过自 hook 写
 * {@link AppsTool#isModuleActive} 标记。这里改为绑定框架的 {@link XposedService}，
 * 由 {@link #onServiceBind} / {@link #onServiceDied} 维护激活状态。</p>
 */
public class Application extends android.app.Application
    implements XposedServiceHelper.OnServiceListener {

    private static final String TAG = "HyperCeiler";

    @Nullable
    private static volatile XposedService sService;

    @Override
    protected void attachBaseContext(Context base) {
        PrefsUtils.mSharedPreferences = PrefsUtils.getSharedPrefs(base);
        super.attachBaseContext(base);
        try {
            XposedServiceHelper.registerListener(this);
        } catch (Throwable t) {
            // 框架不支持 service（如 LSPosed 1.x）时保持未激活状态，不影响应用启动。
            Log.w(TAG, "register XposedService listener failed: " + t);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    public static XposedService getXposedService() {
        return sService;
    }

    public static boolean isXposedServiceBound() {
        return sService != null;
    }

    @Override
    public void onServiceBind(@NonNull XposedService service) {
        sService = service;
        setModuleActivated(true);
        Log.i(TAG, "XposedService connected, module activated.");
    }

    @Override
    public void onServiceDied(@NonNull XposedService service) {
        if (sService == service) sService = null;
        setModuleActivated(false);
        Log.e(TAG, "XposedService died, module deactivated.");
    }

    private static void setModuleActivated(boolean activated) {
        AppsTool.isModuleActive = activated;
        if (activated) AppsTool.XposedVersion = 102;
    }
}
