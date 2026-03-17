package me.neon.libs.taboolib.ui.type

import me.neon.libs.taboolib.ui.ClickEvent
import org.bukkit.inventory.ItemStack

abstract class Action {

    abstract fun getCursor(e: ClickEvent): ItemStack?

    abstract fun setCursor(e: ClickEvent, item: ItemStack?)

    abstract fun getCurrentSlot(e: ClickEvent): Int
}