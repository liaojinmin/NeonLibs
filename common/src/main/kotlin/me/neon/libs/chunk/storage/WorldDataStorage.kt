package me.neon.libs.chunk.storage

import me.neon.libs.chunk.index.VPos
import me.neon.libs.chunk.data.ICustomData
import me.neon.libs.chunk.index.VChunk
import me.neon.libs.chunk.index.VKey
import me.neon.libs.chunk.index.VRegion
import org.bukkit.Chunk
import org.bukkit.World
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * NeonEngine
 * me.neon.engine.chunk
 *
 * @author 老廖
 * @since 2025/10/27 17:57
 */
class WorldDataStorage(
    val world: World
) {

    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    private val baseDir: File = File(world.worldFolder, "neonlibs").apply {
        if (!exists()) {
            mkdirs()
        }
    }

    private val regionDir = File(baseDir, "region").apply { mkdirs() }

    private val regions: ConcurrentHashMap<VRegion, RegionFile> = ConcurrentHashMap()

    init {
        startSchedule()
    }

    // 获取 Region 文件
    private fun getRegionFile(coord: VChunk): RegionFile {
        val regionCoord = coord.region
        return regions.getOrPut(regionCoord) {
            val file = File(regionDir, "r.${regionCoord.x}.${regionCoord.z}.neon")
            RegionFile(file, regionCoord)
        }
    }

    fun loadChunk(chunk: Chunk): ChunkDataStorage {
        return loadChunk(VChunk(chunk.x, chunk.z))
    }

    fun saveChunk(chunk: Chunk) {
        saveChunk(VChunk(chunk.x, chunk.z))
    }

    fun loadChunk(vChunk: VChunk): ChunkDataStorage {
        // 直接尝试读，内部有缓存
        return getRegionFile(vChunk).readChunk(vChunk)
    }

    fun saveChunk(vChunk: VChunk) {
        val regionFile = getRegionFile(vChunk)
        regionFile.getChunk(vChunk)?.let {
            if (it.isEmpty()) return
            regionFile.enqueueChunk(it)
        }
    }

    // ----------------------------------------
    // 块级 API
    // ----------------------------------------
    fun setBlockData(chunk: Chunk, pos: VPos, key: VKey, data: ICustomData) {
        val chunkDataStorage = loadChunk(chunk)
        chunkDataStorage.setBlockData(pos, key, data)
        saveChunk(chunkDataStorage.vChunk)
    }

    fun getBlockData(chunk: Chunk, pos: VPos, key: VKey): ICustomData? {
        val chunkDataStorage = loadChunk(chunk)
        return chunkDataStorage.getBlockData(pos, key)
    }

    fun removeBlockData(chunk: Chunk, pos: VPos, key: VKey) {
        val chunkDataStorage = loadChunk(chunk)
        chunkDataStorage.removeBlockData(pos, key)
        saveChunk(chunk)
    }

    fun setChunkData(chunk: Chunk, key: VKey, data: ICustomData) {
        val chunkDataStorage = loadChunk(chunk)
        chunkDataStorage.setChunkData(key, data)
        saveChunk(chunkDataStorage.vChunk)
    }

    fun getChunkData(chunk: Chunk, key: VKey): ICustomData? {
        val chunkDataStorage = loadChunk(chunk)
        return chunkDataStorage.getChunkData(key)
    }

    fun removeChunkData(chunk: Chunk, key: VKey) {
        val chunkDataStorage = loadChunk(chunk)
        chunkDataStorage.removeChunkData(key)
        saveChunk(chunk)
    }

    // ----------------------------------------
    // 全世界保存和卸载
    // ----------------------------------------
    fun unload() {
        regions.values.forEach { regionFile ->
            // 逐个写入缓存中属于这个 Region 的 chunk
            regionFile.flushQueue(true)
        }
        stopSchedule()
        regions.values.forEach { it.close() }
        regions.clear()
    }

    private fun startSchedule() {
        scheduler.scheduleAtFixedRate({ regions.values.forEach(RegionFile::tick) }, 1, 1, TimeUnit.SECONDS)
    }

    private fun stopSchedule() {
        scheduler.shutdownNow()
    }


}
