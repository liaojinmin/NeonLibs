package me.neon.libs.chunk

import me.neon.libs.chunk.storage.WorldDataStorage
import me.neon.libs.core.LifeCycle
import me.neon.libs.core.inject.Awake
import me.neon.libs.event.SubscribeEvent
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent

/**
 * NeonEngine
 * me.neon.engine.chunk
 *
 * @author 老廖
 * @since 2025/10/27 18:04
 */
object WorldDataManager {

    private val worlds = mutableMapOf<String, WorldDataStorage>()

    fun getWorldDataStorage(world: World): WorldDataStorage? {
        return worlds[world.name]
    }

    fun onWorldLoad(world: World) {
        // DungeonPlus 的世界直接排除
        if (world.name.startsWith("dungeon_")) {
            return
        }

        if (worlds.containsKey(world.name)) return
        worlds[world.name] = WorldDataStorage(world)
    }

    fun onWorldUnload(world: World) {
        worlds.remove(world.name)?.unload()
    }

    fun onChunkLoad(chunk: Chunk) {
        val storage = worlds[chunk.world.name] ?: return
        storage.loadChunk(chunk)
    }

    fun onChunkUnload(chunk: Chunk) {
        //println("onChunkUnload ${chunk.x} ${chunk.z}")
        val storage = worlds[chunk.world.name] ?: return
        storage.saveChunk(chunk)
    }

    @SubscribeEvent
    private fun onWorldLoad(e: WorldLoadEvent) = onWorldLoad(e.world)

    @SubscribeEvent
    private fun onWorldUnload(e: WorldUnloadEvent) = onWorldUnload(e.world)

    @SubscribeEvent
    private fun onChunkLoad(e: ChunkLoadEvent) = onChunkLoad(e.chunk)

    @SubscribeEvent
    private fun onChunkUnload(e: ChunkUnloadEvent) = onChunkUnload(e.chunk)

}