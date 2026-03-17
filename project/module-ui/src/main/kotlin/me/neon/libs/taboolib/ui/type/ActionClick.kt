package me.neon.libs.taboolib.ui.type

import me.neon.libs.taboolib.ui.ClickEvent
import org.bukkit.inventory.ItemStack

class ActionClick : Action() {

    override fun getCursor(e: ClickEvent): ItemStack {
        return e.clicker.itemOnCursor
    }

    override fun setCursor(e: ClickEvent, item: ItemStack?) {
        e.clicker.setItemOnCursor(item)
    }

    override fun getCurrentSlot(e: ClickEvent): Int {
        return e.rawSlot
    }
}