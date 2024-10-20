package me.neon.libs.carrier

import me.neon.libs.carrier.minecraft.EntityOperatorHandler
import me.neon.libs.carrier.minecraft.EntitySpawnHandler

/**
 * NeonLibs
 * me.neon.libs.carrier
 *
 * @author 老廖
 * @since 2024/9/14 5:19
 */
object PacketAPI {

    fun getEntityOperatorHandler(): EntityOperatorHandler {
        return PacketHandler.entityOperatorHandler
    }

    fun getEntitySpawnHandler(): EntitySpawnHandler {
        return PacketHandler.entitySpawnHandler
    }

}