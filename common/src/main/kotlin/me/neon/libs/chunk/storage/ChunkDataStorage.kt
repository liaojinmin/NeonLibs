package me.neon.libs.chunk.storage

import com.google.gson.JsonObject
import me.neon.libs.chunk.index.VPos
import me.neon.libs.chunk.data.ICustomData
import me.neon.libs.chunk.data.SerializationRegistry
import me.neon.libs.chunk.index.VChunk
import me.neon.libs.chunk.index.VKey
import me.neon.libs.chunk.index.VKey.Companion.fromString

/**
 * NeonEngine
 * me.neon.engine.chunk
 * 
 * @author 老廖
 * @since 2025/10/27 18:02
 */
data class ChunkDataStorage internal constructor(val vChunk: VChunk) {

    private val blockDataMap: MutableMap<VPos, MutableMap<VKey, ICustomData>> = mutableMapOf()

    private val chunkDataMap: MutableMap<VKey, ICustomData> = mutableMapOf()

    // 插件存储数据的接口
    /**
     * 为指定插件设置数据
     * @param pluginName 插件名称
     * @param dataKey 数据标识
     * @param data 插件数据
     */
    fun setPluginData(pluginName: String, dataKey: String, data: ICustomData) {
        val vKey = VKey(namespace = pluginName, path = dataKey)
        setChunkData(vKey, data)
    }

    /**
     * 获取指定插件的数据
     * @param pluginName 插件名称
     * @param dataKey 数据标识
     * @return 返回插件的数据，如果没有数据则返回 null
     */
    fun getPluginData(pluginName: String, dataKey: String): ICustomData? {
        val vKey = VKey(namespace = pluginName, path = dataKey)
        return getChunkData(vKey)
    }

    /**
     * 删除指定插件的数据
     * @param pluginName 插件名称
     * @param dataKey 数据标识
     */
    fun removePluginData(pluginName: String, dataKey: String) {
        val vKey = VKey(namespace = pluginName, path = dataKey)
        removeChunkData(vKey)
    }

    /**
     * 检查插件数据是否存在
     * @param pluginName 插件名称
     * @param dataKey 数据标识
     * @return 如果数据存在返回 true，否则返回 false
     */
    fun hasPluginData(pluginName: String, dataKey: String): Boolean {
        val vKey = VKey(namespace = pluginName, path = dataKey)
        return getChunkData(vKey) != null
    }

    fun setBlockData(pos: VPos, key: VKey, data: ICustomData) {
        blockDataMap.getOrPut(pos) { mutableMapOf() }[key] = data
    }
    fun getBlockData(pos: VPos, key: VKey): ICustomData? = blockDataMap[pos]?.get(key)

    fun hasBlockData(pos: VPos, key: VKey): Boolean = blockDataMap[pos]?.containsKey(key) == true

    fun removeBlockData(pos: VPos, key: VKey) {
        blockDataMap[pos]?.remove(key)
        if (blockDataMap[pos]?.isEmpty() == true) blockDataMap.remove(pos)
    }

    fun setChunkData(key: VKey, data: ICustomData) { chunkDataMap[key] = data }

    fun getChunkData(key: VKey): ICustomData? = chunkDataMap[key]

    fun hasChunkData(key: VKey): Boolean = chunkDataMap.containsKey(key)

    fun removeChunkData(key: VKey) { chunkDataMap.remove(key) }

    fun isEmpty(): Boolean {
        return blockDataMap.isEmpty() && chunkDataMap.isEmpty()
    }

    internal fun toJson(): JsonObject {
        val json = JsonObject()

        val blocksJson = JsonObject()
        blockDataMap.forEach { (pos, map) ->
            val posKey = "${pos.x},${pos.y},${pos.z}"
            val mapJson = JsonObject()
            map.forEach { (k, v) ->
                mapJson.add(k.toString(), SerializationRegistry.serialize(v))
            }
            blocksJson.add(posKey, mapJson)
        }
        json.add("blocks", blocksJson)

        val chunkJson = JsonObject()
        chunkDataMap.forEach { (k, v) ->
            chunkJson.add(k.toString(), SerializationRegistry.serialize(v))
        }
        json.add("chunk", chunkJson)

        return json
    }

    internal companion object {

        internal fun fromJson(vChunk: VChunk, json: JsonObject): ChunkDataStorage {
            val entry = ChunkDataStorage(vChunk)
            val blocksJson = json.getAsJsonObject("blocks") ?: JsonObject()
            blocksJson.entrySet().forEach { (posKey, obj) ->
                val parts = posKey.split(",")
                if (parts.size != 3) return@forEach
                val pos = VPos(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
                val map = mutableMapOf<VKey, ICustomData>()
                obj.asJsonObject.entrySet().forEach { (k, v) ->
                    map[k.fromString()] = SerializationRegistry.deserialize(v.asJsonObject) as ICustomData
                }
                entry.blockDataMap[pos] = map
            }

            val chunkJson = json.getAsJsonObject("chunk") ?: JsonObject()
            chunkJson.entrySet().forEach { (k, v) ->
                entry.chunkDataMap[k.fromString()] = SerializationRegistry.deserialize(v.asJsonObject) as ICustomData
            }

            return entry
        }
    }
}
