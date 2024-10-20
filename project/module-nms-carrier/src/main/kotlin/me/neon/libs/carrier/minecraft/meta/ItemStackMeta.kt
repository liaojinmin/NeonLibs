package me.neon.libs.carrier.minecraft.meta


import me.neon.libs.carrier.PacketHandler
import org.bukkit.inventory.ItemStack


data class ItemStackMeta(
    var glow: Boolean,
    var item: ItemStack,
): CarrierMeta {

    override fun adapt(): Array<Any> {
        var entity = 0
        if (glow) entity += 0x40.toByte()
        return arrayOf(
            PacketHandler.entityOperatorHandler.createByteMeta(0, entity.toByte()),
            PacketHandler.entityOperatorHandler.createBooleanMeta(5, true),
            PacketHandler.entityOperatorHandler.createItemStackMeta(PacketHandler.itemStackIndex, item)
        )
    }

}