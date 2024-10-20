package me.neon.libs.util.item

import com.google.common.collect.ImmutableMap
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.function.Function
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


@OptIn(ExperimentalContracts::class)
fun Material?.isAir(): Boolean {
    contract { returns(false) implies (this@isAir != null) }
    return this == null || this == Material.AIR || name.endsWith("_AIR")
}

@OptIn(ExperimentalContracts::class)
fun Material?.isNotAir(): Boolean {
    contract { returns(true) implies (this@isNotAir != null) }
    return !isAir()
}

@OptIn(ExperimentalContracts::class)
fun ItemStack?.isAir(): Boolean {
    contract { returns(false) implies (this@isAir != null) }
    return this == null || type == Material.AIR || type.name.endsWith("_AIR")
}

@OptIn(ExperimentalContracts::class)
fun ItemStack?.isNotAir(): Boolean {
    contract { returns(true) implies (this@isNotAir != null) }
    return !isAir()
}

/**
 * 编辑物品元数据
 */
@Suppress("UNCHECKED_CAST")
fun <T : ItemMeta> ItemStack.modifyMeta(func: T.() -> Unit): ItemStack {
    if (isAir()) {
        error("air")
    }
    return also { itemMeta = ((itemMeta as? T)?.also(func) ?: itemMeta) }
}

/**
 * 编辑物品描述
 */
fun ItemMeta.modifyLore(func: MutableList<String>.() -> Unit): ItemMeta {
    return also { lore = (lore ?: ArrayList<String>()).also(func) }
}

/**
 * 编辑物品描述
 */
fun ItemStack.modifyLore(func: MutableList<String>.() -> Unit): ItemStack {
    if (isAir()) {
        error("air")
    }
    return modifyMeta<ItemMeta> { modifyLore(func) }
}

/**
 * 判断物品是否存在名称或特定名称
 * @param name 特定名称（留空判断是否存在任意名称）
 */
fun ItemStack.hasName(name: String? = null): Boolean {
    return if (name == null) itemMeta?.hasDisplayName() == true else itemMeta?.displayName?.contains(name) == true
}

/**
 * 判断物品是否存在描述或特定描述
 * @param lore 特定描述（留空判断是否存在任意描述）
 */
fun ItemStack.hasLore(lore: String? = null): Boolean {
    return if (lore == null) itemMeta?.hasLore() == true else itemMeta?.lore?.toString()?.contains(lore) == true
}

/**
 * 替换物品名称（完全替换）
 *
 * @param nameOld 文本
 * @param nameNew 文本
 * @return ItemStack
 */
fun ItemStack.replaceName(nameOld: String, nameNew: String): ItemStack {
    return replaceName(ImmutableMap.of(nameOld, nameNew))
}

/**
 * 替换物品描述（完全替换）
 *
 * @param loreOld 文本
 * @param loreNew 文本
 * @return ItemStack
 */
fun ItemStack.replaceLore(loreOld: String, loreNew: String): ItemStack {
    return replaceLore(ImmutableMap.of(loreOld, loreNew))
}

/**
 * 替换物品名称（完全替换）
 *
 * @param map  文本关系
 * @return ItemStack
 */
fun ItemStack.replaceName(map: Map<String, String>): ItemStack {
    if (hasName()) {
        val meta = itemMeta!!
        var name = meta.displayName
        map.forEach { name = name.replace(it.key, it.value) }
        meta.setDisplayName(name)
        itemMeta = meta
    }
    return this
}

/**
 * 替换物品描述（完全替换）
 *
 * @param map  文本关系
 * @return ItemStack
 */
fun ItemStack.replaceLore(map: Map<String, String>): ItemStack {
    if (hasLore()) {
        val meta = itemMeta!!
        val lore = meta.lore!!
        lore.indices.forEach { i ->
            var line = lore[i]
            map.forEach { line = line.replace(it.key, it.value) }
            lore[i] = line
        }
        meta.lore = lore
        itemMeta = meta
    }
    return this
}

/**
 * 替换物品描述（完全替换）
 *
 * @param function  文本更新
 * @return ItemStack
 */
fun ItemStack.replaceMatchLore(function: Function<String, String>): ItemStack {
    if (hasLore()) {
        val meta = itemMeta!!
        val lore = meta.lore!!
        lore.indices.forEach { i ->
            lore[i] = function.apply(lore[i])
        }
        meta.lore = lore
        itemMeta = meta
    }
    return this
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
fun Collection<ItemStack>?.serializeItemStacks(): String {
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

fun String.deserializeItemStacks(): Collection<ItemStack> {
    if (this == "null" || this.isEmpty()) {
        return mutableListOf()
    }
    ByteArrayInputStream(Base64Coder.decodeLines(this)).use {
        BukkitObjectInputStream(it).use { var2 ->
            val contents = arrayOfNulls<ItemStack?>(var2.readInt())
            for ((index, _) in contents.withIndex()) {
                contents[index] = deserialize(var2.readObject())
            }
            return ArrayList(contents.filterNotNull())
        }
    }
}



private fun deserialize(item: Any?): ItemStack? {
    return if (item != null) ItemStack.deserialize((item as Map<String, Any>)) else null
}

private fun serialize(item: ItemStack?): Map<String, Any>? {
    return item?.serialize()
}


