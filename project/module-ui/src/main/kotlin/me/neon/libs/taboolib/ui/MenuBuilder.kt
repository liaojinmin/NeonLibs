package me.neon.libs.taboolib.ui

import me.neon.libs.event.InternalEventBus
import me.neon.libs.taboolib.nms.MinecraftVersion
import me.neon.libs.taboolib.nms.PacketSendEvent
import me.neon.libs.taboolib.ui.type.impl.ChestImpl
import me.neon.libs.taboolib.ui.virtual.*
import me.neon.libs.util.item.isNotAir
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * 允许在 Vanilla Inventory 中使用 Raw Title
 */
var isRawTitleInVanillaInventoryEnabled = false
    private set

/**
 * 允许在 Vanilla Inventory 中使用 Raw Title
 *
 * 为什么需要主动启用?
 * 1. 一旦注册 [PacketSendEvent] 事件，就需要注入玩家的 Channel 来启用数据包系统。
 * 2. 对于没有这类需求的用户来说，安装 module-ui 就意味着被迫启用数据包系统，造成不必要的性能损耗。
 *
 * 虚拟菜单不需要开启该选项
 */
fun enableRawTitleInVanillaInventory() {
    // 防止重复注册
    if (isRawTitleInVanillaInventoryEnabled) {
        return
    }
    isRawTitleInVanillaInventoryEnabled = true
    // 监听数据包
    InternalEventBus.listen<PacketSendEvent> { e ->
        if (e.packet.name == "PacketPlayOutOpenWindow" || e.packet.name == "ClientboundOpenScreenPacket") {
            // 1.20.5 -> d, 不再是 c
            val field = if (MinecraftVersion.isUniversal) "title" else "c"
            val plain = InventoryHandlers.instance.craftChatMessageToPlain(e.packet.read(field)!!)
            if (plain.startsWith('{') && plain.endsWith('}')) {
                e.packet.write(field, InventoryHandlers.instance.parseToCraftChatMessage(plain))
            }
        }
    }
}

/**
 * 构建一个菜单
 */
inline fun <reified T : Menu> buildMenu(title: String = "chest", builder: T.() -> Unit): Inventory {
    val type = if (T::class.java.isInterface) Menu.getImplementation(T::class.java) else T::class.java
    val instance = type.getDeclaredConstructor(String::class.java).newInstance(title) as T
    return instance.also(builder).build()
}

/**
 * 构建一个菜单并为玩家打开
 */
inline fun <reified T : Menu> HumanEntity.openMenu(title: String = "chest", builder: T.() -> Unit) {
    try {
        openMenu(buildMenu(title, builder))
    } catch (ex: Throwable) {
        ex.printStackTrace()
    }
}

/**
 * 打开一个构建后的菜单
 */
fun HumanEntity.openMenu(buildMenu: Inventory, changeId: Boolean = true) {
    try {
        if (buildMenu is VirtualInventory) {
            val remoteInventory = openVirtualInventory(buildMenu, changeId)
            val basic = MenuHolder.fromInventory(buildMenu)
            if (basic is ChestImpl) {
                remoteInventory.inject(basic)
            }
        } else {
            openInventory(buildMenu)
        }
    } catch (ex: Throwable) {
        ex.printStackTrace()
    }
}

/**
 * 获取当前点击事件下所有受影响的物品
 */
fun InventoryClickEvent.getAffectItems(): List<ItemStack> {
    val items = ArrayList<ItemStack>()
    if (click == ClickType.NUMBER_KEY) {
        val hotbarButton = whoClicked.inventory.getItem(hotbarButton)
        if (hotbarButton.isNotAir()) {
            items += hotbarButton
        }
    }
    if (currentItem.isNotAir()) {
        items += currentItem!!
    }
    return items
}