package me.neon.libs.script.action

import me.neon.libs.script.action.group.IGroup
import me.neon.libs.script.action.impl.Break
import me.neon.libs.script.action.impl.Delay
import org.bukkit.entity.Player
import org.bukkit.event.Event
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import javax.script.SimpleScriptContext

/**
 * NeonDesire
 * me.neon.desire.feature.action
 *
 * @author 老廖
 * @since 2024/4/29 19:16
 */
class ActionExecute(
    val player: Player,
    val event: Event? = null,
    val reacts: List<IGroup>,
    /**
     * 用于控制中断
     */
    private val atomic: AtomicBoolean = AtomicBoolean(false),
    private val factory: SimpleScriptContext,
) {

    //init {
        //println("  初始化 ActionExecute")
    //}


    fun eval(): CompletableFuture<Boolean> {
        //println("  try")
        try {
            if (atomic.get()) return CompletableFuture.completedFuture(false)
            for (group in reacts) {
                if (atomic.get()) return CompletableFuture.completedFuture(false)
                for (action in group.getActions(player, event, factory)) {
                    if (atomic.get()) return CompletableFuture.completedFuture(false)
                    if (action.option.evalChance()) {
                        //println("action: ${action.base.lowerName}")
                        when {
                            action.base is Break && action.option.evalCondition(player, event, factory) -> {
                                return CompletableFuture.completedFuture(false)
                            }
                            action.base is Delay -> {
                                val delay = action.base.getDelay(player, action.contents.stringContent(), factory) * 50
                                Thread.sleep(delay)
                            }
                            else -> action.execute(player, event, factory)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return CompletableFuture.completedFuture(false)
        }
        return CompletableFuture.completedFuture(true)
    }
}