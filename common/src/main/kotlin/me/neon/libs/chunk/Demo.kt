package me.neon.libs.chunk

import com.google.gson.JsonObject
import me.neon.libs.chunk.Demo.MyBlockData
import me.neon.libs.chunk.index.VPos.Companion.of
import me.neon.libs.chunk.data.DataSerializer
import me.neon.libs.chunk.data.ICustomData
import me.neon.libs.chunk.data.SerializationRegistry
import me.neon.libs.chunk.index.VKey
import me.neon.libs.chunk.index.VKey.Companion.fromString

import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player

/**
 * NeonEngine
 * me.neon.engine.chunk
 *
 * @author 老廖
 * @since 2025/10/27 19:16
 */
object Demo: DataSerializer<MyBlockData> {

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

    init {
        // 1️⃣ 注册序列化器
        SerializationRegistry.register(this)
    }

    /*
    @SubscribeEvent
    fun onPlayerInteract(e: PlayerInteractEvent) {
        val block = e.clickedBlock ?: return
        if (block.type == Material.AIR) return
        if (e.hand != EquipmentSlot.HAND) return
        if (e.action == Action.LEFT_CLICK_BLOCK) {
            test(e.player, block.world, block)
        } else {
            if (e.action == Action.RIGHT_CLICK_BLOCK) {
                parse(e.player, block.world, block)
            }
        }
    }

     */

    private fun parse(player: Player, world: World, click: Block) {
        // 2️⃣ 获取世界数据存储
        val storage = WorldDataManager.getWorldDataStorage(world)
            ?: error("WorldDataStorage 未加载")
        val loadedData = storage.getBlockData(click.chunk, click.location.of(), "example".fromString()) as? MyBlockData
        player.sendMessage("Loaded color=${loadedData?.color}, durability=${loadedData?.durability}")
    }

    private fun test(player: Player, world: World, click: Block) {
        // 2获取世界数据存储
        val storage = WorldDataManager.getWorldDataStorage(world)
            ?: error("WorldDataStorage 未加载")
        // 存储自定义方块数据
        val pos = click.location.of()
        storage.setBlockData(click.chunk, pos, "example".fromString(), MyBlockData( color = "red", durability =  100))
        player.sendMessage("Block data saved in chunk.")
        // 重新加载验证
        val reloadedChunk = storage.getBlockData(click.chunk, pos, "example".fromString()) as? MyBlockData
        player.sendMessage("Loaded $reloadedChunk")
    }

}