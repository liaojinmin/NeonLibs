package me.neon.libs.carrier.minecraft


import me.neon.libs.carrier.PacketHandler
import me.neon.libs.taboolib.nms.MinecraftVersion
import me.neon.libs.taboolib.nms.dataSerializerBuilder
import me.neon.libs.taboolib.nms.sendAsyncPacket
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
internal class EntitySpawnHandlerImpl: EntitySpawnHandler {

    private val major = MinecraftVersion.major
    private val majorLegacy = MinecraftVersion.majorLegacy
    private val minor = MinecraftVersion.minor

    override fun spawnEntity(player: Player, entityType: EntityType, entityId: Int, uuid: UUID, location: Location, data: Int) {
        // 计算视角
        val yaw = (location.yaw * 256.0f / 360.0f).toInt().toByte()
        val pitch = (location.pitch * 256.0f / 360.0f).toInt().toByte()
        // 版本判断
        val packet: Any = when (major) {
            // 1.9, 1.10, 1.11, 1.12, 1.13
            1, 2, 3, 4, 5 -> NMS9PacketPlayOutSpawnEntity().also {
                it.a(dataSerializerBuilder {
                    writeVarInt(entityId)
                    writeUUID(uuid)
                    // 1.13 以下版本使用 Bukkit 的实体 ID
                    if (major != 5) {
                        writeByte(PacketHandler.entityOperatorHandler.createEntityType(entityType).toString().toByte())
                    } else {
                        // 1.13 使用 NMS 的实体 ID, 同时 1.13 版本的 IRegistry.ENTITY_TYPE 无法与 1.14, 1.15, 1.16 版本兼容
                        // 1.13 -> interface IRegistry<T>
                        writeByte(NMS13IRegistry.ENTITY_TYPE.a(PacketHandler.entityOperatorHandler.createEntityType(entityType) as NMS13EntityTypes<*>).toByte())
                    }
                    writeDouble(location.x)
                    writeDouble(location.y)
                    writeDouble(location.z)
                    writeByte(pitch)
                    writeByte(yaw)
                    writeInt(data)
                    writeShort(0)
                    writeShort(0)
                    writeShort(0)
                }.build() as NMS9PacketDataSerializer)
            }
            // 1.14, 1.15, 1.16
            6, 7, 8 -> NMS16PacketPlayOutSpawnEntity().also {
                it.a(dataSerializerBuilder {
                    writeVarInt(entityId)
                    writeUUID(uuid)
                    // 1.14, 1.15, 1.16 -> abstract class IRegistry<T> -> IRegistry 类型发生变化
                    writeVarInt(NMS16IRegistry.ENTITY_TYPE.a(PacketHandler.entityOperatorHandler.createEntityType(entityType) as NMS16EntityTypes<*>))
                    writeDouble(location.x)
                    writeDouble(location.y)
                    writeDouble(location.z)
                    writeByte(pitch)
                    writeByte(yaw)
                    writeInt(data)
                    writeShort(0)
                    writeShort(0)
                    writeShort(0)
                }.build() as NMS16PacketDataSerializer)
            }
            // 1.17, 1.18, 1.19, 1.20
            9, 10, 11, 12 -> NMSPacketPlayOutSpawnEntity(dataSerializerBuilder {
                writeVarInt(entityId)
                writeUUID(uuid)
                when (major) {
                    // 1.17, 1.18 写法相同
                    9, 10 -> {
                        val id = PacketHandler.entityOperatorHandler.createEntityType(entityType) as NMSEntityTypes<*>
                        writeVarInt(NMSIRegistry.ENTITY_TYPE.getId(id))
                    }
                    // 1.19 写法不同
                    11 -> {
                        when (minor) {
                            // 1.19, 1.19.1, 1.19.2 -> this.type = (EntityTypes)var0.readById(IRegistry.ENTITY_TYPE);
                            0, 1, 2 -> writeVarInt(NMSIRegistry.ENTITY_TYPE.getId(PacketHandler.entityOperatorHandler.createEntityType(entityType) as NMSEntityTypes<*>))
                            // 1.19.3               -> this.type = (EntityTypes)var0.readById(BuiltInRegistries.ENTITY_TYPE);
                            // 注意从该版本开始 RegistryBlocks 的类型发生变化，无法在同一个模块内向下兼容
                            3 -> error("未支持的服务端版本")
                        }
                    }
                    // 1.20
                    /*
                    12 -> writeVarInt(
                        NMS120.INSTANCE.entityTypeGetId(
                        Packet.nmsSundryHandler.adaptNMSEntityType(
                            entityType
                        ) as NMSEntityTypes<*>))

                     */
                }
                writeDouble(location.x)
                writeDouble(location.y)
                writeDouble(location.z)
                // xRot     -> pitch -> 纵向视角
                writeByte(pitch)
                // yRot     -> yaw -> 普通实体没效果
                writeByte(yaw)
                // yHeadRot -> yaw -> 横向视角
                // 1.19 才有这个
                if (major == 11) {
                    writeByte(yaw)
                    writeVarInt(data)
                } else {
                    writeInt(data)
                }
                writeShort(0)
                writeShort(0)
                writeShort(0)
            }.build() as NMSPacketDataSerializer)
            // 不支持
            else -> error("不支持的版本")
        }
        // 发送数据包
        player.sendAsyncPacket(packet)
    }

    override fun spawnEntityLiving(player: Player, entityType: EntityType, entityId: Int, uuid: UUID, location: Location) {
        // 1.13 以下版本盔甲架子不是 EntityLiving 类型，1.19 以上版本所有实体使用 PacketPlayOutSpawnEntity 数据包生成
        if ((entityType == EntityType.ARMOR_STAND && majorLegacy < 11300) || majorLegacy >= 11900) {
            return spawnEntity(player, entityType, entityId, uuid, location)
        }
        // 计算视角
        val yaw = (location.yaw * 256.0f / 360.0f).toInt().toByte()
        val pitch = (location.pitch * 256.0f / 360.0f).toInt().toByte()
        // 版本判断
        val packet: Any = when (major) {
            // 1.11, 1.12, 1.13
            3, 4, 5 -> NMS11PacketPlayOutSpawnEntityLiving().also {
                it.a(dataSerializerBuilder {
                    writeVarInt(entityId)
                    writeUUID(uuid)
                    // 1.13 以下版本使用 Bukkit 的实体 ID
                    if (major != 5) {
                       // TODO()
                        writeVarInt(PacketHandler.entityOperatorHandler.createEntityType(entityType).toString().toInt())
                    } else {
                        // 1.13 使用 NMS 的实体 ID, 同时 1.13 版本的 IRegistry.ENTITY_TYPE 无法与 1.14, 1.15, 1.16 版本兼容
                        // 1.13 -> interface IRegistry<T> -> 从 Bukkit 实体 ID 转变为 NMS 实体 ID
                        writeVarInt(NMS13IRegistry.ENTITY_TYPE.a(PacketHandler.entityOperatorHandler.createEntityType(entityType) as NMS13EntityTypes<*>))
                    }
                    writeDouble(location.x)
                    writeDouble(location.y)
                    writeDouble(location.z)
                    writeByte(yaw)
                    writeByte(pitch)
                    writeByte(yaw)
                    writeShort(0)
                    writeShort(0)
                    writeShort(0)
                  //  NMS11DataWatcher(null).also { dw -> livingDataWatcherSetterM.bindTo(it).invokeWithArguments(dw) }.a(toNMS() as NMS11PacketDataSerializer)
                }.build() as NMS11PacketDataSerializer)
            }
            // 1.14, 1.15, 1.16
            6, 7, 8 -> NMS16PacketPlayOutSpawnEntityLiving().also {
                it.a(dataSerializerBuilder {
                    writeVarInt(entityId)
                    writeUUID(uuid)
                    // 1.14, 1.15, 1.16 -> abstract class IRegistry<T> -> IRegistry 类型发生变化
                    writeVarInt(NMS16IRegistry.ENTITY_TYPE.a(PacketHandler.entityOperatorHandler.createEntityType(entityType) as NMS16EntityTypes<*>))
                    writeDouble(location.x)
                    writeDouble(location.y)
                    writeDouble(location.z)
                    writeByte(yaw)
                    writeByte(pitch)
                    writeByte(yaw)
                    writeShort(0)
                    writeShort(0)
                    writeShort(0)
                }.build() as NMS16PacketDataSerializer)
            }
            // 1.17, 1.18
            // 使用带有 DataSerializer 的构造函数生成数据包
            9, 10 -> NMSPacketPlayOutSpawnEntityLiving(dataSerializerBuilder {
                writeVarInt(entityId)
                writeUUID(uuid)
                writeVarInt(NMSIRegistry.ENTITY_TYPE.getId(PacketHandler.entityOperatorHandler.createEntityType(entityType) as NMSEntityTypes<*>))
                writeDouble(location.x)
                writeDouble(location.y)
                writeDouble(location.z)
                // yRot -> yaw
                writeByte(yaw)
                // xRot -> pitch
                writeByte(pitch)
                // yHeadRot -> yaw
                writeByte(yaw)
                writeShort(0)
                writeShort(0)
                writeShort(0)
            }.build() as NMSPacketDataSerializer)
            // 不支持
            else -> error("不支持的版本")
        }
        // 发送数据包
        player.sendAsyncPacket(packet)
    }

}