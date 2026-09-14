package android.app;

/**
 * legacy AndroidAppHelper 的 API 102 兼容影子实现。
 *
 * <p>libxposed API 102 不再提供该类，这里用模块缓存的 application 兜底。</p>
 */
public final class AndroidAppHelper {

    private static volatile Application sApplication;
    private static volatile String sProcessName;
    private static volatile String sPackageName;

    private AndroidAppHelper() {
    }

    /** 记录当前进程已就绪的 Application。 */
    public static void onApplicationReady(Application application) {
        if (application == null) return;
        sApplication = application;
        sPackageName = application.getPackageName();
    }

    public static void onPackageLoaded(String packageName, String processName) {
        sPackageName = packageName;
        sProcessName = processName;
    }

    /** 当前进程的 Application；尚未就绪时返回 null。 */
    public static Application currentApplication() {
        Application app = sApplication;
        if (app != null) return app;
        try {
            return (Application) Class.forName("android.app.ActivityThread")
                .getMethod("currentApplication").invoke(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static String currentProcessName() {
        String name = sProcessName;
        if (name != null) return name;
        Application app = currentApplication();
        return app == null ? "" : app.getPackageName();
    }

    public static String currentPackageName() {
        String name = sPackageName;
        if (name != null) return name;
        Application app = currentApplication();
        return app == null ? "" : app.getPackageName();
    }
}
