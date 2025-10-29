package me.neon.libs.chunk.data

import com.google.gson.JsonObject

/**
 * NeonEngine
 * me.neon.engine.chunk.data
 *
 * @author 老廖
 * @since 2025/10/27 18:27
 */
interface DataSerializer<T: ICustomData> {

    val typeClass: Class<T>

    fun serialize(obj: T): JsonObject

    fun deserialize(json: JsonObject): T

}
