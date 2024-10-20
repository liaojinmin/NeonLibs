package me.neon.libs.carrier.minecraft.meta

import me.neon.libs.carrier.PacketHandler


data class ArmorStandMeta(
    /** 是否是看不见的 **/
    var isInvisible: Boolean,
    var isGlowing: Boolean,
    var isSmall: Boolean,
    var hasArms: Boolean,
    var noBasePlate: Boolean,
    var isMarker: Boolean
): CarrierMeta {

    override fun adapt(): Array<Any> {
        var entity = 0
        var armorstand = 0
        if (isInvisible) entity += 0x20.toByte()
        if (isGlowing) entity += 0x40.toByte()
        if (isSmall) armorstand += 0x01.toByte()
        if (hasArms) armorstand += 0x04.toByte()
        if (noBasePlate) armorstand += 0x08.toByte()
        if (isMarker) armorstand += 0x10.toByte()
        return arrayOf(
            PacketHandler.entityOperatorHandler.createByteMeta(0, entity.toByte()),
            PacketHandler.entityOperatorHandler.createByteMeta(PacketHandler.armorStandIndex, armorstand.toByte()))
    }

}