

package me.neon.libs.taboolib.chat

import me.neon.libs.NeonLibsLoader
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.ConcurrentHashMap

class ChatCapture: Listener {

    companion object {

        val inputs = ConcurrentHashMap<String, (String) -> Unit>()
    }

    @EventHandler
    fun e(e: PlayerQuitEvent) {
        inputs.remove(e.player.name)
    }

    @EventHandler
    fun e(e: AsyncPlayerChatEvent) {
        e.player.location.yaw
        if (inputs.containsKey(e.player.name)) {
            inputs.remove(e.player.name)?.invoke(e.message)
            e.isCancelled = true
        }
    }
}

    /**
     * 捕获玩家输入的消息
     */
    fun Player.nextChat(function: (message: String) -> Unit) {
        ChatCapture.inputs[name] = function
    }

    /**
     * 捕获玩家输入的消息
     */
    fun Player.nextChat(function: (message: String) -> Unit, reuse: (player: Player) -> Unit = {}) {
        if (ChatCapture.inputs.containsKey(name)) {
            reuse(this)
        } else {
            ChatCapture.inputs[name] = function
        }
    }

    /**
     * 捕获玩家输入的消息（在一定时间内）
     */
    fun Player.nextChatInTick(tick: Long, func: (message: String) -> Unit, timeout: (player: Player) -> Unit = {}, reuse: (player: Player) -> Unit = {}) {
        if (ChatCapture.inputs.containsKey(name)) {
            reuse(this)
        } else {
            ChatCapture.inputs[name] = func

            Bukkit.getScheduler().runTaskLater(NeonLibsLoader.instance, Runnable {
                if (ChatCapture.inputs.containsKey(name)) {
                    timeout(this@nextChatInTick)
                    ChatCapture.inputs.remove(name)
                }
            }, tick)
        }
    }

    fun Player.cancelNextChat(execute: Boolean = true) {
        val listener = ChatCapture.inputs.remove(name)
        if (listener != null && execute) {
            listener("")
        }
    }





