package me.neon.libs.script.action

import me.neon.libs.script.JavaScriptHandle
import me.neon.libs.script.action.option.Option
import me.neon.libs.script.action.option.OptionType
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.plugin.Plugin
import javax.script.SimpleScriptContext

/**
 * NeonFlash
 * me.neon.flash.api.action
 *
 * @author 老廖
 * @since 2024/3/20 13:24
 */
abstract class ActionBase {

    val lowerName = javaClass.simpleName.lowercase()

    private val defRegex = "${javaClass.simpleName.replace("(?!^)([A-Z])".toRegex(), ".*?\$1").lowercase()}.*?".toRegex()

    open val regex get() = defRegex

    abstract val plugin: Plugin

    abstract fun onExecute(player: Player, contents: ActionContext, scriptContext: SimpleScriptContext, event: Event?)

    open fun readContents(contents: Any): ActionContext {
        return ActionContext(contents)
    }

    private fun isParsable(baseContent: String): Boolean {
        return JavaScriptHandle.containsPlaceholder(baseContent)
                || baseContent.contains("&")
                || baseContent.contains("$")
    }

    fun Player.parse(context: String, factory: SimpleScriptContext): String {
        return parseContext(this, context, factory)
    }

    fun parseContext(player: Player, context: String, factory: SimpleScriptContext): String {
        return if (isParsable(context)) {
            /*
            if (context.startsWith("\$js")) {
                JavaScriptHandle.parserJS.replaceNested(JavaScriptHandle.placeholderParser.invoke(player, context, factory)) {
                    println("value $this")
                    JavaScriptHandle.runJS(this@replaceNested, factory).get().toString()
                }
            } else {
                JavaScriptHandle.placeholderParser.invoke(
                    player,
                    context,
                    factory
                )
            }
             */
            JavaScriptHandle.placeholderParser.invoke(
                player,
                context,
                factory
            )
        } else {
            context
        }
    }

    fun String.parseContextSplit(
        player: Player,
        factory: SimpleScriptContext,
        vararg delimiters: String = arrayOf("\\n", "\\r")
    ): List<String> {
        return parseContext(player, this, factory).split(*delimiters)
    }

    fun ofEntry(contents: Any? = null, option: Option = Option()): ActionEntry {
        return ActionEntry(this, readContents(contents ?: ""), option)
    }

    fun ofOption(string: String): Pair<String, Option> {
        var content = string
        val options = mutableMapOf<OptionType, String>()

        OptionType.values().forEach {
            it.regex.find(content)?.let { find ->
                val value = find.groupValues.getOrElse(it.group) { "" }
                options[it] = value
                content = it.regex.replace(content, "")
            }
        }
        return content.removePrefix(" ") to Option(options)
    }

}