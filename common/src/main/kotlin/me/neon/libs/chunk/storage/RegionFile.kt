package me.neon.libs.chunk.storage

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import me.neon.libs.chunk.index.VChunk
import me.neon.libs.chunk.index.VRegion
import java.io.*
import java.util.concurrent.ConcurrentHashMap


/**
 * NeonEngine RegionFile with reusable sectors
 * me.neon.engine.chunk
 *
 * @author 老廖
 * @since 2025/10/27 17:54
 */

class RegionFile(internal val file: File, val vRegion: VRegion) {

    internal data class PendingChunk(
        val chunk: ChunkDataStorage,
        val enqueueTime: Long = System.currentTimeMillis()
    )

    internal companion object {

        private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

        const val REGION_SIZE = 32

        const val HEADER_SIZE = 4096

        const val SECTOR_BYTES = 4096

        const val BATCH_SIZE = 32

        const val MAX_WAIT_MS = 5000L
    }

    private val raf = RandomAccessFile(file, "rw")

    private val offsets = IntArray(REGION_SIZE * REGION_SIZE)

    private val freeRanges = mutableListOf<IntRange>() // 连续空闲扇区

    private val chunkHashes = mutableMapOf<VChunk, Int>()

    private val chunksCache = mutableMapOf<VChunk, ChunkDataStorage>()

    private val pendingQueue = ConcurrentHashMap<VChunk, PendingChunk>()

    init {
        initialize()
    }

    private var initialized = false

    fun initialize() {
        if (initialized) return
        if (raf.length() < HEADER_SIZE) raf.setLength(HEADER_SIZE.toLong())
        readOffsets()
        rebuildFreeRanges()
        initialized = true
    }

    fun getChunks(): List<ChunkDataStorage> = chunksCache.values.toList()

    fun getChunk(vChunk: VChunk): ChunkDataStorage? {
        ensureChunkInRegion(vChunk)
        return chunksCache[vChunk]
    }

    fun enqueueChunk(chunk: ChunkDataStorage) {
        ensureChunkInRegion(chunk.vChunk)
        val pending = pendingQueue.put(chunk.vChunk, PendingChunk(chunk))
        // 如果队列达到阈值，立即批量写入
        if (pendingQueue.size >= BATCH_SIZE) flushQueue()
    }

    @Synchronized
    fun flushQueue(nowUpdate: Boolean = false) {
        if (pendingQueue.isEmpty()) return

        val now = System.currentTimeMillis()
        val chunksToWrite = mutableListOf<ChunkDataStorage>()

        val iterator = pendingQueue.entries.iterator()
        while (iterator.hasNext()) {
            val (vChunk, pending) = iterator.next()
            if (nowUpdate || (now - pending.enqueueTime >= MAX_WAIT_MS || pendingQueue.size >= BATCH_SIZE)) {
                chunksToWrite += pending.chunk
                iterator.remove()
            }
        }
        if (chunksToWrite.isNotEmpty()) {
            writeChunks(chunksToWrite)
        }
    }

    @Synchronized
    fun writeChunks(chunks: Collection<ChunkDataStorage>) {
        for (chunk in chunks) {
           // println("准备写入: ${chunk.vChunk}")
            ensureChunkInRegion(chunk.vChunk)

            val jsonStr = gson.toJson(chunk.toJson())
            if (chunk.isEmpty() || !isChunkChanged(chunk.vChunk, jsonStr)) {
               // println("  内容空或相同，跳过存入...")
                continue
            }

            // 更新缓存
            cachePut(chunk, jsonStr)

            val index = getIndex(chunk.vChunk.x, chunk.vChunk.z)
            val data = jsonStr.toByteArray()
            val sectorsNeeded = (data.size + 5 + SECTOR_BYTES - 1) / SECTOR_BYTES
            val sectorOffset = allocateSectors(sectorsNeeded)

            raf.seek(sectorOffset * SECTOR_BYTES.toLong())
            raf.writeInt(data.size)
            raf.write(data)

            val oldOffset = offsets[index]
            if (oldOffset != 0) freeOldSectors(oldOffset)

            offsets[index] = (sectorOffset shl 8) or sectorsNeeded
            raf.seek(index * 4L)
            raf.writeInt(offsets[index])

            chunksCache[chunk.vChunk] = chunk
        }
        rebuildFreeRanges() // 批量完成后再重建
    }

    /** 读取单个 chunk */
    @Synchronized
    fun readChunk(vChunk: VChunk): ChunkDataStorage {
        ensureChunkInRegion(vChunk)

        return chunksCache.getOrPut(vChunk) {
            val c = privateReadChunk(vChunk) ?: ChunkDataStorage(vChunk)
            cachePut(c)
            c
        }
    }

    @Synchronized
    fun readAllChunks(): Map<VChunk, ChunkDataStorage> {
        val allChunks = mutableMapOf<VChunk, ChunkDataStorage>()

        for (cz in 0 until REGION_SIZE) {
            for (cx in 0 until REGION_SIZE) {
                val vChunk = VChunk((vRegion.x shl 5) + cx, (vRegion.z shl 5) + cz)
                readOldChunkSafe(vChunk)
                try {
                    val chunk = readOldChunkSafe(vChunk)
                    if (chunk != null) {
                        cachePut(chunk)
                        allChunks[vChunk] = chunk
                    }
                } catch (ex: Exception) {
                    println("解析 $vChunk 时出错: ${ex.message}")
                }
            }
        }

        return allChunks
    }

    fun close() = raf.close()

    internal fun tick() {
        flushQueue()
    }

    private fun isChunkChanged(vChunk: VChunk, jsonStr: String): Boolean {
        val hash = jsonStr.hashCode()
        val changed = chunkHashes[vChunk] != hash
        if (changed) chunkHashes[vChunk] = hash
        return changed
    }

    private fun ensureChunkInRegion(chunk: VChunk) {
        if (!vRegion.contains(chunk)) {
            throw IllegalArgumentException("Chunk $chunk 不属于 $vRegion")
        }
    }

    private fun readOldChunkSafe(vChunk: VChunk): ChunkDataStorage? {
        return chunksCache[vChunk] ?: privateReadChunk(vChunk)
    }


    private fun cachePut(chunk: ChunkDataStorage, jsonStr: String? = null) {
        chunksCache[chunk.vChunk] = chunk
        chunkHashes[chunk.vChunk] = jsonStr?.hashCode() ?: chunk.toJson().hashCode()
    }

    private fun readOffsets() {
        raf.seek(0)
        for (i in offsets.indices) offsets[i] = raf.readInt()
    }

    private fun privateReadChunk(vChunk: VChunk): ChunkDataStorage? {
        val index = getIndex(vChunk.x, vChunk.z)

        val offsetEntry = offsets[index]

        if (offsetEntry == 0) return null

        val sectorOffset = offsetEntry shr 8
        val numSectors = offsetEntry and 0xFF
        if (sectorOffset + numSectors > raf.length() / SECTOR_BYTES) return null

        raf.seek(sectorOffset * SECTOR_BYTES.toLong())
        val length = raf.readInt()
        if (length <= 0 || length > numSectors * SECTOR_BYTES) return null

        val data = ByteArray(length)
        raf.readFully(data)
        val jsonStr = String(data)

        return try {
            ChunkDataStorage.fromJson(vChunk, JsonParser.parseString(jsonStr).asJsonObject)
        } catch (ex: Exception) {
            println("解析 $vChunk 时出错: ${ex.message}")
            null
        }
    }

    private fun getIndex(cx: Int, cz: Int) = (cx and 31) + (cz and 31) * 32

    /** 重建空闲扇区列表 */
    private fun rebuildFreeRanges() {
        val totalSectors = (raf.length() / SECTOR_BYTES).toInt()
        val used = BooleanArray(totalSectors)
        used[0] = true
        for (offset in offsets) {
            if (offset != 0) {
                val sectorOffset = offset shr 8
                val numSectors = offset and 0xFF
                for (i in 0 until numSectors) if (sectorOffset + i < totalSectors) used[sectorOffset + i] = true
            }
        }
        freeRanges.clear()
        var start = -1
        for (i in 1 until totalSectors) {
            if (!used[i]) {
                if (start == -1) start = i
            } else if (start != -1) {
                freeRanges += start until i
                start = -1
            }
        }
        if (start != -1) freeRanges += start until totalSectors
    }

    private fun allocateSectors(sectorsNeeded: Int): Int {
        for ((i, range) in freeRanges.withIndex()) {
            if (range.count() >= sectorsNeeded) {
                val offset = range.first
                val newRange = (offset + sectorsNeeded) until range.last
                if (newRange.isEmpty()) freeRanges.removeAt(i) else freeRanges[i] = newRange
                return offset
            }
        }
        // 没找到空闲，扩展文件
        val totalSectors = (raf.length() / SECTOR_BYTES).toInt()
        raf.setLength((totalSectors + sectorsNeeded) * SECTOR_BYTES.toLong())
        return totalSectors
    }

    private fun freeOldSectors(offsetEntry: Int) {
        val sectorOffset = offsetEntry shr 8
        val numSectors = offsetEntry and 0xFF
        freeRanges += sectorOffset until (sectorOffset + numSectors)
        // 可考虑合并相邻区间，避免碎片
        mergeFreeRanges()
    }

    private fun mergeFreeRanges() {
        if (freeRanges.isEmpty()) return
        freeRanges.sortBy { it.first }
        val merged = mutableListOf<IntRange>()
        var current = freeRanges.first()
        for (range in freeRanges.drop(1)) {
            if (current.last == range.first) {
                current = current.first until range.last
            } else {
                merged.add(current)
                current = range
            }
        }
        merged.add(current)
        freeRanges.clear()
        freeRanges.addAll(merged)
    }

    /*
   /** 写入单个 chunk，如果内容未变化则跳过 */
   @Synchronized
   fun writeChunk(chunk: ChunkDataStorage) {
       ensureChunkInRegion(chunk.vChunk)

       val jsonStr = gson.toJson(chunk.toJson())
       if (!isChunkChanged(chunk.vChunk, jsonStr)) return
       // 更新缓存
       cachePut(chunk, jsonStr)

       val index = getIndex(chunk.vChunk.x, chunk.vChunk.z)
       val oldOffset = offsets[index]

       // ---- ② 写入新数据 ----
       val data = jsonStr.toByteArray()
       val sectorsNeeded = (data.size + 5 + SECTOR_BYTES - 1) / SECTOR_BYTES
       val sectorOffset = allocateSectors(sectorsNeeded)

       raf.seek(sectorOffset * SECTOR_BYTES.toLong())
       raf.writeInt(data.size)
       raf.write(data)

       // ---- ③ 释放旧扇区（如有） ----
       if (oldOffset != 0) freeOldSectors(oldOffset)

       // ---- ④ 更新 offset 表 ----
       offsets[index] = (sectorOffset shl 8) or sectorsNeeded
       raf.seek(index * 4L)
       raf.writeInt(offsets[index])

       rebuildFreeRanges()
   }
    */

}