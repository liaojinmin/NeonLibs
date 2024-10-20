package me.neon.libs.carrier.minecraft

import me.neon.libs.carrier.minecraft.meta.CarrierMeta
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/**
 * NeonDesire
 * me.neon.desire.nms.minecraft
 *
 * @author 老廖
 * @since 2024/4/27 8:48
 */
interface EntityOperatorHandler {

    /**
     * 移除数据包实体
     *
     * @param player 数据包接收人
     * @param entityId 实体序号
     */
    fun destroyEntity(player: List<Player>, entityId: Int)
    fun destroyEntity(player: Player, entityId: Int) {
        destroyEntity(listOf(player), entityId)
    }

    /**
     * 传送数据包实体到另一个位置
     *
     * @param player 数据包接收人
     * @param entityId 实体序号
     * @param location 传送位置
     * @param onGround 是否在地面上
     */
    fun teleportEntity(player: List<Player>, entityId: Int, location: Location, onGround: Boolean = false)
    fun teleportEntity(player: Player, entityId: Int, location: Location, onGround: Boolean = false) {
        teleportEntity(listOf(player), entityId, location, onGround)
    }

    /**
     * 发送实体名称
     */
    fun sendCustomName(player: List<Player>, entityId: Int, name: String, isVisible: Boolean = true)
    fun sendCustomName(player: Player, entityId: Int, name: String, isVisible: Boolean = true) {
        sendCustomName(listOf(player), entityId, name, isVisible)
    }

    /**
     * 更新数据包实体的装备信息
     *
     * @param player 数据包接收人
     * @param entityId 实体序号
     * @param slot 装备槽
     * @param itemStack 物品对象
     */
    fun sendEquipment(player: List<Player>, entityId: Int, slot: EquipmentSlot, itemStack: ItemStack)
    fun sendEquipment(player: Player, entityId: Int, slot: EquipmentSlot, itemStack: ItemStack) {
        sendEquipment(listOf(player), entityId, slot, itemStack)
    }

    /**
     * 更新数据包实体的装备信息
     *
     * @param player 数据包接收人
     * @param entityId 实体序号
     * @param equipment 装备信息
     */
    fun sendEquipment(player: List<Player>, entityId: Int, equipment: Map<EquipmentSlot, ItemStack>)
    fun sendEquipment(player: Player, entityId: Int, equipment: Map<EquipmentSlot, ItemStack>) {
        sendEquipment(listOf(player), entityId, equipment)
    }

    /**
    * 更新数据包骑乘状态
    */
    fun sendMount(player: List<Player>, entityId: Int, mount: IntArray)
    fun sendMount(player: Player, entityId: Int, mount: IntArray) {
        sendMount(listOf(player), entityId, mount)
    }


    /**
     * 更新数据包 MetaData
     */
    fun sendEntityMetadata(player: List<Player>, entityId: Int, carrierMeta: CarrierMeta)
    fun sendEntityMetadata(player: Player, entityId: Int, carrierMeta: CarrierMeta) {
        sendEntityMetadata(listOf(player), entityId, carrierMeta)
    }


    fun createEntityType(entityType: EntityType): Any
    fun createItemStackMeta(index: Int, itemStack: ItemStack): Any
    fun createStringMeta(index: Int, value: String): Any
    fun createByteMeta(index: Int, value: Byte): Any
    fun createBooleanMeta(index: Int, value: Boolean): Any



}