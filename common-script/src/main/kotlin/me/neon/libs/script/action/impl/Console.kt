package me.neon.libs.script.action.impl

import me.neon.libs.NeonLibsLoader
import me.neon.libs.script.action.ActionBase
import me.neon.libs.script.action.ActionContext
import org.bukkit.Bukkit
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
class Console : ActionBase() {

    override val regex = "console".toRegex()

    override val plugin: Plugin = NeonLibsLoader.getInstance()

    override fun onExecute(player: Player, contents: ActionContext, scriptContext: SimpleScriptContext, event: Event?) {
        val func = {
            contents.stringContent().parseContextSplit(player, scriptContext, ";").forEach {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it)
            }
        }
        if (Bukkit.isPrimaryThread()) {
            func.invoke()
        } else {
            Bukkit.getScheduler().runTask(NeonLibsLoader.getInstance(), Runnable {
                func.invoke()
            })
        }
    }

}