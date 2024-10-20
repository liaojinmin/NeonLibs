package me.neon.libs.taboolib.effect.wing.impl

import me.neon.libs.taboolib.effect.ProxyParticle
import me.neon.libs.taboolib.effect.sendTo
import me.neon.libs.taboolib.effect.wing.WingRender
import org.bukkit.Particle
import org.bukkit.entity.Player

/**
 * 使用粒子翅膀工具制作的一个翅膀动画demo
 * @author Soldier
 */
@Suppress("SpellCheckingInspection")
class AngelWing {

    /**
     * 这里需要指定该特效的height和width
     */
    val renderer = WingRender(7, 5)

    init {
        renderer.add(" a   ")
        renderer.add(" aa  ")
        renderer.add(" aaa ")
        renderer.add("  aaa")
        renderer.add("  aaa")
        renderer.add(" aaaa")
        renderer.add(" aaa ")
        renderer.add(" a   ")
        renderer.set('a') {
            ProxyParticle.FLAME.sendTo(it)
        }
    }

    /**
     * 从0-10传入frame，即可渲染出一个完整的翅膀粒子动画
     */
    fun send(player: Player, frame: Int) {
        renderer.render(player, frame, 0.0, 0.5, 0.0)
    }
}