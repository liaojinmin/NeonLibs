package me.neon.libs.taboolib.ui

import me.neon.libs.taboolib.ui.type.*
import me.neon.libs.taboolib.ui.type.impl.*
import org.bukkit.inventory.Inventory
import java.util.concurrent.ConcurrentHashMap

interface Menu {

    /** 标题 */
    var title: String

    /** 构建菜单 */
    fun build(): Inventory

    companion object {

        private val impl = ConcurrentHashMap<Class<*>, Class<*>>()

        init {
            impl[Anvil::class.java] = AnvilImpl::class.java
            impl[Chest::class.java] = ChestImpl::class.java
            impl[Hopper::class.java] = HopperImpl::class.java
            impl[PageableChest::class.java] = PageableChestImpl::class.java
            impl[StorableChest::class.java] = StorableChestImpl::class.java
        }

        /** 注册实现 */
        fun registerImplementation(clazz: Class<*>, implementation: Class<*>) {
            impl[clazz] = implementation
        }

        /** 获取实现 */
        fun getImplementation(clazz: Class<*>): Class<*> {
            return impl[clazz] ?: error("Cannot find implementation for ${clazz.name}")
        }
    }
}