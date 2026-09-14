package com.sevtinge.hyperceiler.compat;

/**
 * legacy XC_LayoutInflated 的 API 102 兼容影子。
 *
 * <p>libxposed API 102 未提供布局 hook 能力，这里只保留类型以便旧代码编译；
 * 实际布局替换请改用 ResourcesTool 的资源替换。</p>
 */
public abstract class XC_LayoutInflated {

    public abstract void onLayoutInflated(Object layoutInfo) throws Throwable;
}
