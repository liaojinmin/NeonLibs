package me.neon.libs.taboolib.effect

import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.material.MaterialData
import org.bukkit.util.Vector
import java.awt.Color
import java.util.*

/**
 * @Author sky
 * @Since 2020-08-04 18:36
 */
enum class ProxyParticle(vararg val aliases: String) {

    EXPLOSION_NORMAL("EXPLOSION"),
    EXPLOSION_LARGE("LARGE_EXPLOSION"),
    EXPLOSION_HUGE("HUGE_EXPLOSION"),
    FIREWORKS_SPARK("@", "FIREWORK"),
    WATER_BUBBLE("@", "BUBBLE"),
    WATER_SPLASH("@", "SPLASH"),
    WATER_WAKE("@"),
    SUSPENDED("@"),
    SUSPENDED_DEPTH("@"),
    CRIT("CRITICAL_HIT"),
    CRIT_MAGIC("MAGIC_CRITICAL_HIT", "ENCHANTED_HIT"),
    SMOKE_NORMAL("SMOKE"),
    SMOKE_LARGE("LARGE_SMOKE"),
    SPELL("@"),
    SPELL_INSTANT("INSTANT_SPELL"),
    SPELL_MOB("MOB_SPELL"),
    SPELL_MOB_AMBIENT("AMBIENT_MOB_SPELL"),
    SPELL_WITCH("WITCH_SPELL"),
    DRIP_WATER("@"),
    DRIP_LAVA("@"),
    VILLAGER_ANGRY("HAPPY_VILLAGER"),
    VILLAGER_HAPPY("ANGRY_VILLAGER"),
    TOWN_AURA("@"),
    NOTE("@"),
    PORTAL("@"),
    ENCHANTMENT_TABLE("ENCHANTING_GLYPHS"),
    FLAME("@"),
    LAVA("@"),
    CLOUD("@"),
    REDSTONE("REDSTONE_DUST", "DUST"),
    SNOWBALL("@", "ITEM_SNOWBALL"),
    SNOW_SHOVEL("@"),
    SLIME("@", "ITEM_SLIME"),
    HEART("@"),
    BARRIER("@"),
    ITEM_CRACK("@"),
    BLOCK_CRACK("@"),
    BLOCK_DUST("@"),
    WATER_DROP("@"),
    MOB_APPEARANCE("GUARDIAN_APPEARANCE", "ELDER_GUARDIAN"),
    DRAGON_BREATH("@"),
    END_ROD("@"),
    DAMAGE_INDICATOR("@"),
    SWEEP_ATTACK("@"),
    FALLING_DUST("@"),
    TOTEM("@"),
    SPIT("@"),
    SQUID_INK("@"),
    BUBBLE_POP("@"),
    CURRENT_DOWN("@"),
    BUBBLE_COLUMN_UP("@"),
    NAUTILUS("@"),
    DOLPHIN("@"),
    SNEEZE("@"),
    CAMPFIRE_COSY_SMOKE("@"),
    CAMPFIRE_SIGNAL_SMOKE("@"),
    COMPOSTER("@"),
    FLASH("@"),
    FALLING_LAVA("@"),
    LANDING_LAVA("@"),
    FALLING_WATER("@"),
    DRIPPING_HONEY("@"),
    FALLING_HONEY("@"),
    LANDING_HONEY("@"),
    FALLING_NECTAR("@"),
    SOUL_FIRE_FLAME("@"),
    ASH("@"),
    CRIMSON_SPORE("@"),
    WARPED_SPORE("@"),
    SOUL("@"),
    DRIPPING_OBSIDIAN_TEAR("@"),
    FALLING_OBSIDIAN_TEAR("@"),
    LANDING_OBSIDIAN_TEAR("@"),
    REVERSE_PORTAL("@"),
    WHITE_ASH("@"),
    LIGHT("~"),
    DUST_COLOR_TRANSITION("~"),
    VIBRATION("~"),
    FALLING_SPORE_BLOSSOM("~"),
    SPORE_BLOSSOM_AIR("~"),
    SMALL_FLAME("~"),
    SNOWFLAKE("~"),
    DRIPPING_DRIPSTONE_LAVA("~"),
    FALLING_DRIPSTONE_LAVA("~"),
    DRIPPING_DRIPSTONE_WATER("~"),
    FALLING_DRIPSTONE_WATER("~"),
    GLOW_SQUID_INK("~"),
    GLOW("~"),
    WAX_ON("~"),
    WAX_OFF("~"),
    ELECTRIC_SPARK("~"),
    SCRAPE("~");

    fun send(player: Player, location: Location, offset: Vector = Vector(0, 0, 0), count: Int = 1, speed: Double = 0.0, data: Data? = null) {
        player.sendParticle(this, location, offset, count, speed, data)
    }

    fun Player.sendParticle(particle: ProxyParticle, location: Location, offset: Vector, count: Int, speed: Double, data: Data?) {
        // 获取粒子
        val bukkitParticle = runCatching { Particle.valueOf(particle.name) }.getOrNull() ?: error("Unsupported particle ${particle.name}")
        // 获取粒子数据
        val bukkitData: Any? = when (data) {
            is DustTransitionData -> {
                Particle.DustOptions(
                    org.bukkit.Color.fromRGB(data.color.red, data.color.green, data.color.blue),
                    data.size
                )
            }

            is DustData -> {
                Particle.DustOptions(org.bukkit.Color.fromRGB(data.color.red, data.color.green, data.color.blue), data.size)
            }

            is ItemData -> {
                val item = ItemStack(Material.valueOf(data.material))
                val itemMeta = item.itemMeta!!
                itemMeta.setDisplayName(data.name)
                itemMeta.lore = data.lore
                try {
                    itemMeta.setCustomModelData(data.customModelData)
                } catch (ignored: NoSuchMethodError) {
                }
                item.itemMeta = itemMeta
                if (data.data != 0) {
                    item.durability = data.data.toShort()
                }
                item
            }

            is BlockData -> {
                if (bukkitParticle.dataType == MaterialData::class.java) {
                    MaterialData(Material.valueOf(data.material), data.data.toByte())
                } else {
                    Material.valueOf(data.material).createBlockData()
                }
            }

            is VibrationData -> {
                Vibration(
                    data.origin, when (val destination = data.destination) {
                        is VibrationData.LocationDestination -> {
                            Vibration.Destination.BlockDestination(destination.location)
                        }
                        is VibrationData.EntityDestination -> {
                            Vibration.Destination.EntityDestination(Bukkit.getEntity(destination.entity)!!)
                        }
                        else -> error("out of case")
                    }, data.arrivalTime
                )
            }

            else -> null
        }
        spawnParticle(bukkitParticle, location, count, offset.x, offset.y, offset.z, speed, bukkitData)
    }

    interface Data

    open class DustData(val color: Color, val size: Float) : Data

    class DustTransitionData(color: Color, val toColor: Color, size: Float) : DustData(color, size)

    class ItemData(
        val material: String,
        val data: Int = 0,
        val name: String = "",
        val lore: List<String> = emptyList(),
        val customModelData: Int = -1,
    ) : Data

    class BlockData(val material: String, val data: Int = 0) : Data

    class VibrationData(val origin: Location, val destination: Destination, val arrivalTime: Int) : Data {

        interface Destination

        class EntityDestination(val entity: UUID) : Destination

        class LocationDestination(val location: Location) : Destination
    }
}