package me.neon.libs.script.action.impl

import me.neon.libs.NeonLibsLoader
import me.neon.libs.script.action.ActionBase
import me.neon.libs.script.action.ActionContext
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.plugin.Plugin
import javax.script.SimpleScriptContext

/**
 * NeonFlash
 * me.neon.flash.api.action.impl.logic
 *
 * @author 老廖
 * @since 2024/3/25 14:18
 */
class Cancel: ActionBase() {

    override val regex = "cancel|取消事件".toRegex()

    override val plugin: Plugin = NeonLibsLoader.getInstance()

    override fun onExecute(player: Player, contents: ActionContext, scriptContext: SimpleScriptContext, event: Event?) {
        if (event is Cancellable) {
            event.isCancelled = true
        }
    }

}