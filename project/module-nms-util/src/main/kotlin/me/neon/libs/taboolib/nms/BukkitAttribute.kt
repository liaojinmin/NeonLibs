package me.neon.libs.taboolib.nms

import me.neon.libs.util.unsafeLazy
import org.bukkit.attribute.Attribute
import org.tabooproject.reflex.ReflexClass

/**
 * Attribute 映射类
 *
 * @author 坏黑
 * @since 2023/8/10 02:12
 */
enum class BukkitAttribute(val minecraftKey: String, val simplifiedKey: Array<String>) {

    /**
     * 最大生命值
     */
    MAX_HEALTH("generic.maxHealth", arrayOf("health", "maxHealth")),

    /**
     * 最大跟随距离
     */
    FOLLOW_RANGE("generic.followRange", arrayOf("follow", "followRange")),

    /**
     * 击退抗性
     */
    KNOCKBACK_RESISTANCE("generic.knockbackResistance", arrayOf("knockback", "knockbackResistance")),

    /**
     * 移动速度
     */
    MOVEMENT_SPEED("generic.movementSpeed", arrayOf("speed", "movementSpeed", "walkSpeed")),

    /**
     * 飞行速度
     */
    FLYING_SPEED("generic.flyingSpeed", arrayOf("flySpeed", "flyingSpeed")),

    /**
     * 攻击力
     */
    ATTACK_DAMAGE("generic.attackDamage", arrayOf("damage", "attackDamage")),

    /**
     * 击退
     */
    ATTACK_KNOCKBACK("generic.attackKnockback", arrayOf("damageKnockback", "attackKnockback")),

    /**
     * 攻速
     */
    ATTACK_SPEED("generic.attackSpeed", arrayOf("damageSpeed", "attackSpeed")),

    /**
     * 护甲
     */
    ARMOR("generic.armor", arrayOf("armor")),

    /**
     * 护甲韧性
     */
    ARMOR_TOUGHNESS("generic.armorToughness", arrayOf("toughness", "armorToughness")),

    /**
     * 幸运
     */
    LUCK("generic.luck", arrayOf("luck"));

    /**
     * 转换为 Bukkit Attribute
     */
    fun toBukkit(): Attribute {
        if (MinecraftVersion.isLower(MinecraftVersion.V1_9)) {
            throw Exception("Unsupported version")
        }
        return try {
            Attribute.valueOf("GENERIC_$name")
        } catch (e: Exception) {
            Attribute.valueOf(name)
        }
    }

    /**
     * 转换为 NMS Attribute
     */
    fun toNMS(): Any? {
        if (MinecraftVersion.isLower(MinecraftVersion.V1_9)) {
            throw Exception("Unsupported version")
        }
        if (MinecraftVersion.isLowerOrEqual(MinecraftVersion.V1_13)) {
            val getName = me.neon.libs.taboolib.nms.BukkitAttribute.Companion.classAttributeBase.getMethod("getName")
            for (field in me.neon.libs.taboolib.nms.BukkitAttribute.Companion.classAttributeBase.structure.fields) {
                val attribute = field.get(null)!!
                if (getName.invoke(attribute) == minecraftKey) {
                    return attribute
                }
            }
        } else {
            return me.neon.libs.taboolib.nms.BukkitAttribute.Companion.classGenericAttributes.getField(name).get(null)
        }
        return null
    }

    /**
     * 匹配属性
     */
    fun match(source: String): Boolean {
        return minecraftKey.equals(source, true) || simplifiedKey.any { it.equals(source, true) }
    }

    companion object {

        private val classAttributeBase by unsafeLazy { ReflexClass.of(nmsClass("AttributeBase")) }
        private val classGenericAttributes by unsafeLazy { ReflexClass.of(nmsClass("GenericAttributes")) }

        @JvmStatic
        fun parse(source: String): me.neon.libs.taboolib.nms.BukkitAttribute? {
            return values().firstOrNull { it.match(source) }
        }
    }

}