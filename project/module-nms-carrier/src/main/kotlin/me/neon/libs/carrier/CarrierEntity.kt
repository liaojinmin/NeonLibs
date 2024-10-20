package me.neon.libs.carrier

import org.bukkit.entity.Player
import java.util.*

/**
 * NeonLibs
 * me.neon.libs.carrier
 *
 * @author 老廖
 * @since 2024/6/11 8:24
 */
interface CarrierEntity {

    val entityId: Int

    val uniqueId: UUID

    fun spawn(player: Player)

    fun destroy(player: Player)

    fun close()

    fun interact(player: Player, action: CarrierAction, isMainHand: Boolean)

}