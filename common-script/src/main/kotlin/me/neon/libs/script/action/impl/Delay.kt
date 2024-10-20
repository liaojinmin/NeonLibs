package me.neon.libs.script.action.impl

import me.neon.libs.NeonLibsLoader
import me.neon.libs.script.action.ActionBase
import me.neon.libs.script.action.ActionContext
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.plugin.Plugin
import javax.script.SimpleScriptContext


/**
 * TrMenu
 * me.neon.libs.script.action.impl.Delay
 *
 * @author Score2
 * @since 2022/02/10 22:09
 */
class Delay : ActionBase() {

    override val regex = Regex("delay|wait")

    override val plugin: Plugin = NeonLibsLoader.getInstance()

    fun getDelay(player: Player, content: String, factory: SimpleScriptContext): Long {
        return player.parse(content, factory).toLongOrNull() ?: 0
    }

    override fun onExecute(player: Player, contents: ActionContext, scriptContext: SimpleScriptContext, event: Event?) {}

}