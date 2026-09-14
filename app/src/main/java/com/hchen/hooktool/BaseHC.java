package com.hchen.hooktool;

import android.util.Log;

import com.hchen.hooktool.hook.IHook;
import com.sevtinge.hyperceiler.compat.XC_LoadPackage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Optional;

import io.github.libxposed.api.XposedInterface;
import io.github.lingqiqi5211.ezhooktool.core.BestMatchUtils;
import io.github.lingqiqi5211.ezhooktool.core.java.Fields;
import io.github.lingqiqi5211.ezhooktool.core.java.Methods;
import io.github.lingqiqi5211.ezhooktool.xposed.java.Hooks;

/**
 * HChenX HookTool 的 BaseHC 兼容实现（libxposed API 102）。
 *
 * <p>保留 BaseHC 子类的常见调用形态：hookMethod / callMethod / getField / setField /
 * existsClass 等，内部转发到 EzHookTool。</p>
 */
public abstract class BaseHC {

    public String TAG = getClass().getSimpleName();

    /** 当前目标进程信息；由模块入口填入。 */
    public XC_LoadPackage.LoadPackageParam lpparam;

    /** 当前 hook 回调参数（ThreadLocal）。 */
    protected final IHook mHookParam = new IHook() {
    };

    public ClassLoader mClassLoader;

    /** 兼容旧命名：指向 mClassLoader。 */
    public ClassLoader classLoader;

    private static ClassLoader currentLoader() {
        ClassLoader loader = io.github.lingqiqi5211.ezhooktool.xposed.EzXposed.getSafeClassLoader();
        return loader != null ? loader : BaseHC.class.getClassLoader();
    }

    // ==================== 生命周期 ====================

    /** 子类实现具体 hook。 */
    protected void init() {
    }

    /** 子类可覆写，用于在拿到 ClassLoader 后做准备工作。 */
    public void load(ClassLoader classLoader) {
    }

    /** 由 BaseModule 调度。 */
    public void onLoadPackage() {
        ClassLoader loader = lpparam != null && lpparam.classLoader != null
            ? lpparam.classLoader : currentLoader();
        mClassLoader = loader;
        classLoader = loader;
        load(loader);
        try {
            init();
        } catch (Throwable t) {
            logE(TAG, t);
        }
    }

    public void setLoadPackageParam(XC_LoadPackage.LoadPackageParam param) {
        lpparam = param;
    }

    // ==================== 类查找 ====================

    public Optional<Class<?>> findClass(String className) {
        return Optional.ofNullable(io.github.lingqiqi5211.ezhooktool.core.ClassUtils.loadClassOrNull(className, currentLoader()));
    }

    public Optional<Class<?>> findClass(String className, ClassLoader classLoader) {
        return Optional.ofNullable(io.github.lingqiqi5211.ezhooktool.core.ClassUtils.loadClassOrNull(className, classLoader));
    }

    public Class<?> loadClass(String className) {
        return io.github.lingqiqi5211.ezhooktool.core.ClassUtils.loadClass(className, currentLoader());
    }

    public boolean existsClass(String className) {
        return io.github.lingqiqi5211.ezhooktool.core.ClassUtils.loadClassOrNull(className, currentLoader()) != null;
    }

    public boolean existsClass(String className, ClassLoader classLoader) {
        return io.github.lingqiqi5211.ezhooktool.core.ClassUtils.loadClassOrNull(className, classLoader) != null;
    }

    // ==================== 反射调用 ====================

    public Object callMethod(Object obj, String methodName, Object... args) {
        return Methods.callMethod(require(obj), methodName, args);
    }

    public Object callStaticMethod(Class<?> clazz, String methodName, Object... args) {
        return Methods.callStaticMethod(clazz, methodName, args);
    }

    public Object callStaticMethod(String className, String methodName, Object... args) {
        Class<?> clazz = io.github.lingqiqi5211.ezhooktool.core.ClassUtils.loadClassOrNull(className, currentLoader());
        if (clazz == null) return null;
        return Methods.callStaticMethod(clazz, methodName, args);
    }

    public Object getField(Object obj, String fieldName) {
        return Fields.getObjectField(require(obj), fieldName);
    }

    public Object getStaticField(Class<?> clazz, String fieldName) {
        return Fields.getStaticObjectField(clazz, fieldName);
    }

    public Object getStaticField(String className, ClassLoader classLoader, String fieldName) {
        Class<?> clazz = io.github.lingqiqi5211.ezhooktool.core.ClassUtils.loadClassOrNull(className, classLoader);
        return clazz == null ? null : Fields.getStaticObjectField(clazz, fieldName);
    }

    public void setStaticField(String className, ClassLoader classLoader, String fieldName, Object value) {
        Class<?> clazz = io.github.lingqiqi5211.ezhooktool.core.ClassUtils.loadClassOrNull(className, classLoader);
        if (clazz != null) Fields.setStaticObjectField(clazz, fieldName, value);
    }

    public Object getStaticField(Object obj, String fieldName) {
        Class<?> clazz = (obj instanceof Class<?>) ? (Class<?>) obj : obj.getClass();
        return Fields.getStaticObjectField(clazz, fieldName);
    }

    public void setStaticField(Object obj, String fieldName, Object value) {
        Class<?> clazz = (obj instanceof Class<?>) ? (Class<?>) obj : obj.getClass();
        Fields.setStaticObjectField(clazz, fieldName, value);
    }

    public Object newInstance(String className, Object... args) {
        Class<?> clazz = io.github.lingqiqi5211.ezhooktool.core.ClassUtils.loadClassOrNull(className, currentLoader());
        return clazz == null ? null : newInstance(clazz, args);
    }

    public void setField(Object obj, String fieldName, Object value) {
        Fields.setObjectField(require(obj), fieldName, value);
    }

    public void setStaticField(Class<?> clazz, String fieldName, Object value) {
        Fields.setStaticObjectField(clazz, fieldName, value);
    }

    public Object newInstance(Class<?> clazz, Object... args) {
        return io.github.lingqiqi5211.ezhooktool.core.java.Constructors.newInstance(clazz, args);
    }

    // ==================== Hook ====================

    /**
     * hook 指定方法。
     *
     * @param clazz      目标类
     * @param methodName 方法名
     * @param args       参数类型列表，最后一项必须是 {@link IHook}
     */
    public XposedInterface.HookHandle hook(Method method, IHook hook) {
        return Hooks.createHook(method, asMethodHook(hook));
    }

    public XposedInterface.HookHandle hook(Member member, IHook hook) {
        if (member instanceof Method) return hook((Method) member, hook);
        if (member instanceof Constructor) return Hooks.createHook((Constructor<?>) member, asMethodHook(hook));
        return null;
    }

    public XposedInterface.HookHandle hook(Constructor<?> constructor, IHook hook) {
        return Hooks.createHook(constructor, asMethodHook(hook));
    }

    public XposedInterface.HookHandle hookMethod(Class<?> clazz, String methodName, Object... args) {
        if (args == null || args.length == 0 || !(args[args.length - 1] instanceof IHook hook)) {
            throw new IllegalArgumentException("hookMethod: last argument must be IHook.");
        }
        Class<?>[] parameterTypes = new Class<?>[args.length - 1];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = (Class<?>) args[i];
        }
        Method method = io.github.lingqiqi5211.ezhooktool.core.BestMatchUtils.findMethodBestMatch(clazz, methodName, parameterTypes);
        return Hooks.createHook(method, asMethodHook(hook));
    }

    public XposedInterface.HookHandle hookMethod(String className, ClassLoader classLoader, String methodName, Object... args) {
        if (args == null || args.length == 0 || !(args[args.length - 1] instanceof IHook hook)) {
            throw new IllegalArgumentException("hookMethod: last argument must be IHook.");
        }
        Class<?> clazz = io.github.lingqiqi5211.ezhooktool.core.ClassUtils.loadClassOrNull(className, classLoader);
        if (clazz == null) return null;
        Object[] rest = new Object[args.length - 1];
        System.arraycopy(args, 0, rest, 0, args.length - 1);
        Class<?>[] parameterTypes = toTypes(rest, classLoader);
        Method method = io.github.lingqiqi5211.ezhooktool.core.BestMatchUtils.findMethodBestMatch(clazz, methodName, parameterTypes);
        return Hooks.createHook(method, asMethodHook(hook));
    }

    private Class<?>[] toTypes(Object[] args, ClassLoader classLoader) {
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = (args[i] instanceof Class) ? (Class<?>) args[i]
                : io.github.lingqiqi5211.ezhooktool.core.ClassUtils.loadClassOrNull(args[i].toString(), classLoader);
        }
        return types;
    }

    public XposedInterface.HookHandle hookMethod(String className, String methodName, Object... args) {
        Class<?> clazz = io.github.lingqiqi5211.ezhooktool.core.ClassUtils.loadClassOrNull(className, currentLoader());
        if (clazz == null) {
            logE(TAG, "hookMethod: class not found: " + className);
            return null;
        }
        return hookMethod(clazz, methodName, args);
    }

    public XposedInterface.HookHandle hookAllMethod(Class<?> clazz, String methodName, IHook hook) {
        XposedInterface.HookHandle last = null;
        for (Method method : clazz.getDeclaredMethods()) {
            if (methodName.equals(method.getName())) {
                last = Hooks.createHook(method, asMethodHook(hook));
            }
        }
        return last;
    }

    public XposedInterface.HookHandle hookAllConstructors(Class<?> clazz, IHook hook) {
        XposedInterface.HookHandle last = null;
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            last = Hooks.createHook(constructor, asMethodHook(hook));
        }
        return last;
    }

    /** 把 IHook 转成 EzHookTool 回调，并在回调期间填充 ParamTool 状态。 */
    private io.github.lingqiqi5211.ezhooktool.xposed.java.IMethodHook asMethodHook(IHook hook) {
        return new io.github.lingqiqi5211.ezhooktool.xposed.java.IMethodHook() {
            @Override
            public void before(io.github.lingqiqi5211.ezhooktool.xposed.common.HookParam param) {
                IHook.HookState state = new IHook.HookState();
                state.args = param.getArgs();
                state.thisObject = param.getThisObjectOrNull();
                state.method = param.getExecutable() instanceof Method m ? m : null;
                IHook.STATE.set(state);
                try {
                    hook.before();
                } catch (Throwable t) {
                    logE(TAG, t);
                } finally {
                    applyState(param, state);
                    IHook.STATE.remove();
                }
            }

            @Override
            public void after(io.github.lingqiqi5211.ezhooktool.xposed.common.HookParam param) {
                IHook.HookState state = new IHook.HookState();
                state.args = param.getArgs();
                state.thisObject = param.getThisObjectOrNull();
                state.result = param.getResult();
                state.method = param.getExecutable() instanceof Method m ? m : null;
                IHook.STATE.set(state);
                try {
                    hook.after();
                } catch (Throwable t) {
                    logE(TAG, t);
                } finally {
                    applyState(param, state);
                    IHook.STATE.remove();
                }
            }
        };
    }

    private void applyState(io.github.lingqiqi5211.ezhooktool.xposed.common.HookParam param,
                            IHook.HookState state) {
        if (state.throwable != null) {
            param.setThrowable(state.throwable);
        } else if (state.result != null) {
            param.setResult(state.result);
        }
    }

    // ==================== 链式 DSL ====================

    /** 单个方法声明。 */
    public static class MethodDef {
        public String name;
        public Class<?>[] parameterTypes;
        public boolean anyParams;
        public IHook hook;

        MethodDef(String name, Class<?>[] parameterTypes, boolean anyParams) {
            this.name = name;
            this.parameterTypes = parameterTypes;
            this.anyParams = anyParams;
        }
    }

    /** chain DSL 构造器。 */
    public static class ChainBuilder {
        final java.util.List<MethodDef> defs = new java.util.ArrayList<>();

        public ChainBuilder method(String name, Class<?>... parameterTypes) {
            defs.add(new MethodDef(name, parameterTypes, false));
            return this;
        }

        public ChainBuilder anyMethod(String name) {
            defs.add(new MethodDef(name, null, true));
            return this;
        }

        public ChainBuilder hook(IHook hook) {
            if (!defs.isEmpty()) defs.get(defs.size() - 1).hook = hook;
            return this;
        }
    }

    /** 声明一个具参方法。 */
    public ChainBuilder method(String name, Class<?>... parameterTypes) {
        return new ChainBuilder().method(name, parameterTypes);
    }

    /** 声明一个任意参数的方法。 */
    public ChainBuilder anyMethod(String name) {
        return new ChainBuilder().anyMethod(name);
    }

    /** 按链式声明批量 hook。 */
    public void chain(String className, ClassLoader classLoader, ChainBuilder builder) {
        if (builder == null) return;
        Class<?> clazz = io.github.lingqiqi5211.ezhooktool.core.ClassUtils.loadClassOrNull(className,
            classLoader != null ? classLoader : currentLoader());
        if (clazz == null) {
            logE(TAG, "chain: class not found: " + className);
            return;
        }
        for (MethodDef def : builder.defs) {
            if (def.hook == null) continue;
            try {
                if (def.anyParams) {
                    hookAllMethod(clazz, def.name, def.hook);
                } else {
                    hookMethod(clazz, def.name, appendHook(def.parameterTypes, def.hook));
                }
            } catch (Throwable t) {
                logE(TAG, "chain: failed to hook " + def.name + ": " + t);
            }
        }
    }

    public void chain(Class<?> clazz, ChainBuilder builder) {
        chain(clazz.getName(), clazz.getClassLoader(), builder);
    }

    private static Object[] appendHook(Class<?>[] parameterTypes, IHook hook) {
        int len = parameterTypes == null ? 0 : parameterTypes.length;
        Object[] args = new Object[len + 1];
        for (int i = 0; i < len; i++) args[i] = parameterTypes[i];
        args[len] = hook;
        return args;
    }

    /** 子类可覆写：在 init 中满足条件后调用以启用 hook 分组。 */
    public void startHook() {
    }

    private static Object require(Object obj) {
        if (obj == null) throw new NullPointerException("BaseHC: target object is null.");
        return obj;
    }

    // ==================== 便捷 hook 回调 ====================

    /** 直接返回常量。 */
    public static IHook returnResult(final Object result) {
        return new IHook() {
            @Override
            public void before() {
                setResult(result);
            }
        };
    }

    /** 空操作回调。 */
    public static IHook doNothing() {
        return new IHook() {
        };
    }

    /** 返回 null。 */
    public static IHook returnNull() {
        return returnResult(null);
    }

    /** 抛异常。 */
    public static IHook returnThrowable(final Throwable throwable) {
        return new IHook() {
            @Override
            public void before() {
                setThrowable(throwable);
            }
        };
    }

    // ==================== 日志 ====================

    public void logI(String tag, Object message) {
        Log.i("HyperCeiler", "[I/" + tag + "]: " + message);
    }

    public void logE(String tag, Object message) {
        Log.e("HyperCeiler", "[E/" + tag + "]: " + message);
    }

    public void logD(String tag, Object message) {
        Log.d("HyperCeiler", "[D/" + tag + "]: " + message);
    }
}
