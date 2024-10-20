package me.neon.libs.script.action.impl

import me.neon.libs.NeonLibsLoader
import me.neon.libs.script.JavaScriptHandle
import me.neon.libs.script.action.ActionBase
import me.neon.libs.script.action.ActionContext
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
class JavaScript : ActionBase() {

    override val regex = "((java)?-?script|js)s?".toRegex()

    override val plugin: Plugin = NeonLibsLoader.getInstance()

    override fun readContents(contents: Any): ActionContext {
        val sc = super.readContents(contents)
        JavaScriptHandle.compiledScriptAndSave(sc.stringContent())
        return sc
    }

    override fun onExecute(player: Player, contents: ActionContext, scriptContext: SimpleScriptContext, event: Event?) {
        JavaScriptHandle.runJS(player, event, contents.stringContent(), scriptContext)
    }

}