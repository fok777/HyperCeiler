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
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sevtinge.hyperceiler.module.base.tool.AppsTool;
import com.sevtinge.hyperceiler.utils.prefs.PrefsUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.XposedServiceHelper;

/**
 * 模块自身进程的 Application。
 *
 * <p>libxposed API 102 不再把模块注入自身进程，因此无法通过自 hook 写
 * {@link AppsTool#isModuleActive} 标记。这里改为绑定框架的 {@link XposedService}，
 * 由 {@link #onServiceBind} / {@link #onServiceDied} 维护激活状态。</p>
 *
 * <p>服务绑定是异步的，主界面可能在回调到达之前就读取激活状态。因此额外维护一个
 * 宽限期：宽限期内视为“尚未判定”，避免误报未激活。</p>
 */
public class Application extends android.app.Application
    implements XposedServiceHelper.OnServiceListener {

    private static final String TAG = "HyperCeiler";

    /** 服务绑定宽限期；超过后仍无回调才判定为未激活。 */
    private static final long ACTIVATION_GRACE_MS = 3000L;

    @Nullable
    private static volatile XposedService sService;

    /** 注册失败（框架不支持 libxposed service），无需再等待。 */
    private static volatile boolean sRegisterFailed;

    private static final long sStartAt = SystemClock.elapsedRealtime();

    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void attachBaseContext(Context base) {
        PrefsUtils.mSharedPreferences = PrefsUtils.getSharedPrefs(base);
        super.attachBaseContext(base);
        setupCrashHandler();
        try {
            XposedServiceHelper.registerListener(this);
            Log.i(TAG, "XposedServiceHelper.registerListener done.");
        } catch (Throwable t) {
            sRegisterFailed = true;
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

    /** 服务是否已绑定（等价于模块已激活）。 */
    public static boolean isXposedServiceBound() {
        return sService != null;
    }

    /**
     * 激活状态是否已可判定。
     *
     * <p>服务已绑定、注册失败、或已超过宽限期，三者之一成立即视为可判定。</p>
     */
    public static boolean isActivationSettled() {
        if (sService != null || sRegisterFailed) return true;
        return SystemClock.elapsedRealtime() - sStartAt >= ACTIVATION_GRACE_MS;
    }

    /**
     * 在激活状态可判定后执行回调，避免异步绑定导致的误报。
     *
     * @param runnable 判定完成后在主线程执行
     */
    public static void whenActivationSettled(@NonNull Runnable runnable) {
        if (isActivationSettled()) {
            runnable.run();
            return;
        }
        long delay = ACTIVATION_GRACE_MS - (SystemClock.elapsedRealtime() - sStartAt);
        sMainHandler.postDelayed(runnable, Math.max(delay, 0L));
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

    /** 把未捕获异常写入外部存储，便于无 adb 时定位启动崩溃。 */
    private void setupCrashHandler() {
        final Thread.UncaughtExceptionHandler defaultHandler =
            Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            try {
                File dir = new File(getExternalFilesDir(null), "crash");
                if (dir.mkdirs() || dir.isDirectory()) {
                    String name = "crash_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                        .format(new Date()) + ".txt";
                    try (PrintWriter pw = new PrintWriter(new FileWriter(new File(dir, name)))) {
                        pw.println("Thread: " + thread.getName());
                        pw.println("Time: " + new Date());
                        pw.println("ServiceBound: " + isXposedServiceBound());
                        pw.println("RegisterFailed: " + sRegisterFailed);
                        ex.printStackTrace(pw);
                    }
                }
            } catch (Throwable ignored) {
            }
            Log.e(TAG, "Uncaught exception on " + thread.getName(), ex);
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, ex);
            }
        });
    }
}
