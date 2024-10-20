package me.neon.libs.carrier

/**
 * NeonLibs
 * me.neon.libs.carrier
 *
 * @author 老廖
 * @since 2024/5/3 10:50
 */
interface MetaDataValue<T> {

    var value: T

    var timer: Long

    fun isTimerOut(): Boolean

    fun asBoolean(def: Boolean = false): Boolean

    fun asInt(): Int

    fun asDouble(): Double



}