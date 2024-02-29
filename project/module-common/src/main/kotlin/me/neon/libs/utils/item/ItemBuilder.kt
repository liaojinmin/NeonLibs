
@file:Suppress("DEPRECATION")

package me.neon.libs.utils.item

import me.neon.libs.taboolib.chat.HexColor.colored
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.block.banner.Pattern
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.*
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect
import java.util.*



/**
 * 通过现有物品构建新的物品
 *
 * @param itemStack 原始物品
 * @param builder 构建器
 * @return 新的物品
 * @throws IllegalStateException 如果物品为空气
 */
fun buildItem(itemStack: ItemStack, builder: ItemBuilder.() -> Unit = {}): ItemStack {
    if (itemStack.type == Material.AIR) {
        error("air")
    }
    return ItemBuilder(itemStack).also(builder).build()
}

/**
 * 通过 [Material] 材质构建新的物品
 *
 * @param material 材质
 * @param builder 构建器
 * @return 新的物品
 * @throws IllegalStateException 如果材质为空气
 */
fun buildItem(material: Material, builder: ItemBuilder.() -> Unit = {}): ItemStack {
    if (material == Material.AIR) {
        error("air")
    }
    return ItemBuilder(material).also(builder).build()
}

fun parseItemStackToString(isFace: Boolean = true, vararg itemStacks: ItemStack): List<String> {
    return mutableListOf<String>().apply {
        itemStacks.forEach {
            if (isFace) {
                val builder = StringBuilder()
                builder.append("material:").append(it.type).append(",")
                builder.append("amount:").append(it.amount)
                it.itemMeta?.let { meta ->
                    if (meta.hasDisplayName()) {
                        builder.append(",").append("name:").append(meta.displayName)
                    }

                    meta.lore?.let { l1 ->
                        val b = StringBuilder()
                        l1.forEach { l2 ->
                            b.append(l2).append("\\n")
                        }
                        builder.append(",").append("lore:").append(b)
                    }

                    if ((meta as Damageable).hasDamage()) {
                        builder.append(",").append("data:").append((meta as Damageable).damage)
                    }

                    try {
                        if (meta.hasCustomModelData()) {
                            builder.append(",").append("modelData:").append(meta.customModelData)
                        }
                    } catch (ignored: NoSuchMethodException) {
                    }
                }
                add(builder.toString())
            } else {
                add(it.serializeItemStacks())
            }
        }
    }
}

fun parseItemStack(input: String): ItemStack {
    if (!input.contains("material")) {
        return input.deserializeItemStack() ?: error("无法反序列化物品 源: $input")
    }
    val parts = input.split(",").associate {
        val (key, value) = it.split(":")
        key.trim() to value.trim()
    }
    val materialString = parts["material"]?.uppercase() ?: error("无法解析物品 -> $input")
    return buildItem(Material.valueOf(materialString)) {

        amount = parts["amount"]?.toIntOrNull() ?: 1

        parts["name"]?.let {
            name = it.colored()
        }

        parts["lore"]?.let {
            lore.addAll(it.split("\\n").colored())
        }

        parts["data"]?.let {
            this.damage = it.toIntOrNull() ?: 0
        }

        parts["modelData"]?.let {
            this.customModelData = it.toIntOrNull() ?: 0
        }
    }
}

open class ItemBuilder {

    class SkullTexture(val textures: String, val uuid: UUID)

    /**
     * 物品材质
     */
    var material: Material

    /**
     * 数量
     */
    var amount = 1

    /**
     * 附加值（损伤值）
     */
    var damage = 0

    /**
     * 展示名称
     */
    var name: String? = null

    /**
     * 描述
     */
    val lore = ArrayList<String>()

    /**
     * 标签
     */
    val flags = ArrayList<ItemFlag>()

    /**
     * 附魔
     */
    val enchants = HashMap<Enchantment, Int>()

    /**
     * 旗帜花纹
     */
    val patterns = ArrayList<Pattern>()

    /**
     * 颜色
     */
    var color: Color? = null

    /**
     * 药水效果
     */
    val potions = ArrayList<PotionEffect>()

    /**
     * 基础药水效果
     */
    var potionData: PotionData? = null

    /**
     * 生物类型
     */
    var spawnType: EntityType? = null

    /**
     * 头颅信息
     */
    var skullOwner: String? = null

    /**
     * 无法破坏
     */
    var isUnbreakable = false

    /**
     * CustomModelData
     */
    var customModelData = -1

    /**
     * 原始数据
     * 尝试修复自定义 nbt 失效的问题
     */
    var originMeta: ItemMeta? = null

    /**
     * 当构建完成时，做出最后修改
     */
    var finishing: (ItemStack) -> Unit = {}



    /**
     * 使其发光
     */
    fun shiny() {
        flags += ItemFlag.HIDE_ENCHANTS
        enchants[Enchantment.LURE] = 1
    }

    /**
     * 隐藏所有附加信息（赋予所有 ItemFlag）
     */
    fun hideAll() {
        flags.addAll(ItemFlag.values())
    }

    /**
     * 上色
     */
    fun colored() {
        if (name != null) {
            name = try {
                name!!.colored()
            } catch (ex: NoClassDefFoundError) {
                ChatColor.translateAlternateColorCodes('&', name!!)
            }
        }
        if (lore.isNotEmpty()) {
            val newLore = try {
                lore.colored()
            } catch (ex: NoClassDefFoundError) {
                lore.map { ChatColor.translateAlternateColorCodes('&', it) }
            }
            lore.clear()
            lore += newLore
        }
    }

    /**
     * 构建物品
     */
    open fun build(): ItemStack {
        val itemStack = ItemStack(material)
        itemStack.amount = amount
        if (damage != 0) {
            itemStack.durability = damage.toShort()
        }
        val itemMeta = originMeta ?: itemStack.itemMeta ?: return itemStack
        itemMeta.setDisplayName(name)
        itemMeta.lore = lore
        itemMeta.addItemFlags(*flags.toTypedArray())
        if (itemMeta is EnchantmentStorageMeta) {
            enchants.forEach { (e, lvl) -> itemMeta.addStoredEnchant(e, lvl, true) }
        } else {
            enchants.forEach { (e, lvl) -> itemMeta.addEnchant(e, lvl, true) }
        }
        when (itemMeta) {
            is LeatherArmorMeta -> {
                itemMeta.setColor(color)
            }
            is PotionMeta -> {
                potions.forEach { itemMeta.addCustomEffect(it, true) }
                if (color != null) {
                    itemMeta.color = color
                }
                if (potionData != null) {
                    itemMeta.basePotionData = potionData!!
                }
            }
            is SkullMeta -> {
                if (skullOwner != null) {
                    itemMeta.owner = skullOwner
                }
            }
        }
        try {
            itemMeta.isUnbreakable = isUnbreakable
        } catch (_: NoSuchMethodError) {
        }
        try {
            if (spawnType != null && itemMeta is SpawnEggMeta) {
                itemMeta.spawnedType = spawnType
            }
        } catch (ignored: NoClassDefFoundError) {
        }
        try {
            if (patterns.isNotEmpty() && itemMeta is BannerMeta) {
                patterns.forEach { itemMeta.addPattern(it) }
            }
        } catch (ignored: NoClassDefFoundError) {
        }
        try {
            if (customModelData != -1 && customModelData != 0) {
                itemMeta.setCustomModelData(customModelData)
            }
        } catch (ignored: Exception) {
        }
        itemStack.itemMeta = itemMeta
        finishing(itemStack)
        return itemStack
    }

    constructor(material: Material) {
        this.material = material
    }


    /**
     * 通过现有物品构建新的物品
     * 读取基本信息
     */
    constructor(item: ItemStack) {
        material = item.type
        amount = item.amount
        damage = item.durability.toInt()
        // 如果物品没有 ItemMeta 则不进行后续操作
        val itemMeta = item.itemMeta ?: return
        originMeta = itemMeta
        name = itemMeta.displayName
        lore += itemMeta.lore ?: emptyList()
        flags += itemMeta.itemFlags
        enchants += if (itemMeta is EnchantmentStorageMeta) {
            itemMeta.storedEnchants
        } else {
            itemMeta.enchants
        }
        when (itemMeta) {
            is LeatherArmorMeta -> {
                color = itemMeta.color
            }
            is PotionMeta -> {
                color = itemMeta.color
                potions += itemMeta.customEffects
                potionData = itemMeta.basePotionData
            }
            is SkullMeta -> {
                if (itemMeta.owner != null) {
                    skullOwner = itemMeta.owner
                }
               // XSkull.getSkinValue(itemMeta)?.let { skullTexture = it }
            }
        }
        customModelData = try {
            itemMeta.customModelData
        } catch (ignored: NoSuchFieldException) {
            -1
        }
        try {
            isUnbreakable = itemMeta.isUnbreakable
        } catch (ignored: NoSuchMethodError) {
        } catch (ignored: NoSuchMethodException){
            isUnbreakable = false
        }

        try {
            if (itemMeta is SpawnEggMeta && itemMeta.spawnedType != null) {
                spawnType = itemMeta.spawnedType
            }
        } catch (ignored: NoClassDefFoundError) {
        } catch (ignored: UnsupportedOperationException) {

        }
        try {
            if (itemMeta is BannerMeta && itemMeta.patterns.isNotEmpty()) {
                patterns += itemMeta.patterns
            }
        } catch (ignored: NoClassDefFoundError) {
        }
    }
}