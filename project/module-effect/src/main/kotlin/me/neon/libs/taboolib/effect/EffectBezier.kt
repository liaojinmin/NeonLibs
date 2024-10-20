package me.neon.libs.taboolib.effect

import me.neon.libs.taboolib.effect.shape.NRankBezierCurve
import me.neon.libs.taboolib.effect.shape.ThreeRankBezierCurve
import me.neon.libs.taboolib.effect.shape.TwoRankBezierCurve
import org.bukkit.Location

/**
 * 创建一条二阶贝塞尔曲线
 *
 * @param p0 第一点
 * @param p1 第二点
 * @param p2 第三点
 * @param step 步长
 * @param period 特效周期(如果需要可以使用)
 */
fun createTwoRankBezierCurve(
    p0: Location,
    p1: Location,
    p2: Location,
    step: Double = 1.0,
    period: Long = 20,
    spawner: (p: Location) -> Unit = {}
): TwoRankBezierCurve {
    return TwoRankBezierCurve(p0, p1, p2, step, object : ParticleSpawner {
        override fun spawn(location: Location) {
            spawner(location)
        }
    }).also { it.period = period }
}

/**
 * 创建一条三阶贝塞尔曲线
 *
 * @param p0 第一点
 * @param p1 第二点
 * @param p2 第三点
 * @param p3 第四点
 * @param step 步长
 * @param period 特效周期(如果需要可以使用)
 */
fun createThreeRankBezierCurve(
    p0: Location,
    p1: Location,
    p2: Location,
    p3: Location,
    step: Double = 1.0,
    period: Long = 20,
    spawner: (p: Location) -> Unit = {}
): ThreeRankBezierCurve {
    return ThreeRankBezierCurve(p0, p1, p2, p3, step, object : ParticleSpawner {
        override fun spawn(location: Location) {
            spawner(location)
        }
    }).also { it.period = period }
}

/**
 * 创建一条N阶贝塞尔曲线
 *
 * @param points 点集合
 * @param step 步长
 * @param period 特效周期(如果需要可以使用)
 */
fun createNRankBezierCurve(
    points: List<Location>,
    step: Double = 1.0,
    period: Long = 20,
    spawner: (p: Location) -> Unit = {}
): NRankBezierCurve {
    return NRankBezierCurve(points, step, object : ParticleSpawner {
        override fun spawn(location: Location) {
            spawner(location)
        }
    }).also { it.period = period }
}

/**
 * 创建一条N阶贝塞尔曲线
 *
 * @param points 点集合
 * @param step 步长
 * @param period 特效周期(如果需要可以使用)
 */
fun createNRankBezierCurve(
    vararg points: Location,
    step: Double = 1.0,
    period: Long = 20,
    spawner: (p: Location) -> Unit = {}
): NRankBezierCurve {
    return NRankBezierCurve(points.toList(), step, object : ParticleSpawner {
        override fun spawn(location: Location) {
            spawner(location)
        }
    }).also { it.period = period }
}
