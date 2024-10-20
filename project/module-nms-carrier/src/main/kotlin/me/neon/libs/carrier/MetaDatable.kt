package me.neon.libs.carrier

/**
 * NeonDesire
 * me.neon.desire.nms.carrier
 *
 * @author 老廖
 * @since 2024/4/27 13:49
 */
interface MetaDatable<T> {

    fun setMetadata(uuid: T, key: String, value: MetaDataValue<*>)

    fun getMetadata(uuid: T, key: String): MetaDataValue<*>?

    fun getAllMetadata(uuid: T): Map<String, MetaDataValue<*>>

    fun hasMetadata(uuid: T, key: String): Boolean

    fun removeMetadata(uuid: T, key: String)

}