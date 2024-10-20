package me.neon.libs.taboolib.ui

import me.neon.libs.taboolib.ui.type.Chest
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

/**
 * @author 坏黑
 * @since 2019-05-21 20:28
 */
@Suppress("LeakingThis")
open class MenuHolder(val menu: Chest) : InventoryHolder {

    private val inventory = Bukkit.createInventory(this, if (menu.rows > 0) menu.rows * 9 else menu.slots.size * 9, menu.title)

    override fun getInventory(): Inventory {
        return inventory
    }

    companion object {

        fun fromInventory(inventory: Inventory): Chest? {
            return (inventory.holder as? MenuHolder)?.menu
        }
    }
}