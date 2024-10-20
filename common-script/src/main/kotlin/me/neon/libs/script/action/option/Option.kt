package me.neon.libs.script.action.option

import me.neon.libs.script.JavaScriptHandle
import org.bukkit.entity.Player
import org.bukkit.event.Event
import java.util.concurrent.ThreadLocalRandom
import javax.script.SimpleScriptContext

/**
 * NeonFlash
 * me.neon.flash.api.action.option
 *
 * @author 老廖
 * @since 2024/3/20 14:09
 */
class Option(val set: Map<OptionType, String> = mapOf()) {

    fun evalChance(): Boolean {
        return if (!set.containsKey(OptionType.CHANCE)) true
        else (ThreadLocalRandom.current().nextDouble() <= (set[OptionType.CHANCE]!!.toDoubleOrNull() ?: 0.0))
    }

    fun evalCondition(player: Player, event: Event? = null, factory: SimpleScriptContext): Boolean {
        return if (!set.containsKey(OptionType.CONDITION)) {
            true
        } else {
            set[OptionType.CONDITION]?.let {
                JavaScriptHandle.conditionParser.invoke(player, event, it, factory)
            } ?: false
        }
    }
}