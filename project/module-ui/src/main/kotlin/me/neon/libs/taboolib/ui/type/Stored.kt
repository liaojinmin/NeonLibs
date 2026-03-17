package me.neon.libs.taboolib.ui.type

import me.neon.libs.taboolib.ui.type.impl.StorableChestImpl


/**
 * 向下兼容
 */
@Deprecated("Use StorableChest instead.", ReplaceWith("StorableChest"))
open class Stored(override var title: String) : StorableChestImpl(title)