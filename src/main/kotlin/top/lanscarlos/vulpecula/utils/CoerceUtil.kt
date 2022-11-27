package top.lanscarlos.vulpecula.utils

import taboolib.common5.Coerce

/**
 * Vulpecular
 * top.lanscarlos.vulpecular.utils
 *
 * @author Lanscarlos
 * @since 2022-02-27 16:45
 */

fun Any?.coerceBoolean(def: Boolean? = null): Boolean {
    if (this == null && def != null) return def
    return Coerce.toBoolean(this)
}

fun Any?.coerceShort(def: Short? = null): Short {
    if (this == null && def != null) return def
    return Coerce.toShort(this)
}

fun Any?.coerceInt(def: Int? = null): Int {
    if (this == null && def != null) return def
    return Coerce.toInteger(this)
}

fun Any?.coerceLong(def: Long? = null): Long {
    if (this == null && def != null) return def
    return Coerce.toLong(this)
}

fun Any?.coerceFloat(def: Float? = null): Float {
    if (this == null && def != null) return def
    return Coerce.toFloat(this)
}

fun Any?.coerceDouble(def: Double? = null): Double {
    if (this == null && def != null) return def
    return Coerce.toDouble(this)
}