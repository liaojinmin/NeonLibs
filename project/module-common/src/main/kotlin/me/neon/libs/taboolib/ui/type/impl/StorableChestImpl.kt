package me.neon.libs.taboolib.ui.type.impl

import me.neon.libs.taboolib.ui.ClickEvent
import me.neon.libs.taboolib.ui.ClickType
import me.neon.libs.taboolib.ui.type.*
import me.neon.libs.util.item.isAir
import me.neon.libs.util.item.isNotAir
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

open class StorableChestImpl(title: String) : ChestImpl(title), StorableChest {

    /** 页面规则 **/
    val rule = RuleImpl

    /**
     * 定义页面规则
     */
    override fun rule(rule: StorableChest.Rule.() -> Unit) {
        rule(this.rule)
    }

    /**
     * 点击事件回调
     * 仅在特定位置下触发
     * 相比于 Basic，Stored 的所有点击事件回调均会处理 DRAG 类型
     */
    override fun onClick(bind: Int, callback: (event: ClickEvent) -> Unit) {
        onClick {
            if (it.rawSlot == bind) {
                callback(it)
            }
        }
    }

    /**
     * 点击事件回调
     * 仅在特定位置下触发
     */
    override fun onClick(bind: Char, callback: (event: ClickEvent) -> Unit) {
        onClick {
            if (it.slot == bind) {
                callback(it)
            }
        }
    }

    /**
     * 点击事件回调
     * 可选是否自动锁定点击位置
     */
    override fun onClick(lock: Boolean, callback: (event: ClickEvent) -> Unit) {
        if (lock) {
            clickCallback += {
                it.isCancelled = true
                callback(it)
            }
        } else {
            clickCallback += callback
        }
    }

    override fun build(): Inventory {

        // 生成点击回调
        selfClick {
            // 如果事件被取消则不再继续处理
            if (it.isCancelled) {
                return@selfClick
            }
            // 处理拖拽事件
            if (it.clickType === ClickType.DRAG) {
                // 阻止页面内的拖拽行为
                it.dragEvent().rawSlots.forEach { slot ->
                    if (slot < it.dragEvent().inventory.size) {
                        it.isCancelled = true
                    }
                }
            }
            // 处理点击事件
            else if (it.clickType === ClickType.CLICK) {
                // 阻止无法处理的合并行为
                if (it.clickEvent().action == InventoryAction.COLLECT_TO_CURSOR) {
                    it.isCancelled = true
                    return@selfClick
                }
                val currentItem = it.currentItem
                // 自动装填
                if (it.clickEvent().click.isShiftClick && it.rawSlot >= it.inventory.size && currentItem.isNotAir()) {
                    it.isCancelled = true
                    // 获取有效位置
                    val firstSlot = RuleImpl.firstSlot(it.inventory, currentItem)
                    // 目标位置不存在任何物品
                    // 防止覆盖物品
                    if (firstSlot >= 0 && RuleImpl.readItem(it.inventory, firstSlot).isAir()) {
                        // 设置物品
                        RuleImpl.writeItem(it.inventory, currentItem, firstSlot, it.clickEvent().click)
                        // 移除物品
                        it.currentItem?.type = Material.AIR
                        it.currentItem = null
                    }
                } else if (it.rawSlot < it.inventory.size) {
                    // 获取行为
                    val action = when {
                        it.clickEvent().click.isShiftClick && it.rawSlot >= 0 && it.rawSlot < it.inventory.size -> ActionQuickTake()
                        it.clickEvent().click == org.bukkit.event.inventory.ClickType.NUMBER_KEY -> ActionKeyboard()
                        else -> ActionClick()
                    }
                    val cursor = action.getCursor(it) ?: ItemStack(Material.AIR)
                    // 点击有效位置
                    if (RuleImpl.checkSlot(it.inventory, cursor, action.getCurrentSlot(it))) {
                        it.isCancelled = true
                        // 提取物品
                        action.setCursor(it, RuleImpl.readItem(it.inventory, action.getCurrentSlot(it)))
                        // 写入物品
                        RuleImpl.writeItem(it.inventory, cursor, action.getCurrentSlot(it), it.clickEvent().click)
                    } else if (it.rawSlot >= 0 && it.rawSlot < it.inventory.size) {
                        it.isCancelled = true
                    }
                }
            }
        }
        // 生成页面
        return super.build()
    }

    object RuleImpl : StorableChest.Rule {

        /** 检查判定位置回调 **/
        var checkSlot: ((inventory: Inventory, itemStack: ItemStack, slot: Int) -> Boolean) = { _, _, _ -> true }

        /** 获取可用位置回调 **/
        var firstSlot: ((inventory: Inventory, itemStack: ItemStack) -> Int) = { _, _ -> -1 }

        /** 写入物品回调 **/
        var writeItem: ((inventory: Inventory, itemStack: ItemStack, slot: Int, type: BukkitClickType) -> Unit) = { inventory, item, slot, _ ->
            if (slot in 0 until inventory.size) inventory.setItem(slot, item)
        }

        /** 读取物品回调 **/
        var readItem: ((inventory: Inventory, slot: Int) -> ItemStack?) = { inventory, slot ->
            if (slot in 0 until inventory.size) inventory.getItem(slot)
            else null
        }

        /**
         * 定义判定位置
         * 玩家是否可以将物品放入
         */
        override fun checkSlot(intRange: Int, checkSlot: (inventory: Inventory, itemStack: ItemStack) -> Boolean) {
            checkSlot(intRange..intRange, checkSlot)
        }

        /**
         * 定义判定位置
         * 玩家是否可以将物品放入
         */
        override fun checkSlot(intRange: IntRange, callback: (inventory: Inventory, itemStack: ItemStack) -> Boolean) {
            val before = checkSlot
            checkSlot = { inventory, itemStack, slot ->
                if (slot in intRange) {
                    callback(inventory, itemStack)
                } else {
                    before(inventory, itemStack, slot)
                }
            }
        }

        /**
         * 定义判定位置
         * 玩家是否可以将物品放入
         */
        override fun checkSlot(callback: (inventory: Inventory, itemStack: ItemStack, slot: Int) -> Boolean) {
            val before = checkSlot
            checkSlot = { inventory, itemStack, slot ->
                callback(inventory, itemStack, slot) && before(inventory, itemStack, slot)
            }
        }

        /**
         * 获取页面中首个有效的位置
         * 用于玩家 SHIFT 点击快速放入物品，不再触发 checkSlot 回调
         */
        override fun firstSlot(firstSlot: (inventory: Inventory, itemStack: ItemStack) -> Int) {
            RuleImpl.firstSlot = firstSlot
        }

        /**
         * 物品写入回调
         */
        override fun writeItem(writeItem: (inventory: Inventory, itemStack: ItemStack, slot: Int) -> Unit) {
            RuleImpl.writeItem = { inventory, itemStack, slot, _ -> writeItem(inventory, itemStack, slot) }
        }

        /**
         * 物品写入回调
         */
        override fun writeItem(writeItem: (inventory: Inventory, itemStack: ItemStack, slot: Int, type: BukkitClickType) -> Unit) {
            RuleImpl.writeItem = writeItem
        }

        /**
         * 读取物品回调
         */
        override fun readItem(readItem: (inventory: Inventory, slot: Int) -> ItemStack?) {
            RuleImpl.readItem = readItem
        }
    }
}