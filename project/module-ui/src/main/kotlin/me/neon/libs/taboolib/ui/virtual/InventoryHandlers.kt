package me.neon.libs.taboolib.ui.virtual

import me.neon.libs.NeonLibsLoader
import me.neon.libs.core.LifeCycle
import me.neon.libs.core.inject.Awake
import me.neon.libs.event.SubscribeEvent
import me.neon.libs.taboolib.nms.MinecraftVersion
import me.neon.libs.taboolib.nms.PacketReceiveEvent
import me.neon.libs.taboolib.nms.nmsProxy
import me.neon.libs.taboolib.ui.InventoryViewProxy
import me.neon.libs.taboolib.ui.MenuHolder
import me.neon.libs.taboolib.ui.type.AnvilCallback
import me.neon.libs.util.unsafeLazy
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.ConcurrentHashMap

/**
 * NeonLibs
 * me.neon.libs.taboolib.ui.virtual
 *
 * @author 老廖
 * @since 2026/3/8 02:37
 */
object InventoryHandlers {

    val instance by unsafeLazy { nmsProxy<InventoryHandler>(NeonLibsLoader.getInstance()) }

    val playerContainerCounterMap = ConcurrentHashMap<String, Int>()

    val playerRemoteInventoryMap = ConcurrentHashMap<String, RemoteInventory>()

    fun getContainerCounter(player: Player, updateId: Boolean = true): Int {
        val id = playerContainerCounterMap.computeIfAbsent(player.name) { 0 }
        return if (updateId) {
            val newId = id % 100 + 1
            playerContainerCounterMap[player.name] = newId
            newId
        } else id
    }

    @SubscribeEvent
    private fun onQuit(e: PlayerQuitEvent) {
        playerContainerCounterMap.remove(e.player.name)
        playerRemoteInventoryMap.remove(e.player.name)
    }

    @Awake(LifeCycle.DISABLE)
    private fun onDisable() {
        Bukkit.getOnlinePlayers().forEach {
            if (playerRemoteInventoryMap.containsKey(it.name)) {
                playerRemoteInventoryMap[it.name]?.close()
            }
        }
    }

    @SubscribeEvent
    private fun onReceive(e: PacketReceiveEvent) {
        when (e.packet.name) {
            // 关闭窗口
            "PacketPlayInCloseWindow", "ServerboundContainerClosePacket" -> {
                // 如果没有正在开启的页面则不处理
                if (playerRemoteInventoryMap.isEmpty()) {
                    return
                }
                val id = e.packet.read<Int>(if (MinecraftVersion.isUniversal) "containerId" else "id")!!
                val player = e.player
                val remoteInventory = playerRemoteInventoryMap[player.name]
                if (remoteInventory != null && (remoteInventory.id == id || id == 0)) {
                    playerRemoteInventoryMap.remove(player.name)?.close(sendPacket = false)
                    try {
                        player.updateInventory()
                    } catch (ex: NoSuchMethodError) {
                        ex.printStackTrace()
                    }
                }
            }
            // 点击
            "PacketPlayInWindowClick", "ServerboundContainerClickPacket" -> {
                // 如果没有正在开启的页面则不处理
                if (playerRemoteInventoryMap.isEmpty()) {
                    return
                }
                val id = e.packet.read<Int>(if (MinecraftVersion.isUniversal) "containerId" else "a")!!
                val player = e.player
                val remoteInventory = playerRemoteInventoryMap[player.name]
                if (remoteInventory != null && remoteInventory.id == id) {
                    remoteInventory.handleClick(e.packet)
                }
            }
            // 重命名
            "PacketPlayInItemName", "ServerboundRenameItemPacket" -> {
                val text = e.packet.read<String?>(if (MinecraftVersion.isUniversal) "name" else "a") ?: return
                val player = e.player
                // 虚拟容器处理
                val virtualInventory = playerRemoteInventoryMap[player.name]?.inventory
                if (virtualInventory != null) {
                    val builder = MenuHolder.fromInventory(virtualInventory)
                    if (builder is AnvilCallback) {
                        builder.invoke(player, text, virtualInventory)
                    }
                }
                // 普通容器处理
                else {
                    val openInventory = InventoryViewProxy.getTopInventory(player.openInventory)
                    val builder = MenuHolder.fromInventory(openInventory)
                    if (builder is AnvilCallback) {
                        builder.invoke(player, text, openInventory)
                    }
                }
            }
        }
    }

}