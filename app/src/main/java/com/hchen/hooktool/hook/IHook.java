package com.hchen.hooktool.hook;

import com.hchen.hooktool.tool.ParamTool;

/**
 * HChenX HookTool 的 IHook 兼容实现（API 102）。
 */
public interface IHook extends ParamTool {

    default void before() {
    }

    default void after() {
    }
}
