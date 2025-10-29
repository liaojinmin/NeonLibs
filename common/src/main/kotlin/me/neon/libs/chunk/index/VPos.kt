package me.neon.libs.chunk.index

import org.bukkit.Location
import org.bukkit.block.Block

/**
 * NeonEngine
 * me.neon.engine.chunk.data
 *
 * @author 老廖
 * @since 2025/10/27 18:21
 */
data class VPos(
    val x: Int,
    val y: Int,
    val z: Int
) {

    companion object {

        fun Location.of(): VPos {
            return VPos(blockX, blockY, blockZ)
        }

        fun Block.of(): VPos {
            return VPos(x, y, z)
        }

    }

}