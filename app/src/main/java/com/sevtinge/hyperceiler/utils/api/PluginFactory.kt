package com.sevtinge.hyperceiler.utils.api

import android.content.*
import com.sevtinge.hyperceiler.compat.XposedHelpers;
import com.sevtinge.hyperceiler.compat.XposedBridge;
import com.sevtinge.hyperceiler.compat.XC_MethodHook;
import com.sevtinge.hyperceiler.compat.XC_LoadPackage;
import java.lang.ref.*


// https://github.com/buffcow/Hyper5GSwitch/blob/master/app/src/main/kotlin/cn/buffcow/hyper5g/hooker/PluginLoader.kt
internal class PluginFactory(obj: Any) {
    lateinit var pluginCtxRef: WeakReference<Context>
    val mComponentName: Any? = XposedHelpers.getObjectField(obj , "mComponentName")

    fun componentNames(str: String): ComponentName {
        return ComponentName("miui.systemui.plugin", str)
    }
}