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
 * @since 2024/3/23 0:30
 */
interface IGroup {

    val priority: Int

    fun isEmpty(): Boolean

    fun getActions(player: Player?, event: Event?, factory: SimpleScriptContext): List<ActionEntry>

    companion object {

        private fun of(priority: Int, any: Any): IGroup? {
            if (any is String) {
                return StringGroup(priority, ActionEntry.of(any))
            }
            val section = Property.asSection(any) ?: return null
            val keyPriority = Property.PRIORITY.getKey(section)
            val keyRequirement = Property.CONDITION.getKey(section)
            val keyActions = Property.ACTIONS.ofList(section)
            val keyDenyActions = Property.DENY_ACTIONS.ofList(section)
            return ConditionGroup(
                section.getInt(keyPriority, priority),
                section.getString(keyRequirement, "")!!,
                ofGroups(keyActions),
                ofGroups(keyDenyActions)
            )
        }

        internal fun ofGroups(any: Any?): List<IGroup> {
            val reacts = mutableListOf<IGroup>()
            if (any != null) {
                if (any is List<*>) {
                    var order = 0
                    any.filterNotNull().forEach { of(order++, it)?.let { react -> reacts.add(react) } }
                } else of(-1, any)?.let { reacts.add(it) }
            }
            return reacts
        }

    }

}