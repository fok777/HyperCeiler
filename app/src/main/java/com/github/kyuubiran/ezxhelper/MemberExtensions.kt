@file:Suppress("unused")

package com.github.kyuubiran.ezxhelper

import java.lang.reflect.Member
import java.lang.reflect.Modifier

/**
 * EzXHelper MemberExtensions 的 API 102 兼容实现。
 */

/** 成员参数个数。 */
val Member.paramCount: Int
    get() = when (this) {
        is java.lang.reflect.Method -> parameterCount
        is java.lang.reflect.Constructor<*> -> parameterCount
        else -> 0
    }

/** 是否静态成员。 */
val Member.isStatic: Boolean
    get() = Modifier.isStatic(modifiers)

/** 是否 final 成员。 */
val Member.isFinal: Boolean
    get() = Modifier.isFinal(modifiers)

/** 是否抽象成员。 */
val Member.isAbstract: Boolean
    get() = Modifier.isAbstract(modifiers)

/** 是否 public 成员。 */
val Member.isPublic: Boolean
    get() = Modifier.isPublic(modifiers)

/** 是否 private 成员。 */
val Member.isPrivate: Boolean
    get() = Modifier.isPrivate(modifiers)
