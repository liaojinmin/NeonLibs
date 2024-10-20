package me.neon.libs.taboolib.ui.type

import me.neon.libs.taboolib.ui.ClickEvent
import me.neon.libs.taboolib.ui.ItemStacker
import me.neon.libs.util.item.isNotAir
import org.bukkit.inventory.ItemStack

class ActionQuickTake : Action() {

    override fun getCursor(e: ClickEvent): ItemStack {
        return e.clicker.itemOnCursor
    }

    override fun setCursor(e: ClickEvent, item: ItemStack?) {
        if (item.isNotAir()) {
            ItemStacker.MINECRAFT.moveItemFromChest(item, e.clicker)
        }
        e.clicker.setItemOnCursor(null)
    }

    override fun getCurrentSlot(e: ClickEvent): Int {
        return e.rawSlot
    }
}