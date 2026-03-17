package me.neon.libs.taboolib.ui.type

import me.neon.libs.taboolib.ui.type.impl.ChestImpl

/**
 * 向下兼容
 */
@Deprecated("Use Chest instead.", ReplaceWith("Chest"))
open class Basic(override var title: String) : ChestImpl(title)