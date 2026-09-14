package io.github.lingqiqi5211.ezhooktool.xposed.java

import io.github.lingqiqi5211.ezhooktool.xposed.common.HookParam

/** before / after 两段式回调。 */
interface IMethodHook {
    fun before(param: HookParam) {}
    fun after(param: HookParam) {}
}
