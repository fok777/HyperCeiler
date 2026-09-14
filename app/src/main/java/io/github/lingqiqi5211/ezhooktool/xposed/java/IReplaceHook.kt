package io.github.lingqiqi5211.ezhooktool.xposed.java

import io.github.lingqiqi5211.ezhooktool.xposed.common.HookParam

/** 替换型回调：返回值直接作为方法结果，原方法不执行。 */
fun interface IReplaceHook {
    fun replace(param: HookParam): Any?
}
