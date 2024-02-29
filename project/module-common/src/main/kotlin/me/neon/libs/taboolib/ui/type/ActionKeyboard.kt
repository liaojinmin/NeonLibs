package me.neon.libs.taboolib.ui.type

import org.bukkit.inventory.ItemStack
import me.neon.libs.taboolib.ui.ClickEvent

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