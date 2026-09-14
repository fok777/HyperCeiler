-keep class com.sevtinge.hyperceiler.XposedInit
-keep class com.sevtinge.hyperceiler.module.skip.SystemFrameworkForCorePatch
-keep class com.sevtinge.hyperceiler.ui.activity.LauncherActivity
-keep class com.sevtinge.hyperceiler.utils.blur.*
-keep class com.sevtinge.hyperceiler.module.base.tool.AppsTool { boolean isModuleActive; }
-keep class com.sevtinge.hyperceiler.module.base.tool.AppsTool { int XposedVersion; }
-keep class fan.**{ *; }
-keep class androidx.preference.**{ *; }
-keep class org.luckypray.dexkit.**{ *; }
-keep class cn.lyric.getter.api.**{ *; }
-keep class com.sevtinge.hyperceiler.module.base.**{ *; }
-keep class com.sevtinge.hyperceiler.holiday.**{ *; }
-keep class * extends com.sevtinge.hyperceiler.ui.fragment.base.*
-keep class * extends com.sevtinge.hyperceiler.module.base.BaseHook { <init>(); }
-keep class com.sevtinge.hyperceiler.module.base.dexkit.**{ *; }
-keep class * extends com.sevtinge.hyperceiler.module.base.BaseModule
-keep class com.sevtinge.hyperceiler.module.base.BaseModule { *; }
-keep class com.sevtinge.hyperceiler.utils.api.miuiStringToast.res.** { *; }
-keep class com.sevtinge.hyperceiler.utils.ContentModel {*;}
-keep class com.sevtinge.hyperceiler.utils.FileHelper {*;}

-keepattributes SourceFile,LineNumberTable
-dontwarn android.app.ActivityTaskManager$RootTaskInfo
-dontwarn miui.app.MiuiFreeFormManager$MiuiFreeFormStackInfo
-dontwarn com.android.internal.view.menu.MenuBuilder
-dontwarn javax.annotation.processing.AbstractProcessor
-dontwarn javax.annotation.processing.SupportedAnnotationTypes
-dontwarn javax.annotation.processing.SupportedOptions
-dontwarn javax.annotation.processing.SupportedSourceVersion
-dontwarn javax.annotation.processing.Processor
-dontwarn miui.util.HapticFeedbackUtil
-allowaccessmodification
-overloadaggressively

# ---------- libxposed API 102 ----------
-dontwarn io.github.libxposed.**
-keep class io.github.libxposed.api.** { *; }
-keep class io.github.libxposed.service.** { *; }

# 模块入口类不能被移除或重命名，同时重写 java_init.list 以匹配混淆后的类名
-keep,allowoptimization,allowobfuscation public class * extends io.github.libxposed.api.XposedModule { public <init>(); }
-adaptresourcefilecontents META-INF/xposed/java_init.list

# EzHookTool 反射入口
-keep class io.github.lingqiqi5211.ezhooktool.** { *; }
-dontwarn io.github.lingqiqi5211.ezhooktool.**
