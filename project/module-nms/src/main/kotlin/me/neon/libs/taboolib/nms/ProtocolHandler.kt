package me.neon.libs.taboolib.nms

import io.netty.channel.Channel
import me.neon.libs.NeonLibsLoader
import me.neon.libs.core.LifeCycle
import me.neon.libs.core.inject.Awake
import me.neon.libs.event.SubscribeEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.tabooproject.reflex.ClassMethod
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.ReflexClass
import org.tabooproject.reflex.util.ClassHelper
import java.lang.reflect.Constructor
import java.util.concurrent.ConcurrentHashMap

/**
 * @author 坏黑
 * @since 2018-10-28 14:34
 */
object ProtocolHandler {

    /**
     * 使用 LightInjector 作为 TabooLib 数据包实现。
     * 不再对外开放，因为随着版本更新，底层实现可能会变更，在这之前曾使用 TinyProtocol。
     *
     * 这类工具现均已停止维护，可能因服务端改动频繁维护成本极高。
     * 未来可能会选择使用 retrooper/packetevents，但它是个巨无霸。
     */
    var instance: LightInjector? = null
        private set

    // 数据包监听器
    const val PACKET_LISTENER = "packet_listener/v1"

    // 数据包监听器注销
    const val PACKET_LISTENER_EJECT = "packet_listener_eject/v1"

    // 数据包接收
    const val PACKET_RECEIVE = "packet_receive/v1"

    // 数据包发送
    const val PACKET_SEND = "packet_send/v1"


    private val playerConnectionMap = ConcurrentHashMap<String, Any>()
    private var sendPacketMethod: ClassMethod? = null
    private var newPacketBundlePacket: Constructor<*>? = null

    init {
        try {
            val bundlePacketClass = ClassHelper.getClass("net.minecraft.network.protocol.game.ClientboundBundlePacket")
            newPacketBundlePacket = bundlePacketClass.getDeclaredConstructor(Iterable::class.java)
            newPacketBundlePacket?.isAccessible = true
        } catch (ignored: Exception) {
        }
    }

    /**
     * 当前插件是否已经注入数据包监听器
     */
    fun isInjected(): Boolean {
        return instance != null
    }

    @Awake(LifeCycle.NONE)
    fun none() {
        println("none LightReflection init")
        LightReflection.init()
    }
    @Awake(LifeCycle.ENABLE)
    fun init() {
        instance?.close()
        instance = LightInjectorImpl(NeonLibsLoader.getInstance())
        Exchanges[PACKET_LISTENER] = "NeonLibs"
    }

    @Awake(LifeCycle.DISABLE)
    fun close() {
        instance?.close()
    }

    /**
     * 创建混合包（我也不知道这东西应该翻译成什么）
     */
    fun createBundlePacket(packets: List<Any>): Any? {
        return newPacketBundlePacket?.newInstance(packets)
    }

    /**
     * 发送数据包
     * @param player 玩家
     * @param packet 数据包实例
     */
    fun sendPacket(player: Player, packet: Any) {
        // 使用原版方法发送数据包
        // 之前通过 TinyProtocol 的 channel.pipeline().writeAndFlush() 暴力发包会有概率出问题
        val connection = getConnection(player)
        if (sendPacketMethod == null) {
            val reflexClass = ReflexClass.of(connection.javaClass)
            // 1.18 更名为 send 方法
            sendPacketMethod = if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_18)) {
                try {
                    reflexClass.getMethod("send", true, true, packet)
                } catch (_: NoSuchMethodException) {
                    reflexClass.getMethod("sendPacket", true, true, packet)
                }
            } else {
                reflexClass.getMethod("sendPacket", true, true, packet)
            }
        }
        sendPacketMethod!!.invoke(connection, packet)
    }

    fun getChannel(player: Player): Channel? {
        return instance?.getChannel(player)
    }

    /**
     * 获取玩家的连接实例，如果不存在则会抛出 [NullPointerException]
     */
    fun getConnection(player: Player): Any {
        return if (playerConnectionMap.containsKey(player.name)) {
            playerConnectionMap[player.name]!!
        } else {
            val connection = if (MinecraftVersion.isUniversal) {
                player.getProperty<Any>("entity/connection")!!
            } else {
                player.getProperty<Any>("entity/playerConnection")!!
            }
            playerConnectionMap[player.name] = connection
            connection
        }
    }

    @SubscribeEvent
    private fun onJoin(e: PlayerJoinEvent) {
        playerConnectionMap.remove(e.player.name)
    }

    @SubscribeEvent
    private fun onQuit(e: PlayerQuitEvent) {
        Bukkit.getScheduler().runTaskLater(NeonLibsLoader.getInstance(), Runnable {
            playerConnectionMap.remove(e.player.name)
        }, 20)
    }
}