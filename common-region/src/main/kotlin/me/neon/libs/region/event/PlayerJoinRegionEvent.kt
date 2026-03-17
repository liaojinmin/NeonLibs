package me.neon.libs.region.event

import me.neon.libs.region.RegionRange
import me.neon.libs.region.Regions
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/**
 * NeonLibs
 * me.neon.libs.region.event
 *
 * @author 老廖
 * @since 2025/12/26 20:40
 */
class PlayerJoinRegionEvent(player: Player, val regions: Regions, val child: RegionRange?): PlayerEvent(player), Cancellable {

    private var cancel = false

    fun isChild(): Boolean = child != null

    override fun getHandlers(): HandlerList {
        return handlersList
    }

    override fun isCancelled(): Boolean {
        return cancel
    }

    override fun setCancelled(p0: Boolean) {
        cancel = p0
    }

    companion object {

        private val handlersList = HandlerList()

        fun getHandlerList(): HandlerList {
            return handlersList;
        }

    }
}