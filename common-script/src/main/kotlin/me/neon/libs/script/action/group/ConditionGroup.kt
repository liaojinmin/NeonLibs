package me.neon.libs.script.action.group

import me.neon.libs.script.JavaScriptHandle
import me.neon.libs.script.action.ActionEntry
import org.bukkit.entity.Player
import org.bukkit.event.Event
import javax.script.SimpleScriptContext

/**
 * NeonFlash
 * me.neon.flash.api.action.group
 *
 * @author 老廖
 * @since 2024/3/22 22:11
 */
internal data class ConditionGroup(
    override val priority: Int,
    val condition: String,
    val accept: List<IGroup>,
    val deny: List<IGroup>,
): IGroup {

    override fun isEmpty(): Boolean {
        return check(accept) && check(deny)
    }

    override fun getActions(player: Player?, event: Event?, factory: SimpleScriptContext): List<ActionEntry> {
        return if (evalCondition(player, event, factory)) {
            getAction(player, event, factory, accept)
        } else {
            getAction(player, event, factory, deny)
        }
    }

    private fun evalCondition(player: Player?, event: Event?, factory: SimpleScriptContext): Boolean {
        return if (condition.isNotBlank()) {
            JavaScriptHandle.conditionParser.invoke(player, event, condition, factory)
        } else true
    }

    private fun getAction(player: Player?, event: Event?, factory: SimpleScriptContext, list: List<IGroup>): List<ActionEntry> {
        return mutableListOf<ActionEntry>().apply {
            list.sortedBy { it.priority }.forEach { addAll(it.getActions(player, event, factory)) }
        }
    }

    private fun check(list: List<IGroup>): Boolean {
        return list.isEmpty() || list.all { it.isEmpty() }
    }

}