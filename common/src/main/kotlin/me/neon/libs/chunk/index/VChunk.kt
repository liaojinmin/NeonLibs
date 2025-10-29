package me.neon.libs.chunk.index

import me.neon.libs.chunk.index.VRegion

/**
 * NeonEngine
 * me.neon.engine.chunk.index
 *
 * @author 老廖
 * @since 2025/10/29 06:06
 */
data class VChunk(val x: Int, val z: Int) {
    /** 计算所属 Region 坐标 */
    val region: VRegion
        get() = VRegion(x shr 5, z shr 5)

    override fun toString(): String {
        return "VChunk(x=$x, z=$z, region=$region)"
    }


}