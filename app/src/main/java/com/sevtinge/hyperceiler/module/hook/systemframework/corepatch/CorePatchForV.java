package com.sevtinge.hyperceiler.module.hook.systemframework.corepatch;

import com.sevtinge.hyperceiler.compat.XposedHelpers;

public class CorePatchForV extends CorePatchForU {
    @Override
    Class<?> getParsedPackage(ClassLoader classLoader) {
        return XposedHelpers.findClassIfExists("com.android.internal.pm.parsing.pkg.ParsedPackage", classLoader);
    }
}
