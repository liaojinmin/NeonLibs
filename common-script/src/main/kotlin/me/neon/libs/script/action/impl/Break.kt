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
 * trplugins.menu.api.action.impl.logic.Return
 *
 * @author Score2
 * @since 2022/02/10 22:09
 */
class Break : ActionBase() {

    override val regex = "return|break".toRegex()
    override val plugin: Plugin = NeonLibsLoader.getInstance()
    override fun onExecute(player: Player, contents: ActionContext, scriptContext: SimpleScriptContext, event: Event?) {
    }
}