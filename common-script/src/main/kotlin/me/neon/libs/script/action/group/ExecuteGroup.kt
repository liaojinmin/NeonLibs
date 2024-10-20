package me.neon.libs.script.action.group

import me.neon.libs.script.action.ActionEntry
import me.neon.libs.script.action.ActionExecute
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import javax.script.SimpleScriptContext

/**
 * NeonFlash
 * me.neon.flash.api.action.group
 *
 * @author 老廖
 * @since 2024/3/22 21:57
 */
data class ExecuteGroup(
    internal var reacts: List<IGroup>
) {

    init {
        if (reacts.isNotEmpty()) {
            reacts = reacts.sortedBy { it.priority }
        }
    }

    fun size(): Int {
        return reacts.size
    }

    fun eval(player: Player, factory: SimpleScriptContext, stop: AtomicBoolean): CompletableFuture<Boolean> {
        if (reacts.isEmpty()) return CompletableFuture.completedFuture(true)
        return if (Bukkit.isPrimaryThread()) {
            CompletableFuture.supplyAsync {
                ActionExecute(player, null, reacts, stop, factory).eval()
                    .join()
            }
        } else {
            ActionExecute(player, null, reacts, stop, factory).eval()
        }
    }

    fun eval(player: Player, factory: SimpleScriptContext, event: Event? = null): CompletableFuture<Boolean> {
        if (reacts.isEmpty()) return CompletableFuture.completedFuture(true)
        //println("准备运行")
        return if (Bukkit.isPrimaryThread()) {
            //println("  异步线程运行")
            CompletableFuture.supplyAsync {
                //println("  正在取值")
                ActionExecute(player, null, reacts, AtomicBoolean(false), factory).eval()
                    .join()
            }
        } else {
            ActionExecute(player, event, reacts, AtomicBoolean(false), factory).eval()
        }
    }

    private fun getActions(player: Player?, event: Event?, factory: SimpleScriptContext): List<ActionEntry> {
        //println("取动作列表")
        return mutableListOf<ActionEntry>().also { a ->
            reacts.forEach {
                //println("    group ${it::class.java}")
                a.addAll(it.getActions(player, event, factory))
            }
            //println("取动作列表完成")
        }
    }


}