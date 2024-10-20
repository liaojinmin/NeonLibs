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
 * @since 2024/3/25 14:43
 */
class Sound : ActionBase() {

    override val regex = "(play)?-?sounds?".toRegex()

    override val plugin: Plugin = NeonLibsLoader.getInstance()

    override fun onExecute(player: Player, contents: ActionContext, scriptContext: SimpleScriptContext, event: Event?) {
        contents.stringContent().parseContextSplit(player, scriptContext,";").forEach {
            val split = it.split("-")
            if (split.isNotEmpty()) {
                val sound: org.bukkit.Sound
                try {
                    sound = org.bukkit.Sound.valueOf(split[0].uppercase())
                } catch (t: Throwable) {
                    println("未知音效 $it")
                    t.printStackTrace()
                    return
                }
                val volume: Float = split.getOrNull(1)?.toFloatOrNull() ?: 1f
                val pitch: Float = split.getOrNull(2)?.toFloatOrNull() ?: 1f
                player.playSound(player.location, sound, volume, pitch)
            }
        }
    }
}