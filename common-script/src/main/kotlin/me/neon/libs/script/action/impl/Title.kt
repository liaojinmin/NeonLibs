package me.neon.libs.script.action.impl

import me.neon.libs.NeonLibsLoader
import me.neon.libs.script.action.ActionBase
import me.neon.libs.script.action.ActionContext
import me.neon.libs.util.replaceWithOrder
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.plugin.Plugin
import javax.script.SimpleScriptContext

/**
 * NeonFlash
 * me.neon.flash.api.action
 *
 * @author 老廖
 * @since 2024/3/20 13:50
 */
class Title : ActionBase() {

    companion object {

        val SENTENCE =  Regex("`(.+?)`")

    }

    override val regex = "(send)?-?(sub)?titles?".toRegex()

    override val plugin: Plugin = NeonLibsLoader.getInstance()

    override fun onExecute(player: Player, contents: ActionContext, scriptContext: SimpleScriptContext, event: Event?) {
        val title: String by contents
        val subTitle: String by contents
        val fadeIn: Int by contents
        val stay: Int by contents
        val fadeOut: Int by contents
        player.sendTitle(
            player.parse(title, scriptContext),
            player.parse(subTitle, scriptContext),
            fadeIn,
            stay,
            fadeOut
        )
    }

    override fun readContents(contents: Any): ActionContext {
        val actionContents = ActionContext()
        var content: String = contents.toString()
        val replacements = SENTENCE.findAll(content).mapIndexed { index, result ->
            content = content.replace(result.value, "{$index}")
            index to result.groupValues[1].replace("\\s", " ")
        }.toMap().values.toTypedArray()

        val split = content.split(" ", limit = 5)

        var title by actionContents
            title = split.getOrElse(0) { "" }.replaceWithOrder(*replacements)
        var subTitle by actionContents
            subTitle = split.getOrElse(1) { "" }.replaceWithOrder(*replacements)
        var fadeIn by actionContents
            fadeIn = split.getOrNull(2)?.toIntOrNull() ?: 15
        var stay by actionContents
            stay = split.getOrNull(3)?.toIntOrNull() ?: 20
        var fadeOut by actionContents
            fadeOut = split.getOrNull(4)?.toIntOrNull() ?: 15

        return actionContents
    }
}