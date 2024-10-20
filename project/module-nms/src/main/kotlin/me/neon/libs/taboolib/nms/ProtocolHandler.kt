package me.neon.libs.taboolib.nms

import me.neon.libs.core.LifeCycle
import me.neon.libs.core.inject.Awake
import org.bukkit.entity.Player

/**
 * @author 坏黑
 * @since 2018-10-28 14:34
 */
object ProtocolHandler {

    /** TinyProtocol 实例 */
    var instance: TinyProtocol? = null
        private set

    // 数据包监听器
    const val PACKET_LISTENER = "packet_listener/v1"

    // 数据包监听器注销
    const val PACKET_LISTENER_EJECT = "packet_listener_eject/v1"

    // 数据包接收
    const val PACKET_RECEIVE = "packet_receive/v1"

    // 数据包发送
    const val PACKET_SEND = "packet_send/v1"


    private var newPacketBundlePacket: TinyReflection.ConstructorInvoker? = null

    init {
        try {
            val bundlePacketClass =
                TinyReflection.getClass("net.minecraft.network.protocol.game.ClientboundBundlePacket")
            newPacketBundlePacket = TinyReflection.getConstructor(bundlePacketClass, Iterable::class.java)

        } catch (ignored: Exception) {
        }
    }

    /**
     * 当前插件是否已经注入数据包监听器
     */
    fun isInjected(): Boolean {
        return instance != null
    }

    @Awake(LifeCycle.ENABLE)
    fun init() {
        instance?.close()
        instance = TinyProtocol()
    }

    @Awake(LifeCycle.DISABLE)
    fun close() {
        instance?.close()
    }

    /**
     * 创建混合包（我也不知道这东西应该翻译成什么）
     */
    fun createBundlePacket(packets: List<Any>): Any? {
        return newPacketBundlePacket?.invoke(packets)
    }

    /**
     * 发送数据包
     * @param player 玩家
     * @param packet 数据包实例
     */
    fun sendPacket(player: Player, packet: Any) {
        instance?.sendPacket(player, packet)
    }

}