package me.neon.libs.region

import me.neon.libs.NeonLibsLoader
import me.neon.libs.core.LifeCycle
import me.neon.libs.core.inject.Awake
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * NeonLibs
 * me.neon.libs.region
 *
 * @author 老廖
 * @since 2025/12/27 01:46
 */
object RegionManager {

    private val registerRegions: MutableMap<Plugin, MutableList<Regions>> = mutableMapOf()

    private val actionQueue: ConcurrentLinkedQueue<() -> Unit> = ConcurrentLinkedQueue()

    private var bukkitTask: BukkitTask? = null

    fun register(plugin: Plugin, regions: Regions) {
        actionQueue.add {
            val list = registerRegions.computeIfAbsent(plugin) { mutableListOf() }
            list.add(regions)
        }
    }

    fun unregister(plugin: Plugin) {
        actionQueue.add {
            registerRegions.remove(plugin)
        }
    }

    fun unregister(plugin: Plugin, regions: Regions) {
        actionQueue.add {
            registerRegions[plugin]?.removeIf { it.uniqueId == regions.uniqueId }
        }
    }

    @Awake(LifeCycle.ENABLE)
    internal fun start() {
        bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(NeonLibsLoader.getInstance(), Runnable {
            try {
                val players = Bukkit.getOnlinePlayers().toList()
                if (players.isEmpty()) return@Runnable
                var run = actionQueue.poll()
                while (run != null) {
                    run.invoke()
                    run = actionQueue.poll()
                }
                registerRegions.forEach {
                    it.value.forEach {
                        it.tick(players)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 20, 20)
    }

    @Awake(LifeCycle.DISABLE)
    internal fun close() {
        bukkitTask?.cancel()
    }
}