package me.neon.libs.script

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.neon.libs.util.getMetaFirstOrNull
import me.neon.libs.util.hasMeta
import me.neon.libs.util.replacePlaceholder
import me.neon.libs.util.setMeta
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import java.net.URL

/**
 * NeonLibs
 * me.neon.libs.api.script
 *
 * @author 老廖
 * @since 2024/2/21 22:10
 */
class JavaScriptUtils {

    private val serializer: GsonBuilder by lazy {
        GsonBuilder()
    }

    fun getPlayer(name: String): Player? {
        return Bukkit.getPlayerExact(name)
    }

    fun getOfflinePlayer(name: String): OfflinePlayer? {
        return Bukkit.getOfflinePlayers().find { it.name.equals(name, true) }
    }

    fun getOnlinePlayers(): List<Player> {
        return Bukkit.getOnlinePlayers().sortedBy { it.name }
    }

    fun getItemInHand(player: String, offhand: Boolean = false): ItemStack? {
        return getPlayerInventory(player)?.let {
            if (offhand) it.itemInOffHand
            else it.itemInMainHand
        }
    }

    // utils.getEquipment("老廖", "HEAD")
    fun getEquipment(player: String, equipmentSlot: String): ItemStack? {
        return getPlayer(player)?.run {
            try {
                inventory.getItem(EquipmentSlot.valueOf(equipmentSlot.uppercase()))
            }catch (e: Exception) {
                null
            }
        }
    }

    fun getPlayerInventory(player: String): PlayerInventory? {
        return getPlayer(player)?.inventory
    }

    fun getMeta(player: Player, key: String): String {
        return player.getMetaFirstOrNull(key)?.value()?.toString() ?: "null"
    }

    fun getEntityMeta(entity: Entity, key: String): String {
        return entity.getMetaFirstOrNull(key)?.value()?.toString() ?: "null"
    }

    fun getMetaObj(player: Player, key: String): Any {
        return player.getMetaFirstOrNull(key)?.value() ?: "null"
    }

    fun getMetaEntityObj(entity: Entity, key: String): Any {
        return entity.getMetaFirstOrNull(key)?.value() ?: "null"
    }

    fun hasMeta(player: Player, key: String): Boolean {
        return player.hasMeta(key)
    }

    fun hasEntityMeta(entity: Entity, key: String): Boolean {
        return entity.hasMeta(key)
    }

    fun setMeta(player: Player, key: String, value: String) {
        player.setMeta(key, value)
    }


    fun asString(any: Any): String {
        return any.toString()
    }

    fun asInt(any: Any): Int {
        return if (any is Number) {
            any.toInt()
        } else any.toString().toIntOrNull() ?: 0
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

    /**
     * 读 URL 内容
     */
    fun fromURL(url: String) = try {
        String(URL(url).openStream().readBytes())
    } catch (t: Throwable) {
        "<ERROR: ${t.localizedMessage}>"
    }

    /**
     * JSON 处理
     */
    fun asJsonElement(json: String): JsonElement = JsonParser.parseString(json)

    fun asJsonObject(json: String): JsonObject = JsonParser.parseString(json).asJsonObject

    fun asJsonArray(json: String): JsonArray = JsonParser.parseString(json).asJsonArray


}