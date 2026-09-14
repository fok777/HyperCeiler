package io.github.lingqiqi5211.ezhooktool.xposed.common

import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Executable
import java.lang.reflect.Member

/**
 * HyperCeiler 内部使用的 hook 调用参数。
 *
 * <p>本项目不再依赖外部 EzHookTool（其 1.1.3 版本以 Java 25 字节码发布，AGP 8.x 的 R8 无法 dex），
 * 这里直接在 libxposed API 102 的 [XposedInterface.Chain] 之上实现等价语义。</p>
 */
open class HookParam internal constructor(
    internal val chain: XposedInterface.Chain
) {

    private var resultValue: Any? = null
    private var throwableValue: Throwable? = null
    private var skipped = false
    private var hasResult = false

    /** 当前被 hook 的成员。 */
    val executable: Executable
        get() = chain.executable

    /** 兼容旧命名的成员访问。 */
    val member: Member
        get() = chain.executable as Member

    /** 当前实例；静态方法时读取会抛异常。 */
    val thisObject: Any
        get() = chain.thisObject

    /** 当前实例；静态方法返回 null。 */
    val thisObjectOrNull: Any?
        get() = runCatching { chain.thisObject }.getOrNull()

    /** 当前调用参数；原地修改下标生效。 */
    val args: Array<Any?>
        get() = chain.args

    /** 当前返回值。before 阶段写入会跳过原方法；after 阶段可读写。 */
    var result: Any?
        get() = resultValue
        set(value) {
            resultValue = value
            hasResult = true
            skipped = true
        }

    /** 当前异常，可读写。 */
    var throwable: Throwable?
        get() = throwableValue
        set(value) {
            throwableValue = value
        }

    val hasThrowable: Boolean
        get() = throwableValue != null

    /** 是否已由 before 阶段短路（跳过原方法）。 */
    val isSkipped: Boolean
        get() = skipped

    fun getExecutable(): Executable = executable

    fun getThisObject(): Any = thisObject

    fun getThisObjectOrNull(): Any? = thisObjectOrNull

    fun getArgs(): Array<Any?> = args

    fun arg(index: Int): Any? = args[index]

    fun <T> argAs(index: Int): T = args[index] as T

    fun getResult(): Any? = resultValue

    fun setResult(value: Any?) {
        result = value
    }

    fun getThrowable(): Throwable? = throwableValue

    fun setThrowable(value: Throwable?) {
        throwable = value
    }

    fun getHasThrowable(): Boolean = hasThrowable

    @Throws(Throwable::class)
    fun getResultOrThrowable(): Any? =
        if (hasThrowable) throw throwableValue!! else resultValue

    internal fun setProceedResult(value: Any?) {
        resultValue = value
        hasResult = true
    }

    internal fun setProceedThrowable(value: Throwable) {
        throwableValue = value
    }

    internal fun hasProceedResult(): Boolean = hasResult
}
