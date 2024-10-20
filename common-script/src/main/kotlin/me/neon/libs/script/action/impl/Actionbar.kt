package me.neon.libs.script.action.impl

import me.neon.libs.NeonLibsLoader
import me.neon.libs.script.action.ActionBase
import me.neon.libs.script.action.ActionContext
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.plugin.Plugin
import javax.script.SimpleScriptContext


/**
 * NeonFlash
 * me.neon.flash.api.action
 *
 * @author 老廖
 * @since 2024/3/20 13:43
 */
class Actionbar() : ActionBase() {

    override val regex = "action(bar)?s?".toRegex()

    override val plugin: Plugin = NeonLibsLoader.getInstance()

    override fun onExecute(player: Player, contents: ActionContext, scriptContext: SimpleScriptContext, event: Event?) {
        player.sendActionBar(parseContext(player, contents.stringContent(), scriptContext))
    }
}