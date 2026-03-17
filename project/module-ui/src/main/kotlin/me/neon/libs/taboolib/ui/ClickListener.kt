package me.neon.libs.taboolib.ui

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import me.neon.libs.NeonLibsLoader
import me.neon.libs.core.LifeCycle
import me.neon.libs.core.inject.Awake
import me.neon.libs.event.SubscribeEvent
import me.neon.libs.taboolib.ui.type.impl.ChestImpl
import me.neon.libs.util.asyncRunner
import me.neon.libs.util.setMeta
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*
import java.util.stream.IntStream
import kotlin.math.cos
import kotlin.math.sin

internal object ClickListener {

    @Awake(LifeCycle.DISABLE)
    fun onDisable() {
        Bukkit.getOnlinePlayers().forEach {
            if (MenuHolder.fromInventory(InventoryViewProxy.getTopInventory(it.openInventory)) != null) {
                it.closeInventory()
            }
        }
    }

    @SubscribeEvent
    fun onOpen(e: InventoryOpenEvent) {
        val builder = MenuHolder.fromInventory(e.inventory) as? ChestImpl ?: return
        val player = e.player as Player
        // 构建回调
        Bukkit.getScheduler().runTask(NeonLibsLoader.getInstance(), Runnable {
            builder.buildCallback(player, e.inventory)
            builder.finalBuildCallback(player, e.inventory)
        })
        // 异步构建回调
        asyncRunner {
            builder.asyncBuildCallback(player, e.inventory)
            builder.asyncFinalBuildCallback(player, e.inventory)
        }
    }

    @Suppress("DuplicatedCode")
    @SubscribeEvent
    fun onClick(e: InventoryClickEvent) {
        val builder = MenuHolder.fromInventory(e.inventory) as? ChestImpl ?: return
        // 锁定主手
        if (builder.handLocked) {
            if (e.action == InventoryAction.COLLECT_TO_CURSOR) {
                if (e.cursor?.isSimilar(e.inventory.getItem(e.whoClicked.inventory.heldItemSlot)) == true) {
                    e.isCancelled = true
                }
            } else if (e.rawSlot - e.inventory.size - 27 == e.whoClicked.inventory.heldItemSlot || e.click == org.bukkit.event.inventory.ClickType.NUMBER_KEY && e.hotbarButton == e.whoClicked.inventory.heldItemSlot) {
                e.isCancelled = true
            }
        }
        // 处理事件
        try {
            val event = ClickEvent(e, ClickType.CLICK, builder.getSlot(e.rawSlot), builder)
            builder.clickCallback.forEach { it(event) }
            builder.selfClickCallback(event)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        // 如果事件取消则不处理后续逻辑
        if (e.isCancelled) {
            return
        }
        // 丢弃逻辑
        if (e.currentItem?.type != Material.AIR && e.click == org.bukkit.event.inventory.ClickType.DROP) {
            val item = itemDrop(e.whoClicked as Player, e.currentItem)
            item.pickupDelay = 20
            item.setMeta("internal-drop", true)
            val event = PlayerDropItemEvent((e.whoClicked as Player), item)
            Bukkit.getPluginManager().callEvent(event)
            if (event.isCancelled) {
                event.itemDrop.remove()
            } else {
                e.currentItem?.type = Material.AIR
                e.currentItem = null
            }
        } else if (e.currentItem?.type != Material.AIR && e.rawSlot == -999) {
            val item = itemDrop(e.whoClicked as Player, e.cursor)
            item.pickupDelay = 20
            item.setMeta("internal-drop", true)
            val event = PlayerDropItemEvent((e.whoClicked as Player), item)
            Bukkit.getPluginManager().callEvent(event)
            if (event.isCancelled) {
                event.itemDrop.remove()
            } else {
                InventoryViewProxy.getCursor(e.view)?.type = Material.AIR
                InventoryViewProxy.setCursor(e.view, null)
            }
        }
    }

    @SubscribeEvent
    fun onDrag(e: InventoryDragEvent) {
        val menu = MenuHolder.fromInventory(e.inventory) as? ChestImpl ?: return
        val clickEvent = ClickEvent(e, ClickType.DRAG, ' ', menu)
        menu.clickCallback.forEach { it.invoke(clickEvent) }
        menu.selfClickCallback(clickEvent)
    }

    @SubscribeEvent
    fun onClose(e: InventoryCloseEvent) {
        val menu = MenuHolder.fromInventory(e.inventory) as? ChestImpl ?: return
        // 标题更新 && 跳过关闭回调
        if (menu.isUpdateTitle && menu.isSkipCloseCallbackOnUpdateTitle) {
            return
        }
        menu.closeCallback.invoke(e)
        // 只触发一次
        if (menu.onceCloseCallback) {
            menu.closeCallback = {}
        }
    }

    @SubscribeEvent
    fun onDropItem(e: PlayerDropItemEvent) {
        val builder = MenuHolder.fromInventory(InventoryViewProxy.getTopInventory(e.player.openInventory)) ?: return
        if (builder.handLocked && !e.itemDrop.hasMetadata("internal-drop")) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun onItemHeld(e: PlayerItemHeldEvent) {
        val builder = MenuHolder.fromInventory(InventoryViewProxy.getTopInventory(e.player.openInventory)) ?: return
        if (builder.handLocked) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun onSwap(e: PlayerSwapHandItemsEvent) {
        val builder = MenuHolder.fromInventory(InventoryViewProxy.getTopInventory(e.player.openInventory)) ?: return
        if (builder.handLocked) {
            e.isCancelled = true
        }
    }


    private fun itemDrop(player: Player, itemStack: ItemStack?, bulletSpread: Double = 0.0, radius: Double = 0.4): Item {
        val location = player.location.add(0.0, 1.5, 0.0)
        val item = player.world.dropItem(location, itemStack!!)
        val yaw = Math.toRadians((-player.location.yaw - 90.0f).toDouble())
        val pitch = Math.toRadians(-player.location.pitch.toDouble())
        val x: Double
        val y: Double
        val z: Double
        val v = cos(pitch) * cos(yaw)
        val v1 = -sin(yaw) * cos(pitch)
        if (bulletSpread > 0.0) {
            val spread = doubleArrayOf(1.0, 1.0, 1.0)
            IntStream.range(0, 3)
                .forEach { t: Int -> spread[t] = (Random().nextDouble() - Random().nextDouble()) * bulletSpread * 0.1 }
            x = v + spread[0]
            y = sin(pitch) + spread[1]
            z = v1 + spread[2]
        } else {
            x = v
            y = sin(pitch)
            z = v1
        }
        val dirVel = Vector(x, y, z)
        item.velocity = dirVel.normalize().multiply(radius)
        return item
    }
}