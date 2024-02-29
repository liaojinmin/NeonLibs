package me.neon.libs.taboolib.event

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.Plugin
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

/**
 * NeonLibs
 * me.neon.libs
 *
 * @author 老廖
 * @since 2024/2/23 16:56
 */
object Event {

    private val listenEvents = ConcurrentHashMap<Plugin, ArrayList<BukkitListener>>()

    fun addEventCache(plugin: Plugin, bukkitListener: BukkitListener) {
        listenEvents.computeIfAbsent(plugin) {
            ArrayList<BukkitListener>().also { it.add(bukkitListener) }
        }.add(bukkitListener)
    }

    fun onDisable(plugin: Plugin) {
        listenEvents.remove(plugin)?.forEach { unregisterListener(it) }
    }
}

/**
 * 注册一个 Bukkit/Nukkit 监听器
 *
 * @param event 事件
 * @param priority 优先级
 * @param ignoreCancelled 是否忽略取消事件
 * @param func 事件处理函数
 * @return 监听器
 */
@Suppress("UNCHECKED_CAST")
fun <T> registerListener(
    plugin: Plugin,
    event: Class<T>,
    priority: EventPriority,
    ignoreCancelled: Boolean,
    func: (T) -> Unit
): BukkitListener {
    if (Event::class.java.isAssignableFrom(event)) {
        val listener = BukkitListener(event) { func(it as T) }
        me.neon.libs.taboolib.event.Event.addEventCache(plugin, listener)
        Bukkit.getPluginManager()
            .registerEvent(event as Class<Event>, listener, priority, listener, plugin, ignoreCancelled)
        return listener
    } else error("unsupported event type: ${event.name}\"")
}


fun unregisterListener(bukkitListener: BukkitListener) {
    HandlerList.unregisterAll(bukkitListener)
}


class BukkitListener(private val clazz: Class<*>, val consumer: (Event) -> Unit): Listener, EventExecutor, Closeable {

    private val isVanillaEvent = Event::class.java.isAssignableFrom(clazz)

    private val ignored = CopyOnWriteArraySet<Class<*>>()

    override fun execute(listener: Listener, event: Event) {
        if (ignored.contains(event.javaClass)) {
            return
        }

        if (!isVanillaEvent) {
            ignored += event.javaClass
            return
        }

        if (clazz.isAssignableFrom(event.javaClass)) {
            consumer(event)
        } else {
            ignored += event.javaClass
        }
    }

    override fun close() {
        unregisterListener(this)
    }
}
