package me.neon.libs.script

import me.neon.libs.utils.getMetaFirstOrNull
import me.neon.libs.utils.hasMeta
import me.neon.libs.utils.replacePlaceholder
import me.neon.libs.utils.setMeta
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * NeonLibs
 * me.neon.libs.api.script
 *
 * @author 老廖
 * @since 2024/2/21 22:10
 */
class JavaScriptUtils {

    fun getPlayer(name: String): Player? {
        return Bukkit.getPlayerExact(name)
    }

    fun getMeta(player: Player, key: String): String {
        return player.getMetaFirstOrNull(key)?.value()?.toString() ?: "null"
    }

    fun getMetaObj(player: Player, key: String): Any {
        return player.getMetaFirstOrNull(key)?.value() ?: "null"
    }

    fun hasMeta(player: Player, key: String): Boolean {
        return player.hasMeta(key)
    }

    fun setMeta(player: Player, key: String, value: String) {
        player.setMeta(key, value)
    }


    fun asString(any: Any): String {
        return any.toString()
    }

    fun parse(player: Player, any: Any): String {
        return any.toString().replacePlaceholder(player)
    }


    fun runCommand(player: Player, string: String) {
        Bukkit.dispatchCommand(player, string.replacePlaceholder(player))
    }

    fun runConsoleCommand(player: Player?, string: String) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), if (player != null) string.replacePlaceholder(player) else string)
    }



}