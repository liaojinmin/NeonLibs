package me.neon.libs.chunk

import com.google.gson.JsonObject
import me.neon.libs.chunk.DefaultData.MyBlockData
import me.neon.libs.chunk.data.DataSerializer
import me.neon.libs.chunk.data.ICustomData
import me.neon.libs.chunk.data.SerializationRegistry
import me.neon.libs.chunk.index.VKey
import me.neon.libs.chunk.index.VKey.Companion.fromString

/**
 * NeonEngine
 * me.neon.engine.chunk
 *
 * @author 老廖
 * @since 2025/10/27 19:16
 */
object DefaultData: DataSerializer<MyBlockData> {

    data class MyBlockData(
        override val key: VKey = "MyBlockData".fromString(),
        val color: String,
        val durability: Int
    ) : ICustomData {

        override fun toString(): String {
            return "MyBlockData(key=$key, color='$color', durability=$durability)"
        }
    }

    override val typeClass: Class<MyBlockData> = MyBlockData::class.java

    override fun serialize(obj: MyBlockData): JsonObject {
        return JsonObject().apply {
            addProperty("color", obj.color)
            addProperty("durability", obj.durability)
        }
    }

    override fun deserialize(json: JsonObject): MyBlockData {
        return MyBlockData(
            color = json.get("color").asString,
            durability = json.get("durability").asInt
        )
    }

    fun init() {
        // 1️⃣ 注册序列化器
        SerializationRegistry.register(this)
    }

}