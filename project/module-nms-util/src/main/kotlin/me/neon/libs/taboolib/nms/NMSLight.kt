package me.neon.libs.taboolib.nms

import me.neon.libs.NeonLibsLoader
import me.neon.libs.taboolib.nms.type.LightType
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.block.Block
import org.bukkit.entity.Player

/**
 * 创建光源
 *
 * @param lightLevel 光照等级
 * @param lightType 光源类型
 * @param update 是否更新区块光照
 * @param viewers 可见玩家
 */
fun Block.createLight(
    lightLevel: Int,
    lightType: LightType = LightType.ALL,
    update: Boolean = true,
    viewers: Collection<Player> = Bukkit.getOnlinePlayers(),
): Boolean {
    if (MinecraftVersion.isLower(MinecraftVersion.V1_12)) {
        throw Exception("Unsupported version")
    }
    if (nmsProxy<NMSLight>(NeonLibsLoader.getInstance()).getRawLightLevel(this, lightType) > lightLevel) {
        nmsProxy<NMSLight>(NeonLibsLoader.getInstance()).deleteLight(this, lightType)
    }
    val result = nmsProxy<NMSLight>(NeonLibsLoader.getInstance()).createLight(this, lightType, lightLevel)
    if (update) {
        updateLight(lightType, viewers)
    }
    return result
}

/**
 * 删除光源
 *
 * @param lightType 光源类型
 * @param update 是否更新区块光照
 * @param viewers 可见玩家
 */
fun Block.deleteLight(
    lightType: LightType = LightType.ALL,
    update: Boolean = true,
    viewers: Collection<Player> = Bukkit.getOnlinePlayers(),
): Boolean {
    if (MinecraftVersion.isLower(MinecraftVersion.V1_12)) {
        throw Exception("Unsupported version")
    }
    val result = nmsProxy<NMSLight>(NeonLibsLoader.getInstance()).deleteLight(this, lightType)
    if (update) {
        updateLight(lightType, viewers)
    }
    return result
}

/**
 * 更新光照
 */
fun Block.updateLight(lightType: LightType, viewers: Collection<Player>) {
    if (MinecraftVersion.isUniversal) {
        nmsProxy<NMSLight>(NeonLibsLoader.getInstance()).updateLightUniversal(this, lightType, viewers)
    } else {
        // 更新邻边区块 (为了防止光只在一个区块的尴尬局面)
        (-1..1).forEach { x ->
            (-1..1).forEach { z ->
                nmsProxy<NMSLight>(NeonLibsLoader.getInstance()).updateLight(world.getChunkAt(chunk.x + x, chunk.z + z), viewers)
            }
        }
    }
}

abstract class NMSLight {

    abstract fun createLight(block: Block, lightType: LightType, lightLevel: Int): Boolean

    abstract fun deleteLight(block: Block, lightType: LightType): Boolean

    abstract fun getRawLightLevel(block: Block, lightType: LightType): Int

    abstract fun setRawLightLevel(block: Block, lightType: LightType, lightLevel: Int)

    abstract fun recalculateLight(block: Block, lightType: LightType)

    abstract fun recalculateLightAround(block: Block, lightType: LightType, lightLevel: Int)

    abstract fun updateLight(chunk: Chunk, viewers: Collection<Player>)

    abstract fun updateLightUniversal(block: Block, lightType: LightType, viewers: Collection<Player>)
}