package me.neon.libs.script.action.group

import me.neon.libs.script.action.ActionEntry
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.plugin.Plugin
import javax.script.SimpleScriptContext

/**
 * NeonFlash
 * me.neon.flash.api.action.group
 *
 * @author 老廖
 * @since 2024/3/23 0:29
 */
internal class StringGroup(
    override val priority: Int,
    private val actions: List<ActionEntry>
): IGroup {

    override fun isEmpty(): Boolean {
        return actions.isEmpty()
    }

    override fun getActions(player: Player?, event: Event?, factory: SimpleScriptContext): List<ActionEntry> {
        return actions
    }


}