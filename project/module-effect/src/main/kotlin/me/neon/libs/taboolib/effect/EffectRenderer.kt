package me.neon.libs.taboolib.effect

import me.neon.libs.taboolib.effect.renderer.GeneralEquationRenderer
import me.neon.libs.taboolib.effect.renderer.ParametricEquationRenderer
import me.neon.libs.taboolib.effect.renderer.PolarEquationRenderer
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.material.MaterialData
import org.bukkit.util.Vector

/**
 * 创建一个普通方程渲染器
 *
 * @param origin 原点
 * @param function 函数
 * @param minX 最小X
 * @param maxX 最大X
 * @param dx 每次增加的X
 * @param period 特效周期(如果需要可以使用)
 */
fun createGeneralEquationRenderer(
    origin: Location,
    function: (x: Double) -> Double,
    minX: Double = -5.0,
    maxX: Double = 5.0,
    dx: Double = 0.1,
    period: Long = 20,
    spawner: (p: Location) -> Unit = {}
): GeneralEquationRenderer {
    return GeneralEquationRenderer(origin, function, minX, maxX, dx, object : ParticleSpawner {
        override fun spawn(location: Location) {
            spawner(location)
        }
    }).also { it.period = period }
}

/**
 * 创建一个参数方程渲染器
 *
 * @param origin 原点
 * @param xFunction X函数
 * @param yFunction Y函数
 * @param zFunction Z函数
 * @param minT 最小T
 * @param maxT 最大T
 * @param dt 每次增加的T
 * @param period 特效周期(如果需要可以使用)
 */
fun createParametricEquationRenderer(
    origin: Location,
    xFunction: (x: Double) -> Double,
    yFunction: (y: Double) -> Double,
    zFunction: (z: Double) -> Double = { 0.0 },
    minT: Double = -5.0,
    maxT: Double = 5.0,
    dt: Double = 0.1,
    period: Long = 20,
    spawner: (p: Location) -> Unit = {}
): ParametricEquationRenderer {
    return ParametricEquationRenderer(origin, xFunction, yFunction, zFunction, minT, maxT, dt, object :
        ParticleSpawner {
        override fun spawn(location: Location) {
            spawner(location)
        }
    }).also { it.period = period }
}

/**
 * 创建一个极坐标方程渲染器
 *
 * @param origin 原点
 * @param rFunction R函数
 * @param minT 最小T
 * @param maxT 最大T
 * @param dt 每次增加的T
 * @param period 特效周期(如果需要可以使用)
 */
fun createPolarEquationRenderer(
    origin: Location,
    rFunction: (r: Double) -> Double,
    minT: Double = -5.0,
    maxT: Double = 5.0,
    dt: Double = 0.1,
    period: Long = 20,
    spawner: (p: Location) -> Unit = {}
): PolarEquationRenderer {
    return PolarEquationRenderer(origin, rFunction, minT, maxT, dt, object : ParticleSpawner {
        override fun spawn(location: Location) {
            spawner(location)
        }
    }).also { it.period = period }
}

fun ProxyParticle.sendTo(location: Location, range: Double = 128.0, offset: Vector = Vector(0, 0, 0), count: Int = 1, speed: Double = 0.0, data: ProxyParticle.Data? = null) {
    Bukkit.getOnlinePlayers().filter { it.world == location.world && it.location.distance(location) <= range }.forEach {
        this.send(it, location, offset, count, speed, data)
    }
}
