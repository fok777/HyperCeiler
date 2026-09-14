package com.sevtinge.hyperceiler.compat;

import android.view.View;

/**
 * legacy XC_LayoutInflated 的 API 102 兼容影子。
 *
 * <p>libxposed API 102 未提供布局 hook 能力，这里只保留类型以便旧代码编译。</p>
 */
public abstract class XC_LayoutInflated {

    /** legacy 布局 inflate 参数。 */
    public static class LayoutInflatedParam {
        public View view;
        public Object resNames;
        public String resDir;
        public String variant;

        public LayoutInflatedParam() {
        }
    }

    public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
        onLayoutInflated(liparam.view);
    }

    public abstract void onLayoutInflated(Object layoutInfo) throws Throwable;
}
