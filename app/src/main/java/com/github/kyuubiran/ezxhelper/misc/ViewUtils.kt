@file:Suppress("unused")

package com.github.kyuubiran.ezxhelper.misc

import android.content.res.Resources
import android.view.View

/**
 * EzXHelper ViewUtils 的 API 102 兼容实现。
 */
object ViewUtils {

    /** 按资源名在当前上下文查找 id。 */
    @JvmStatic
    fun getIdByName(name: String): Int {
        val context = com.github.kyuubiran.ezxhelper.EzXHelper.appContext ?: return 0
        return getResId(context.resources, name, "id", context.packageName)
    }

    @JvmStatic
    fun getResId(resources: Resources, name: String, defType: String, packageName: String): Int =
        resources.getIdentifier(name, defType, packageName)
}

/** 按 view 内 id 名查找子 View。 */
fun View.findViewByIdName(name: String): View? {
    val context = context ?: return null
    val id = context.resources.getIdentifier(name, "id", context.packageName)
    return if (id == 0) null else findViewById(id)
}

/** 按 id 名在当前 View 树中查找。 */
fun View.findViewByIdNameOrNull(name: String): View? = findViewByIdName(name)
