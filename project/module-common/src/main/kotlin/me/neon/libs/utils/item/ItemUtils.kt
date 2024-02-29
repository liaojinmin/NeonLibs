package me.neon.libs.utils.item

import me.neon.libs.utils.LocaleI18n
import me.neon.libs.utils.getLocaleFile
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList



/**
 * 获取物品的名称（若存在 displayName 则返回 displayName，反之获取译名）
 */
fun ItemStack.getName(player: Player? = null): String {
    return if (itemMeta?.hasDisplayName() == true) itemMeta!!.displayName else getI18nName(player)
}

/**
 * 获取物品的译名
 */
fun ItemStack.getI18nName(player: Player? = null): String {
    val localeFile = player?.getLocaleFile() ?: LocaleI18n.getDefaultLocaleFile() ?: return type.name // ?: "NO_LOCALE"
    return localeFile[Bukkit.getUnsafe().getTranslationKey(this.type)] ?: type.name
}

/**
 * 序列化 ItemStack 为字符串
 */
fun ItemStack.serializeItemStacks(): String {
    val byteOutputStream = ByteArrayOutputStream()
    try {
        BukkitObjectOutputStream(byteOutputStream).use {
            it.writeInt(1)
            it.writeObject(serialize(this))
            return Base64Coder.encodeLines(byteOutputStream.toByteArray())
        }
    } catch (e: IOException) {
        throw IllegalArgumentException("无法序列化物品堆栈数据")
    }
}

/**
 * 序列化 ItemStack 数组 为字符串
 */
fun CopyOnWriteArrayList<ItemStack>?.serializeItemStacks(): String {
    if (this.isNullOrEmpty()) {
        return ""
    }
    val byteOutputStream = ByteArrayOutputStream()
    try {
        BukkitObjectOutputStream(byteOutputStream).use {
            it.writeInt(this.size)
            for (items in this) {
                it.writeObject(serialize(items))
            }
            return Base64Coder.encodeLines(byteOutputStream.toByteArray())
        }
    } catch (e: IOException) {
        throw IllegalArgumentException("无法序列化物品堆栈数据")
    }
}

fun String.deserializeItemStack(): ItemStack? {
    ByteArrayInputStream(Base64Coder.decodeLines(this)).use {
        BukkitObjectInputStream(it).use { var2 ->
            var2.readInt()
            return deserialize(var2.readObject())
        }
    }
}

fun String.deserializeItemStacks(): CopyOnWriteArrayList<ItemStack> {
    if (this == "null" || this.isEmpty()) {
        return CopyOnWriteArrayList()
    }
    ByteArrayInputStream(Base64Coder.decodeLines(this)).use {
        BukkitObjectInputStream(it).use { var2 ->
            val contents = arrayOfNulls<ItemStack?>(var2.readInt())
            for ((index, _) in contents.withIndex()) {
                contents[index] = deserialize(var2.readObject())
            }
            return CopyOnWriteArrayList(contents.filterNotNull())
        }
    }
}



private fun deserialize(item: Any?): ItemStack? {
    return if (item != null) ItemStack.deserialize((item as Map<String, Any>)) else null
}

private fun serialize(item: ItemStack?): Map<String, Any>? {
    return item?.serialize()
}


