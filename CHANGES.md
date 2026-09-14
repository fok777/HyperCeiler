# a13_eol → libxposed API 102 适配改动说明

目标：把 `a13_eol` 分支从 legacy Xposed API 82（`de.robv.android.xposed.*` + `assets/xposed_init`）
迁移到 libxposed API 102，对齐 `main` 分支的模块声明方式，同时尽量不改动 600+ 个业务 hook 文件。

## 一、核心策略：影子兼容层（Shadow Compat）

a13_eol 有 310 个文件直接用 legacy API、460+ 处用 EzXHelper 2.x DSL。
逐文件重写不现实，因此改为**在同包同名的位置实现兼容层**，业务代码零改动：

| 原依赖 | 处理方式 |
|---|---|
| `de.robv.android.xposed.*` | import 批量改写为 `com.sevtinge.hyperceiler.compat.*`（同名同类） |
| `com.github.kyuubiran.ezxhelper.*` | 在 app 源码树内实现同名兼容包（Kotlin），内部转发 EzHookTool |
| `com.hchen.hooktool.*`（HChenX） | 依赖移除，改为 app 源码树内同名兼容实现 |
| `com.hchen.database.HookBase` | **不动**：它由 `app/processor` 模块提供 |

这样 `XposedHelpers.findAndHookMethod(...)`、`param.setResult(...)`、
`loadClass(...).methodFinder().filterByName(...).single().createHook { returnConstant(x) }`
等写法全部保持原样，只是底层换成了 EzHookTool。

## 二、新增文件

### 1. 模块入口
- `app/src/main/java/com/sevtinge/hyperceiler/XposedInitEntry.java`
  继承 `io.github.libxposed.api.XposedModule`，替代 `assets/xposed_init`。
  - `onModuleLoaded`：原 `initZygote` 逻辑（ResourcesTool / XSPrefs / CorePatch zygote 部分）
  - `onSystemServerStarting` / `onPackageLoaded` / `onPackageReady`：原 `handleLoadPackage` 分发
  - `onHotReloading` / `onHotReloaded`：转发 `EzXposed`
- `app/src/main/resources/META-INF/xposed/`
  - `module.prop`：`minApiVersion=101 / targetApiVersion=102 / autoHotReload=false / staticScope=false`
  - `java_init.list`：`com.sevtinge.hyperceiler.XposedInitEntry`
  - `scope.list`：取自 main 分支（71 个包名）

### 2. legacy API 影子层 `com/sevtinge/hyperceiler/compat/`
`XposedHelpers` `XposedBridge` `XC_MethodHook` `XC_MethodReplacement` `XC_LoadPackage`
`XSharedPreferences` `XCallback` `XC_LayoutInflated` `StartupParam`
`IXposedHookZygoteInit` `IXposedHookLoadPackage` `HookRuntime` `HookBridgeCompat`
`MethodsCompat` `EzXHelper`

要点：
- `XC_MethodHook.MethodHookParam` 的 `thisObject / args` 与底层 `HookParam` 共享引用，原地改参有效
- `setResult()` 在 before 阶段会短路原方法（对应 libxposed 的 skipped 语义）
- `XSharedPreferences` 走 `XposedInterface.getRemotePreferences(group)`，不再依赖 world-readable 文件

### 3. EzXHelper 2.x 兼容包 `com/github/kyuubiran/ezxhelper/`
`HookFactory`（`Method.createHook {}` DSL）`ClassUtils` `ObjectUtils/ObjectHelper`
`finders/MethodFinder|ConstructorFinder|FieldFinder` `MemberExtensions` `Log` `misc/ViewUtils`
`interfaces/IMethodHookCallback` `EzXHelper`

### 4. HChenX HookTool 兼容包 `com/hchen/hooktool/`
`BaseHC`（含 `hookMethod` / `hookAllMethod` / `chain` 链式 DSL / 反射工具）
`HCInit` `hook/IHook` `tool/ParamTool` `tool/CoreTool`
`tool/additional/SystemPropTool` `log/XposedLog`

## 三、修改的文件

- 约 300 个 `.java` / `.kt`：legacy import 改写为 compat 包
- 约 66 个 `.java`：`param.result` 字段读写改为 `getResult()` / `setResult()`
  （libxposed 102 没有可写字段；Kotlin 侧因兼容层提供 `var result` 属性，无需改动）
- `XposedInit.java`：去掉 `implements IXposedHookZygoteInit/IXposedHookLoadPackage`，
  改为被 `XposedInitEntry` 调用的普通类；`moduleActiveHook` 补 null 保护
- `app/build.gradle.kts`
  - 依赖：`xposed api 82` → `io.github.libxposed:api:102.0.0`（compileOnly）
    新增 `io.github.lingqiqi5211.ezhooktool:core:1.1.3` 与 `hook-xposed-102:1.1.3`
    移除 `EzXHelper`、`HChenX HookTool`
  - `packaging.resources.excludes` 去掉 `/META-INF/**`，否则模块声明会被剔除
- `gradle/libs.versions.toml`：对应坐标与版本
- `app/proguard-rules.pro`：加入 libxposed keep 与
  `-adaptresourcefilecontents META-INF/xposed/java_init.list`（配合 lsparanoid 混淆）
- `app/src/main/AndroidManifest.xml`：移除 `xposedmodule` / `xposedminversion` /
  `xposeddescription` / `xposedsharedprefs` / `xposedscope` 五项 meta-data

## 四、删除

- `app/src/main/assets/xposed_init`

## 五、使用前必读

1. **框架要求**：需要实现 libxposed API ≥101 的 LSPosed（2.1.0+ / 2.2.0-7854 等），
   旧版 LSPosed 1.x 无法加载本模块。
2. **模块不再注入自身进程**：API 102 下 `moduleActiveHook` 的自 Hook 失效，
   激活状态检测需要改用 `io.github.libxposed:service`（main 分支的做法）。
   当前实现只做 null 保护，不会崩溃，但界面上的"已激活"标记可能不更新。
3. **热重载**：`autoHotReload=false`（保守值）。EzHookTool 的自动热重载要求 hook
   全部在 `EzXposed.onTargetReady` 同步回调内注册；a13 的 hook 是按包分发的，
   开启后行为不确定，建议验证通过后再打开。
4. **未做编译验证**：本次改动在沙盒内完成，无法执行 Gradle 构建。
   请本地 `./gradlew :app:assembleDebug` 验证，预期会有一批签名/重载相关的
   编译错误需要按报错误微调。
5. `param.args = newArray`（整体替换参数数组）在 API 102 下无效，
   必须原地改下标；改动前已存在于代码中的此类写法需手工检查。
