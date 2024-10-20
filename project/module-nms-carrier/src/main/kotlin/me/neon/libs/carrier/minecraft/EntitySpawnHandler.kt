package me.neon.libs.carrier.minecraft

import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.*

/**
 * NeonDesire
 * me.neon.desire.nms.minecraft
 *
 * @author 老廖
 * @since 2024/4/27 8:58
 */
interface EntitySpawnHandler {

    /**
     * 生成数据包实体
     *
     * @param player 数据包接收人
     * @param entityType 实体类型
     * @param entityId 实体序号
     * @param uuid 实体 UUID
     * @param location 生成坐标
     * @param data 特殊数据
     */
    fun spawnEntity(player: Player, entityType: EntityType, entityId: Int, uuid: UUID, location: Location, data: Int = 0)

    /**
     * 在 1.18 及以下版本生成 EntityLiving 类型的数据包实体，在 1.19 版本中被 [spawnEntity] 取代。
     *
     * 在 1.19 及以上版本调用时会产生异常。
     *
     * @param player 数据包接收人
     * @param entityType 实体类型
     * @param entityId 实体序号
     * @param uuid 实体 UUID
     * @param location 实体坐标
     */
    fun spawnEntityLiving(player: Player, entityType: EntityType, entityId: Int, uuid: UUID, location: Location)

}