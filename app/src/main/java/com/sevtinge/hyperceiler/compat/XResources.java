package com.sevtinge.hyperceiler.compat;

import android.content.res.Resources;

/**
 * legacy XResources 的 API 102 兼容占位。
 *
 * <p>libxposed API 102 不再提供资源 hook（layout inflate hook）能力，因此这里只保留
 * 类型与基本转发，[hookLayout] 相关调用在当前分支已无实际使用点。</p>
 */
public class XResources extends Resources {

    public XResources(Resources res) {
        super(res.getAssets(), res.getDisplayMetrics(), res.getConfiguration());
    }
}
