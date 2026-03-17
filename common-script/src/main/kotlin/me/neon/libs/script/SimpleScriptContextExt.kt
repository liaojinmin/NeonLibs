package me.neon.libs.script

import org.bukkit.entity.Player
import javax.script.ScriptContext
import javax.script.SimpleScriptContext

/**
 * NeonDesire
 * me.neon.desire.utils
 *
 * @author 老廖
 * @since 2025/12/10 01:05
 */

fun SimpleScriptContext.put(key: String, value: Any) {
    setAttribute(key, value, ScriptContext.ENGINE_SCOPE)
}

fun SimpleScriptContext.get(key: String): Any? {
    return getAttribute(key, ScriptContext.ENGINE_SCOPE)
}

fun SimpleScriptContext.remove(key: String): Any? {
    return removeAttribute(key, ScriptContext.ENGINE_SCOPE)
}

fun SimpleScriptContext.getPlayer(): Player? {
    return getAttribute("player", ScriptContext.ENGINE_SCOPE) as? Player
}
