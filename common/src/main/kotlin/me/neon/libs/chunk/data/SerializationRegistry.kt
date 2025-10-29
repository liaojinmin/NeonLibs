package me.neon.libs.chunk.data

import com.google.gson.JsonObject

/**
 * NeonEngine
 * me.neon.engine.chunk.data
 *
 * @author 老廖
 * @since 2025/10/27 18:16
 */
object SerializationRegistry {

    private val serializers = mutableMapOf<String, DataSerializer<out ICustomData>>()

    fun <T: ICustomData> register(serializer: DataSerializer<T>) {
        serializers[serializer.typeClass.simpleName] = serializer
    }

    fun serialize(obj: ICustomData): JsonObject {
        val entry = serializers.entries.find { (_, serializer) ->
            // 使用 serializer 泛型的 reified 类型判断
            val clazz = (serializer as? DataSerializer<*>)?.typeClass
            clazz?.isInstance(obj) == true
        } ?: throw IllegalArgumentException("未注册的序列化器: ${obj::class.simpleName}")

        @Suppress("UNCHECKED_CAST")
        val serializer = entry.value as DataSerializer<ICustomData>
        val json = serializer.serialize(obj)
        json.addProperty("_type", entry.key)
        return json
    }

    fun deserialize(json: JsonObject): Any {
        val type = json.get("_type")?.asString
            ?: throw IllegalArgumentException("JSON 中缺少 _type 字段")

        val serializer = serializers[type]
            ?: throw IllegalArgumentException("未注册的类型: $type")

        @Suppress("UNCHECKED_CAST")
        return (serializer as DataSerializer<ICustomData>).deserialize(json)
    }

}
