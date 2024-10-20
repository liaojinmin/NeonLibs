package me.neon.libs.taboolib.ui.type

import me.neon.libs.taboolib.ui.ClickEvent
import org.bukkit.inventory.ItemStack

class ActionKeyboard : Action() {

    override fun getCursor(e: ClickEvent): ItemStack? {
        return e.clicker.inventory.getItem(e.clickEvent().hotbarButton)
    }

    override fun setCursor(e: ClickEvent, item: ItemStack?) {
        e.clicker.inventory.setItem(e.clickEvent().hotbarButton, item)
    }

    override fun getCurrentSlot(e: ClickEvent): Int {
        return e.rawSlot
    }
}