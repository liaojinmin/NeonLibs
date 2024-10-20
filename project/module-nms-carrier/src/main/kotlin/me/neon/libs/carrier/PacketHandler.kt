package me.neon.libs.carrier

import me.neon.libs.carrier.minecraft.EntityOperatorHandler
import me.neon.libs.carrier.minecraft.EntitySpawnHandler
import me.neon.libs.NeonLibsLoader
import me.neon.libs.core.LifeCycle
import me.neon.libs.core.inject.Awake
import me.neon.libs.event.EventPriority
import me.neon.libs.event.SubscribeEvent
import me.neon.libs.taboolib.nms.MinecraftVersion
import me.neon.libs.taboolib.nms.PacketReceiveEvent
import me.neon.libs.taboolib.nms.nmsProxy
import me.neon.libs.util.random
import me.neon.libs.util.syncRunner
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitTask
import org.tabooproject.reflex.Reflex.Companion.getProperty
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * NeonDesire
 * me.neon.desire.nms
 *
 * @author 老廖
 * @since 2024/4/27 8:48
 */
object PacketHandler {

    private val group = "${PacketHandler::class.java.`package`.name}.minecraft"

    /**
     * max 2147483647
     */
    private var index = 9299959 + random(0, 702)

    /**
     * 盔甲架meta索引号
     */
    internal val armorStandIndex by lazy { arrayOf(11700 to 15, 11500 to 14, 11400 to 13, 11000 to 11, 10900 to 10).firstOrNull {
        MinecraftVersion.majorLegacy >= it.first }?.second ?: -1
    }

    /**
     * 物品生物meta索引号
     */
    internal val itemStackIndex by lazy { arrayOf(11700 to 8, 11300 to 7, 11000 to 6, 10900 to 5).firstOrNull {
        MinecraftVersion.majorLegacy >= it.first }?.second ?: -1
    }

    internal fun nextIndex(): Int {
        return index++
    }

    /** 生物设置选项接口 **/
    lateinit var entityOperatorHandler: EntityOperatorHandler
        private set

    /** 单位生成接口 **/
    lateinit var entitySpawnHandler: EntitySpawnHandler
        private set

    /** 自管理的在线玩家 **/
    private val onlinePlayers: CopyOnWriteArrayList<Player> = CopyOnWriteArrayList()

    /**
     * 活跃载体
     */
    private val carrierCache: ConcurrentHashMap<Int, CarrierBase> = ConcurrentHashMap()
    private val entityCache: ConcurrentHashMap<Int, CarrierEntity> = ConcurrentHashMap()

    private var bukkitTask: BukkitTask? = null

    fun getCarrier(id: Int): CarrierBase? {
        return carrierCache[id]
    }

    fun getEntity(id: Int): CarrierEntity? {
        return entityCache[id]
    }

    fun addCarrier(carrierBase: CarrierBase) {
        carrierCache[carrierBase.entityId] = carrierBase
        carrierBase.getAllRegisterEntity().forEach {
            entityCache[it.entityId] = it
        }
    }

    fun delCarrier(id: Int) {
        carrierCache.remove(id)?.let {
            it.getAllRegisterEntity().forEach { entity ->
                entityCache.remove(entity.entityId)
            }
        }
    }

    fun delCarrier(carrierBase: CarrierBase) {
        return delCarrier(carrierBase.entityId)
    }


    @Awake(LifeCycle.DISABLE)
    private fun close() {
        bukkitTask?.cancel()
        onlinePlayers.clear()
    }

    @Awake(LifeCycle.ENABLE)
    private fun start() {
        entityOperatorHandler = nmsProxy(NeonLibsLoader.getInstance(),"$group.EntityOperatorHandlerImpl")
        entitySpawnHandler = nmsProxy(NeonLibsLoader.getInstance(),"$group.EntitySpawnHandlerImpl")
        onlinePlayers.addAll(Bukkit.getOnlinePlayers())
        bukkitTask?.cancel()
        bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(NeonLibsLoader.getInstance(), Runnable {
            val a = carrierCache.entries.iterator()
            while (a.hasNext()) {
                val carrier = a.next().value
                val player = onlinePlayers.listIterator()
                while (player.hasNext()) {
                    val it = player.next()
                    if (it.isOnline) {
                        if (!carrier.isLock()) {
                            carrier.refreshVisible(it)
                        }
                    }
                }
            }
        }, 20, 1)
    }

    fun createAsyncTask(func: Runnable): BukkitTask {
        return Bukkit.getScheduler().runTaskAsynchronously(NeonLibsLoader.getInstance(), func)
    }

    fun createAsyncTask(delay: Long, func: Runnable): BukkitTask {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(NeonLibsLoader.getInstance(), func, delay)
    }

    fun createAsyncTask(delay: Long, period: Long, func: Runnable): BukkitTask {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(NeonLibsLoader.getInstance(), func, delay, period)
    }

    fun createTask(func: Runnable): BukkitTask {
        return Bukkit.getScheduler().runTask(NeonLibsLoader.getInstance(), func)
    }

    fun createTask(delay: Long, func: Runnable): BukkitTask {
        return Bukkit.getScheduler().runTaskLater(NeonLibsLoader.getInstance(), func, delay)
    }

    fun createTask(delay: Long, period: Long, func: Runnable): BukkitTask {
        return Bukkit.getScheduler().runTaskTimer(NeonLibsLoader.getInstance(), func, delay, period)
    }

    @SubscribeEvent(priority = EventPriority.MONITOR)
    private fun join(event: PlayerJoinEvent) {
        onlinePlayers.add(event.player)
    }

    @SubscribeEvent(priority = EventPriority.MONITOR)
    private fun quit(event: PlayerQuitEvent) {
        onlinePlayers.remove(event.player)
        carrierCache.values.forEach { it.destroy(event.player) }
    }

    private fun callAction(e: PacketReceiveEvent): Pair<CarrierAction, Boolean> {
        return if (MinecraftVersion.isUniversal) {
            val action = e.packet.source.getProperty<Any>("b", remap = false)!!
            val name = action.javaClass.name
            when {
                name.endsWith("PacketPlayInUseEntity\$1") -> CarrierAction.LEFT_CLICK to true
                name.endsWith("PacketPlayInUseEntity\$e") -> CarrierAction.RIGHT_CLICK to (action.getProperty<Any>("a", remap = false).toString() == "MAIN_HAND")
                else -> CarrierAction.UNKNOWN to false
            }
        } else {
            // 低版本 EnumEntityUseAction 为枚举类型
            // 通过字符串判断点击方式
            when (e.packet.source.getProperty<Any>("action")!!.toString()) {
                "ATTACK" -> CarrierAction.LEFT_CLICK to true
                "INTERACT_AT" -> CarrierAction.RIGHT_CLICK to (e.packet.read<Any>("d").toString() == "MAIN_HAND")
                else -> CarrierAction.UNKNOWN to false
            }
        }
    }

    @SubscribeEvent
    private fun onReceive(e: PacketReceiveEvent) {
        if (e.packet.name == "PacketPlayInUseEntity") {
            val entity = getCarrier(e.packet.read<Int>("a")!!)
            if (entity == null) {
                getEntity(e.packet.read<Int>("a")!!)?.let {
                    val action = callAction(e)
                    syncRunner {
                        it.interact(e.player, action.first, action.second)
                    }
                }
                return
            }
            if (entity.viewContains(e.player)) {
                val action = callAction(e)
                syncRunner {
                    entity.interact(e.player, action.first, action.second)
                }
            }
        }
    }


}