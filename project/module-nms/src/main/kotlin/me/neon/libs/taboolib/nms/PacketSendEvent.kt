package me.neon.libs.taboolib.nms

import io.netty.channel.Channel
import org.bukkit.entity.Player
import me.neon.libs.event.CancelableInternalEvent

/**
 * TabooLib
 * taboolib.module.nms.PacketSendEvent
 *
 * @author sky
 * @since 2021/6/24 5:38 下午
 */
class PacketSendEvent(val player: Player, val packet: Packet) : CancelableInternalEvent() {

    /**
     * for early login/status packets
     */
    class Handshake(val channel: Channel, val packet: Packet) : CancelableInternalEvent()
}