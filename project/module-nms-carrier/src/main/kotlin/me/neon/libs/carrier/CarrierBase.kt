package me.neon.libs.carrier

import me.neon.libs.carrier.minecraft.meta.ArmorStandMeta
import me.neon.libs.carrier.minecraft.meta.CarrierMeta
import me.neon.libs.taboolib.chat.HexColor.colored
import me.neon.libs.util.BoundingBox
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.io.Closeable
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function

/**
 * NeonDesire
 * me.neon.desire.nms.carrier
 *
 * @author 老廖
 * @since 2024/4/27 9:57
 */
abstract class CarrierBase: MetaDatable<UUID> {

    abstract var displayName: String

    abstract var displayNameVisible: Boolean

    abstract val location: Location

    abstract val eyeLocation: Location

    abstract val boundingBox: BoundingBox

    /**
     * 可见玩家
     */
    private val viewPlayer: MutableSet<String> = mutableSetOf()

    /**
     * 载体的全息
     */
    private val holo: MutableMap<Int, String> = mutableMapOf()

    private val spawnFunction: MutableSet<Consumer<Player>> = mutableSetOf()

    private val destroyFunction: MutableSet<Consumer<Player>> = mutableSetOf()

    /**
     * 临时数据
     */
    private val metadataList: ConcurrentHashMap<UUID, MutableMap<String, MetaDataValue<*>>> by lazy { ConcurrentHashMap() }

    /**
     * 携带实体
     */
    private val entityMap: MutableMap<Int, CarrierEntity> = mutableMapOf()

    /**
     * 同步锁
     */
    private val synLock: AtomicBoolean = AtomicBoolean(false)

    private var parser: BiFunction<Player, String, String> = BiFunction { _, v -> v }

    private var offset: Double = 0.0

    val entityId: Int by lazy { PacketHandler.nextIndex() }

    val uniqueId: UUID by lazy { UUID.randomUUID() }

    var visibleDistance: Double = 32.0

    var carrierMeta: CarrierMeta? = null
        set(value) {
            // 如果本身不为null,并且新设置的值也不为null,则主动更新meta
            if (field != null && value != null) {
                PacketHandler.entityOperatorHandler.sendEntityMetadata(getVisiblePlayers(), entityId, value)
            }
            field = value
        }

    var isDead: Boolean = false
        set(value) {
            lock()
            field = value
            //println("set isDead $field")
            if (!field) {
                PacketHandler.addCarrier(this)
            } else {
                // 此时已无容器缓存
                PacketHandler.delCarrier(this)
                getVisiblePlayers().forEach {
                    destroy(it)
                }
                viewPlayer.clear()
            }
            unlock()
        }

    open fun interact(player: Player, action: CarrierAction, isMainHand: Boolean) {}

    fun teleport(loc: Location): CarrierBase {
        this.location.x = loc.x
        this.location.y = loc.y
        this.location.z = loc.z
        this.location.yaw = loc.yaw
        this.location.pitch = loc.pitch
        if (!isLock() && !isDead) {
            PacketHandler.entityOperatorHandler.teleportEntity(getVisiblePlayers(), entityId, location)
        }
        return this
    }

    fun teleport(entity: Entity): CarrierBase {
        return teleport(entity.location)
    }

    fun updateCustomName(name: String = displayName) {
        if (name != displayName) {
            displayName = name
        }
        getVisiblePlayers().forEach {
            PacketHandler.entityOperatorHandler.sendCustomName(it, entityId, displayName, displayNameVisible)
        }
    }

    fun initHologram(list: List<String>, offset: Double, parse: BiFunction<Player, String, String>) {
        this.parser = parse
        this.offset = offset
        if (list.isEmpty()) return
        holo.clear()
        list.forEach {
            holo[PacketHandler.nextIndex()] = it.colored()
        }
    }

    fun registerSpawn(consumer: Consumer<Player>) {
        spawnFunction.add(consumer)
    }

    fun registerDestroy(consumer: Consumer<Player>) {
        destroyFunction.add(consumer)
    }

    fun registerEntity(carrierEntity: CarrierEntity): CarrierBase {
        entityMap[carrierEntity.entityId] = carrierEntity
        return this
    }

    fun unregisterEntity(carrierEntity: CarrierEntity): CarrierBase {
        entityMap.remove(carrierEntity.entityId)?.close()
        return this
    }

    fun getAllRegisterEntity(): Collection<CarrierEntity> {
        return entityMap.values
    }

    fun viewContains(player: Player): Boolean {
        return viewPlayer.contains(player.name)
    }

    fun getVisiblePlayers(): List<Player> {
        val list = mutableListOf<Player>()
        val iterator = viewPlayer.iterator()
        while (iterator.hasNext()) {
            val player = Bukkit.getPlayerExact(iterator.next())
            if (player != null) {
                list.add(player)
            }
        }
        return list
    }

    fun setVisibleLock(player: Player) {
        setMetadata(player.uniqueId, "visibleByDistance", MetaDataString("false"))
    }

    fun setVisibleUnLock(player: Player) {
        setMetadata(player.uniqueId, "visibleByDistance", MetaDataString("true"))
    }

    fun lock() = synLock.set(true)

    fun isLock(): Boolean = synLock.get()

    fun unlock() = synLock.set(false)

    override fun setMetadata(uuid: UUID, key: String, value: MetaDataValue<*>) {
        metadataList.computeIfAbsent(uuid) { mutableMapOf() }[key] = value
    }

    override fun getMetadata(uuid: UUID, key: String): MetaDataValue<*>? {
        val map = metadataList.computeIfAbsent(uuid) { mutableMapOf() }
        val data = map[key]
        if (data != null && data.isTimerOut()) {
            map.remove(key)
            return null
        }
        return data
    }

    override fun getAllMetadata(uuid: UUID): Map<String, MetaDataValue<*>> {
        val map = metadataList.computeIfAbsent(uuid) { mutableMapOf() }
        val iterator = map.entries.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().value.isTimerOut()) {
                iterator.remove()
            }
        }
        return map
    }

    override fun hasMetadata(uuid: UUID, key: String): Boolean {
        return getMetadata(uuid, key) != null
    }

    override fun removeMetadata(uuid: UUID, key: String) {
        metadataList[uuid]?.remove(key)
    }

    private fun spawn(player: Player) {
        if (viewPlayer.add(player.name)) {
            spawnArmorStand(player, location, displayName)
            spawnHologram(player)
            entityMap.values.forEach{ it.spawn(player) }
            spawnFunction.forEach {
                it.accept(player)
            }
        }
    }

    internal fun destroy(player: Player) {
        if (viewPlayer.remove(player.name)) {
            PacketHandler.entityOperatorHandler.destroyEntity(player, entityId)
            destroyHologram(player)
            entityMap.values.forEach{ it.destroy(player) }
            destroyFunction.forEach {
                it.accept(player)
            }
        }
    }

    internal fun refreshVisible(player: Player) {
        if (isLock()) return
        if (visibleByDistance(player)) {
            refreshHologram(player)
            spawn(player)
        } else {
            destroy(player)
        }
    }

    private fun spawnArmorStand(target: Player, location: Location, name: String) {
        PacketHandler.entitySpawnHandler.spawnEntityLiving(target, EntityType.ARMOR_STAND, entityId, uniqueId, location)
        if (carrierMeta != null) {
            PacketHandler.entityOperatorHandler.sendEntityMetadata(target, entityId, carrierMeta!!)
        }
        if (name.isNotEmpty()) {
            PacketHandler.entityOperatorHandler.sendCustomName(target, entityId, name, displayNameVisible)
        }
    }

    private fun visibleByDistance(player: Player): Boolean {
        // 如果有这个数据，并且是 false 则不为该玩家解析
        val data = getMetadata(player.uniqueId, "visibleByDistance")
        if (data != null && !data.asBoolean(true)) {
            return false
        }

        if (player.isDead) {
            return false
        }

        if (player.world.name != location.world?.name) {
            return false
        }

        return location.distance(player.location) <= visibleDistance
    }

    private fun spawnHologram(player: Player) {
        var lines = offset
        for (map in holo) {
            PacketHandler.entitySpawnHandler.spawnEntityLiving(
                player,
                EntityType.ARMOR_STAND,
                map.key,
                UUID.randomUUID(),
                eyeLocation.clone().add(0.0, lines, 0.0)
            )
            PacketHandler.entityOperatorHandler.sendEntityMetadata(
                player,
                map.key,
                ArmorStandMeta(isInvisible = true, isGlowing = false, isSmall = false, hasArms = false, noBasePlate = true, isMarker = true)
            )
            PacketHandler.entityOperatorHandler.sendCustomName(player, map.key, parser.apply(player, map.value), true)
            lines += 0.25
        }
    }

    private fun destroyHologram(player: Player) {
        for (map in holo) {
            PacketHandler.entityOperatorHandler.destroyEntity(player, map.key)
        }
    }

    private fun refreshHologram(player: Player) {
        for (map in holo) {
            PacketHandler.entityOperatorHandler.sendCustomName(player, map.key, parser.apply(player, map.value), true)
        }
    }
}