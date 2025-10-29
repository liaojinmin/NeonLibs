package me.neon.libs.chunk.index

/**
 * NeonEngine
 * me.neon.engine.chunk.index
 *
 * @author 老廖
 * @since 2025/10/29 06:06
 */
data class VRegion(val x: Int, val z: Int) {

    init {
        require(x >= Int.MIN_VALUE shr 5 && z >= Int.MIN_VALUE shr 5) {
            "Region 坐标 ($x,$z) 不合法"
        }
    }

    /**
     * 验证给定区块是否属于本 region
     */
    fun contains(chunk: VChunk): Boolean {
        return (chunk.x shr 5 == x) && (chunk.z shr 5 == z)
    }

    /**
     * 修正区块坐标到本 region 范围内的索引 (0..31)
     */
    fun toLocalIndex(chunk: VChunk): Pair<Int, Int> {
        val localX = chunk.x and 31
        val localZ = chunk.z and 31
        return localX to localZ
    }

    override fun toString(): String = "Region($x,$z)"
}