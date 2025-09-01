package me.neon.libs.carrier.minecraft.meta

import org.bukkit.entity.EntityType

/**
 * NeonDesire
 * me.neon.desire.nms.carrierMeta
 *
 * @author 老廖
 * @since 2024/4/27 9:43
 */
enum class NMSEntityType(val id: Int) {

    ARMOR_STAND(78),

    ARROW(60),

    DROPPED_ITEM(2),

    HUSK(23),

    // 生物
    ZOMBIE(110),

    SKELETON(98),

    CREEPER(88),

    SPIDER(102),

    ENDERMAN(97),

    VILLAGER(120);

    companion object {

        fun adapt(entityType: EntityType): NMSEntityType {
            return runCatching {
                NMSEntityType.valueOf(entityType.name)
            }.getOrNull() ?: error("不支持的类型 $entityType")
        }

    }
}