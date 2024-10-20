package me.neon.libs.script.action

import me.neon.libs.script.JavaScriptHandle
import me.neon.libs.script.action.option.Option
import org.bukkit.entity.Player
import org.bukkit.event.Event
import javax.script.SimpleScriptContext

/**
 * NeonFlash
 * me.neon.flash.api.action
 *
 * @author 老廖
 * @since 2024/3/20 13:33
 */
data class ActionEntry(
    val base: ActionBase,
    val contents: ActionContext,
    val option: Option
) {

    fun execute(player: Player, event: Event? = null, factory: SimpleScriptContext) {
        if (!option.evalCondition(player, event, factory)) return
        base.onExecute(player, contents, factory, event)
    }

    companion object {

        private val actionsBound = Regex(" ?(_\\|\\|_|&&&) ?")

        private fun parse(list: MutableList<ActionEntry>, str: String) {
            val loaded = str.split(actionsBound).map { st ->
                val split = st.split(": ", limit = 2)
                val string = split.getOrElse(1) { split[0] }
                JavaScriptHandle.getAction(split[0])?.let {
                    val (content, option) = it.ofOption(string)
                    it.ofEntry(content, option)
                } ?: error("未知动作 $st")
            }
            list.addAll(loaded)
        }

        internal fun of(any: Any): List<ActionEntry> {
            val entries = mutableListOf<ActionEntry>()
            when (any) {
                is Map<*, *> -> {
                    val entry = any.entries.firstOrNull() ?: return entries
                    val key = entry.key.toString()
                    val value = entry.value ?: return entries
                    JavaScriptHandle.getAction(key)?.let {
                        val actionEntry = it.ofEntry(value, Option())
                        entries.add(actionEntry)
                    }
                }
                is List<*> -> {
                    if (any.all { it is String }) {
                        any.forEach {
                            parse(entries, it.toString())
                        }
                    } else parse(entries, any.toString())
                }
                else -> {
                    parse(entries, any.toString())
                }
            }
            return entries
        }
    }

}
