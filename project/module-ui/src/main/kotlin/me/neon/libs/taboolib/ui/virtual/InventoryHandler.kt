package me.neon.libs.taboolib.ui.virtual


import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack


/**
 * TabooLib
 * taboolib.module.ui.virtual.InventoryHandler
 *
 * @author 坏黑
 * @since 2023/1/15 21:27
 */
abstract class InventoryHandler {

    abstract fun craftChatMessageToPlain(message: Any): String

    abstract fun parseToCraftChatMessage(source: String): Any

    abstract fun openInventory(player: Player, inventory: VirtualInventory, cursorItem: ItemStack = player.itemOnCursor, updateId: Boolean = true): RemoteInventory

}