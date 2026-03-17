package me.neon.libs.region

import me.neon.libs.NeonLibsLoader
import me.neon.libs.region.event.PlayerJoinRegionEvent
import me.neon.libs.region.event.PlayerQuitRegionEvent
import me.neon.libs.script.JavaScriptHandle
import me.neon.libs.script.action.group.ExecuteGroup
import me.neon.libs.script.getPlayer
import me.neon.libs.script.put
import me.neon.libs.taboolib.configuration.ConfigurationSection
import me.neon.libs.util.BoundingBox
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Biome
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Supplier
import javax.script.SimpleScriptContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * NeonLibs
 * me.neon.libs.region
 *
 * @author 老廖
 * @since 2025/12/26 19:42
 */
abstract class Regions(
    val source: ConfigurationSection,
) {

    abstract val uniqueId: String

    abstract val group: String

    abstract val world: String

    private val regionPlayer: MutableSet<UUID> = mutableSetOf()

    private val childPlayer: MutableMap<String, MutableSet<UUID>> = mutableMapOf()

    private val ranges: Map<String, RegionRange> = source.getConfigurationSection("ranges")?.let {
        it.getKeys(false).map { key ->
            RegionRange(key, RegionRange.ofBoundingBox(it.getConfigurationSection(key)!!))
        }.associateBy { it.uniqueId }
    } ?: emptyMap()

    private val actions: Map<RegionAction, ExecuteGroup> = source.getConfigurationSection("actions")?.let {
        val root = JavaScriptHandle.loaderGroup(it)
        val out = mutableMapOf<RegionAction, ExecuteGroup>()
        root.forEach { (key, value) ->
            val action = RegionAction.valueOf(key.uppercase())
            out[action] = value
        }
        out
    } ?: emptyMap()

    val context: SimpleScriptContext by lazy {
        SimpleScriptContext().also {
            it.put("Regions", this@Regions)
            it.put("getPlayerRegionName", Supplier<String> {
                val player = it.getPlayer() ?: return@Supplier "PLAYER IS NULL"
                for (list in childPlayer) {
                    if (list.value.contains(player.uniqueId)) {
                        return@Supplier list.key
                    }
                }
                return@Supplier "NULL"
            })
        }
    }

    var pvp: Boolean = false

    var biome: Biome? = (source.getString("biome")?.uppercase()?.let { Biome.valueOf(it) })
        set(value) {
            if (value != field) {
                field = value

                initBiome()
            }
        }

    open fun initBiome() {
        if (world.isEmpty()) {
            NeonLibsLoader.warning("Regions.initBiome >>> world is empty")
        }
        if (biome == null) return
        val w = Bukkit.getWorld(world) ?: return
        if (ranges.isEmpty()) return
        ranges.values.forEach {
            // it.box = BoundingBox
            val box = it.box
            val minX = box.getMinX().toInt()
            val maxX = box.getMaxX().toInt()
            val minZ = box.getMinZ().toInt()
            val maxZ = box.getMaxZ().toInt()
            for (x in minX..maxX) {
                for (z in minZ..maxZ) {
                    w.setBiome(x, z, biome!!)
                }
            }
        }
    }

    open fun tick(players: List<Player>) {
        if (players.isEmpty()) return
      //  val tickPlayer = mutableListOf<Player>()
      //  val removePlayer = mutableListOf<UUID>()
        players.forEach {
            if (it.world.name == world) {
                val location = it.location
                val ins = getRegionRange(location)
                if (ins != null) {
                    if (regionPlayer.add(it.uniqueId)) {
                        Bukkit.getScheduler().runTask(NeonLibsLoader.getInstance(), Runnable {
                            val event = PlayerJoinRegionEvent(it, this, ins)
                            event.callEvent()
                            if (!event.isCancelled) {
                                actions[RegionAction.JOIN]?.eval(it, context, event)
                               // tickPlayer.add(it)
                            }
                        })
                    }
                    evalChildPlayer(it, location)
                } else {
                    if (regionPlayer.remove(it.uniqueId)) {
                        Bukkit.getScheduler().runTask(NeonLibsLoader.getInstance(), Runnable {
                            val event = PlayerQuitRegionEvent(it, this, null)
                            event.callEvent()
                            if (!event.isCancelled) {
                                actions[RegionAction.QUIT]?.eval(it, context, event)
                            }
                        })
                    //    removePlayer.add(it.uniqueId)
                        // 跑一遍更新
                        evalChildPlayer(it, location)
                    }
                }
            }
        }
    }

    fun getAllRegion(): Set<RegionRange> {
        return ranges.values.toSet()
    }

    fun checkRegions(location: Location): Boolean {
        return getRegionRange(location) != null
    }

    fun getRegionRange(location: Location): RegionRange? {
        if (location.world.name.equals(world, ignoreCase = true)) {
            for (range in this.ranges.values) {
                if (ins(location, range)) {
                    return range
                }
            }
        }
        return null
    }

    fun countBlocksInSquare(box: BoundingBox): Int {
        val deltaX = abs(box.getMaxX() - box.getMinX()).toInt() // / 1;
        val deltaY = abs(box.getMaxY() - box.getMinY()).toInt() // / 1;
        val deltaZ = abs(box.getMaxZ() - box.getMinZ()).toInt() // / 1;
        return deltaX * deltaY * deltaZ
    }

    private fun evalChildPlayer(player: Player, location: Location) {
        for (entry in ranges) {
            // 先检测子区域的加入
            if (ins(location, entry.value)) {
                val inPlayers = childPlayer.computeIfAbsent(entry.value.uniqueId) { mutableSetOf() }
                if (inPlayers.add(player.uniqueId)) {
                    Bukkit.getScheduler().runTask(NeonLibsLoader.getInstance(), Runnable {
                        val event = PlayerJoinRegionEvent(player, this, entry.value)
                        event.callEvent()
                        if (!event.isCancelled) {
                            actions[RegionAction.JOIN_CHILD]?.eval(player, context, event)
                        }
                    })
                }
            } else {
                // 可直接尝试算离开
                val inPlayers = childPlayer.computeIfAbsent(entry.value.uniqueId) { mutableSetOf() }
                if (inPlayers.remove(player.uniqueId)) {
                    Bukkit.getScheduler().runTask(NeonLibsLoader.getInstance(), Runnable {
                        val event = PlayerQuitRegionEvent(player, this, entry.value)
                        event.callEvent()
                        if (!event.isCancelled) {
                            actions[RegionAction.QUIT_CHILD]?.eval(player, context, event)
                        }
                    })
                }
            }
        }
    }

    private fun ins(location: Location, range: RegionRange): Boolean {
        if (ins(location.x, range.box.getMaxX(), range.box.getMinX())) {
            if (ins(location.y, range.box.getMaxY(), range.box.getMinY())) {
                if (ins(location.z, range.box.getMaxZ(), range.box.getMinZ())) {
                    return true
                }
            }
        }
        return false
    }

    private fun ins(a: Double, b: Double, c: Double): Boolean {
        val max = max(b, c)
        val min = min(b, c)
        return a in min..max
    }

}