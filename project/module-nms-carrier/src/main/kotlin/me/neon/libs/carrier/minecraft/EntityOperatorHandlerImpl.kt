package me.neon.libs.carrier.minecraft

import com.mojang.datafixers.util.Pair
import me.neon.libs.carrier.minecraft.meta.CarrierMeta
import me.neon.libs.carrier.minecraft.meta.NMSEntityType
import me.neon.libs.taboolib.nms.MinecraftVersion
import me.neon.libs.taboolib.nms.dataSerializerBuilder
import me.neon.libs.taboolib.nms.sendAsyncPacket
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.tabooproject.reflex.Reflex.Companion.getProperty
import java.util.*


/**
 * NeonDesire
 * me.neon.desire.nms.minecraft
 *
 * @author 老廖
 * @since 2024/4/27 8:48
 */
internal class EntityOperatorHandlerImpl: EntityOperatorHandler {

    private val isUniversal = MinecraftVersion.isUniversal
    private val major = MinecraftVersion.major
    private val majorLegacy = MinecraftVersion.majorLegacy

    private fun sendPacket(player: List<Player>, packet: Any) {
        player.forEach { it.sendAsyncPacket(packet) }
    }

    override fun destroyEntity(player: List<Player>, entityId: Int) {
        sendPacket(player, NMSPacketPlayOutEntityDestroy(entityId))
    }

    override fun teleportEntity(player: List<Player>, entityId: Int, location: Location, onGround: Boolean) {
        // 计算视角
        val yaw = (location.yaw * 256 / 360).toInt().toByte()
        val pitch = (location.pitch * 256 / 360).toInt().toByte()
        // 版本判断
        val packet: Any = when (major) {
            // 1.9, 1.10, 1.11, 1.12, 1.13, 1.14, 1.15, 1.16
            in 1..8 -> NMS9PacketPlayOutEntityTeleport().also {
                it.a(dataSerializerBuilder {
                    writeVarInt(entityId)
                    writeDouble(location.x)
                    writeDouble(location.y)
                    writeDouble(location.z)
                    // 在传送包下，yaw 与 pitch的读取顺序颠倒
                    writeByte(yaw)
                    writeByte(pitch)
                    writeBoolean(onGround)
                }.build() as NMS9PacketDataSerializer)
            }
            // 1.17, 1.18, 1.19
            // 使用带有 DataSerializer 的构造函数生成数据包
            9, 10, 11, 12 -> NMSPacketPlayOutEntityTeleport(dataSerializerBuilder {
                writeVarInt(entityId)
                writeDouble(location.x)
                writeDouble(location.y)
                writeDouble(location.z)
                writeByte(yaw)
                writeByte(pitch)
                writeBoolean(onGround)
            }.build() as NMSPacketDataSerializer)
            // 不支持
            else -> error("Unsupported version.")
        }
        sendPacket(player, packet)
    }

    override fun sendCustomName(player: List<Player>, entityId: Int, name: String, isVisible: Boolean) {
        createAndSendEntityMetadata(player, entityId, *arrayOf(getMetaEntityChatBaseComponent(2, name), createBooleanMeta(3, isVisible)))
    }

    override fun sendEquipment(player: List<Player>, entityId: Int, slot: EquipmentSlot, itemStack: ItemStack) {
        sendEquipment(player, entityId, mapOf(slot to itemStack))
    }

    override fun sendEquipment(player: List<Player>, entityId: Int, equipment: Map<EquipmentSlot, ItemStack>) {
        when {
            // 从 1.16 开始每个包支持多个物品
            majorLegacy >= 11600 -> {
                val items = equipment.map { Pair(it.key.toNMSEnumItemSlot(), CraftItemStack19.asNMSCopy(it.value)) }
                sendPacket(player, NMSPacketPlayOutEntityEquipment(entityId, items))
            }
            // 低版本
            else -> {
                equipment.forEach { (k, v) ->
                    sendPacket(player, NMS13PacketPlayOutEntityEquipment(entityId, k.toNMS13EnumItemSlot(), CraftItemStack13.asNMSCopy(v)))
                }
            }
        }
    }

    override fun sendEntityMetadata(player: List<Player>, entityId: Int, carrierMeta: CarrierMeta) {
        createAndSendEntityMetadata(player, entityId, *carrierMeta.adapt())
    }

    override fun sendMount(player: List<Player>, entityId: Int, mount: IntArray) {
        if (isUniversal) {
            sendPacket(player, NMSPacketPlayOutMount(dataSerializerBuilder {
                writeVarInt(entityId)
                writeVarIntArray(mount)
            }.build() as NMSPacketDataSerializer))
        } else {
            sendPacket(player, NMS16PacketPlayOutMount().also {
                it.a(dataSerializerBuilder {
                    writeVarInt(entityId)
                    writeVarIntArray(mount)
                }.build() as NMS16PacketDataSerializer)
            })
        }
    }

    private fun createAndSendEntityMetadata(player: List<Player>, entityId: Int, vararg metadata: Any) {
        // 1.19.3 变更为 record 类型，因此无法兼容之前的写法
        if (majorLegacy >= 11903) {
            //  sendPacket(player, NMS120.INSTANCE.createEntityMetadata(entityId, *metadata))
        } else if (isUniversal) {
            sendPacket(player, NMSPacketPlayOutEntityMetadata(dataSerializerBuilder {
                writeVarInt(entityId)
                writeMetadata(metadata.toList())
            }.build() as NMSPacketDataSerializer))
        } else {
            sendPacket(player, NMS16PacketPlayOutEntityMetadata().also {
                it.a(dataSerializerBuilder {
                    writeVarInt(entityId)
                    writeMetadata(metadata.toList())
                }.build() as NMS16PacketDataSerializer)
            })
        }
    }

    override fun createEntityType(entityType: EntityType): Any {
        if (MinecraftVersion.major < 5) {
            return NMSEntityType.adapt(entityType).id
        }
        val names = ArrayList<String>()
        names +=  if (entityType != EntityType.DROPPED_ITEM) entityType.name.uppercase() else "ITEM"
        names += NMSEntityType.adapt(entityType).id.toString()
        names.forEach {
            kotlin.runCatching {
                return NMS16EntityTypes::class.java.getProperty<Any>(it, isStatic = true)!!
            }
        }
        error("不支持的类型 $entityType $names")
    }

    override fun createItemStackMeta(index: Int, itemStack: ItemStack): Any {
        return when {
            majorLegacy >= 11900 -> {
                NMSDataWatcherItem(
                    NMSDataWatcherObject(index, NMSDataWatcherRegistry.ITEM_STACK),
                    CraftItemStack19.asNMSCopy(itemStack)
                )
            }
            majorLegacy >= 11300 -> {
                NMS16DataWatcherItem(
                    NMS16DataWatcherObject(index, NMS16DataWatcherRegistry.g),
                    CraftItemStack16.asNMSCopy(itemStack)
                )
            }
            majorLegacy >= 11200 -> {
                NMS12DataWatcherItem(
                    NMS12DataWatcherObject(index, NMS12DataWatcherRegistry.f),
                    CraftItemStack12.asNMSCopy(itemStack)
                )
            }
            else -> {
                NMS9DataWatcherItem(
                    NMS9DataWatcherObject(index, NMS9DataWatcherRegistry.f),
                    com.google.common.base.Optional.fromNullable(CraftItemStack9.asNMSCopy(itemStack))
                )
            }
        }
    }

    override fun createStringMeta(index: Int, value: String): Any {
        return if (MinecraftVersion.majorLegacy >= 11900) {
            NMSDataWatcherItem(NMSDataWatcherObject(index, NMSDataWatcherRegistry.STRING), value)
        } else {
            NMS16DataWatcherItem(NMS16DataWatcherObject(index, NMS16DataWatcherRegistry.d), value)
        }

    }

    override fun createByteMeta(index: Int, value: Byte): Any {
        return if (MinecraftVersion.majorLegacy >= 11900) {
            NMSDataWatcherItem(NMSDataWatcherObject(index, NMSDataWatcherRegistry.BYTE), value)
        } else {
            NMS16DataWatcherItem(NMS16DataWatcherObject(index, NMS16DataWatcherRegistry.a), value)
        }
    }

    override fun createBooleanMeta(index: Int, value: Boolean): Any {
        return when {
            MinecraftVersion.majorLegacy >= 11900 -> {
                NMSDataWatcherItem(NMSDataWatcherObject(index, NMSDataWatcherRegistry.BOOLEAN), value)
            }
            MinecraftVersion.majorLegacy >= 11300 -> {
                NMS13DataWatcherItem(NMS13DataWatcherObject(index, NMS13DataWatcherRegistry.i), value)
            }
            else -> {
                NMS11DataWatcherItem(NMS11DataWatcherObject(index, NMS11DataWatcherRegistry.h), value)
            }
        }
    }

    private fun EquipmentSlot.toNMSEnumItemSlot(): NMSEnumItemSlot {
        return when (this) {
            EquipmentSlot.HAND -> NMSEnumItemSlot.MAINHAND
            EquipmentSlot.OFF_HAND -> NMSEnumItemSlot.OFFHAND
            EquipmentSlot.FEET -> NMSEnumItemSlot.FEET
            EquipmentSlot.LEGS -> NMSEnumItemSlot.LEGS
            EquipmentSlot.CHEST -> NMSEnumItemSlot.CHEST
            EquipmentSlot.HEAD -> NMSEnumItemSlot.HEAD
            else -> error("Unknown EquipmentSlot: $this")
        }
    }

    private fun EquipmentSlot.toNMS13EnumItemSlot(): NMS13EnumItemSlot {
        return when (this) {
            EquipmentSlot.HAND -> NMS13EnumItemSlot.MAINHAND
            EquipmentSlot.OFF_HAND -> NMS13EnumItemSlot.OFFHAND
            EquipmentSlot.FEET -> NMS13EnumItemSlot.FEET
            EquipmentSlot.LEGS -> NMS13EnumItemSlot.LEGS
            EquipmentSlot.CHEST -> NMS13EnumItemSlot.CHEST
            EquipmentSlot.HEAD -> NMS13EnumItemSlot.HEAD
            else -> error("Unknown EquipmentSlot: $this")
        }
    }

    private fun craftChatMessageFromString(message: String): Any {
        return CraftChatMessage19.fromString(message)[0]
    }

    private fun getMetaEntityChatBaseComponent(index: Int, rawMessage: String?): Any {
        return when {
            majorLegacy >= 11900 -> {
                NMSDataWatcherItem(
                    NMSDataWatcherObject(index, NMSDataWatcherRegistry.OPTIONAL_COMPONENT),
                    Optional.ofNullable(if (rawMessage == null) null else craftChatMessageFromString(rawMessage) as NMSIChatBaseComponent)
                )
            }
            majorLegacy >= 11300 -> {
                NMS16DataWatcherItem(
                    NMS16DataWatcherObject(index, NMS16DataWatcherRegistry.f),
                    Optional.ofNullable(if (rawMessage == null) null else craftChatMessageFromString(rawMessage) as NMS16IChatBaseComponent)
                )
            }
            else -> {
                NMS12DataWatcherItem(NMS12DataWatcherObject(index, NMS12DataWatcherRegistry.d), rawMessage ?: "")
            }
        }
    }


}