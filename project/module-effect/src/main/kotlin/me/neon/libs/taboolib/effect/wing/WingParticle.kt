package me.neon.libs.taboolib.effect.wing

import org.bukkit.Particle
import org.bukkit.util.Vector

/**
 * 表示一个翅膀的某个粒子效果
 */
class WingParticle(var particle: Particle, var hasColor: Boolean = false) {

    var r = -1.0
    var g = -1.0
    var b = -1.0

    constructor(particle: Particle, r: Int, g: Int, b: Int) : this(particle, true) {
        this.r = r / 255.0
        this.g = g / 255.0
        this.b = b / 255.0
        hasColor = true
    }

    fun getOffset(): Vector {
        return if (hasColor) {
            Vector(r, g, b)
        } else {
            Vector(0, 0, 0)
        }
    }
}