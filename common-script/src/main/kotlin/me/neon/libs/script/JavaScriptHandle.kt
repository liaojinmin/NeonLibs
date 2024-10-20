package me.neon.libs.script

import me.neon.libs.script.action.ActionBase
import me.neon.libs.script.action.group.ExecuteGroup
import me.neon.libs.script.action.group.IGroup
import me.neon.libs.script.action.impl.*
import me.neon.libs.taboolib.chat.HexColor.colored
import me.neon.libs.util.VariableReader
import me.neon.libs.util.replacePlaceholder
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.plugin.Plugin
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function
import javax.script.*

/**
 * NeonLibs
 * me.neon.libs.api.script
 *
 * @author 老廖
 * @since 2024/2/21 22:10
 */

object JavaScriptHandle {

    private val scriptEngineFactory by lazy {
        try {
            Class.forName("jdk.nashorn.api.scripting.NashornScriptEngineFactory").getDeclaredConstructor().newInstance() as ScriptEngineFactory
        } catch (ex: ClassNotFoundException) {
            NashornScriptEngineFactory()
        }
    }

    private val scriptEngine: ScriptEngine by lazy { scriptEngineFactory.scriptEngine }

    private val cacheCompiled: ConcurrentHashMap<String, CompiledScript> = ConcurrentHashMap()

    private val globalRegistries: ConcurrentHashMap<String, ActionBase> =  ConcurrentHashMap()

    private val pluginRegistries: ConcurrentHashMap<Plugin, ConcurrentHashMap<String, ActionBase>> =  ConcurrentHashMap()

    private val placeholderSet: MutableSet<(Player?, String, SimpleScriptContext?) -> String> = mutableSetOf()

    private val globalContext: SimpleScriptContext = SimpleScriptContext().also {
        it.setAttribute("NeonLibs", true, ScriptContext.ENGINE_SCOPE)
        it.setAttribute("conditionParser", Function { any: String? ->
            if (any == null) return@Function false
            return@Function ConditionHandle.parseCondition(any)
        }, ScriptContext.ENGINE_SCOPE)

        it.setAttribute("calculator", Function { any: String? ->
            if (any.isNullOrEmpty()) {
                return@Function "0"
            }
            return@Function CalculatorHandle.getResult(any)
        }, ScriptContext.ENGINE_SCOPE)

        it.setAttribute("server", Bukkit.getServer(), ScriptContext.ENGINE_SCOPE)
        it.setAttribute("utils", JavaScriptUtils(), ScriptContext.ENGINE_SCOPE)
        it.setAttribute("randomUtils", RandomUtils(), ScriptContext.ENGINE_SCOPE)
    }

    val ignoreAttributes = listOf("NeonLibs", "conditionParser", "calculator", "server", "utils", "randomUtils")

    val TRUE = Regex("true|yes|on")

    val SENTENCE =  Regex("`(.+?)`")

    val PLACEHOLDER_API = Regex("(%)(.+?)(%)|(?!\\{\")((\\{)(.+?)(}))")

    val jsVariableReader = VariableReader("\$js{", "}")

    val conditionParser: (Player?, Event?, String, SimpleScriptContext?) -> Boolean = { player: Player?, event, text: String, context: SimpleScriptContext? ->
        //println("条件样式 $text")
        if (text.isEmpty() || TRUE.matches(text)) {
            true
        } else {
            Bukkit.getServer().consoleSender.sendMessage()
            // 不使用 js 解析条件
            if (text.startsWith('*')) {
                ConditionHandle.parseCondition(text)
            } else {
                val back = if (context == null) {
                    runJS(player, event, text, SimpleScriptContext())
                } else {
                    runJS(player, event, text, context)
                }
                //println("条件样式返回 ${back.get()}")
                TRUE.matches(back.get().toString())
            }
        }
    }

    val placeholderParser: (Player?, String, SimpleScriptContext?) -> String = { p, s, factory ->
        var value = s.colored()
        placeholderSet.forEach {
            value = it.invoke(p, value, factory)
        }
        if (value.startsWith('$') || value.startsWith("\$js")) {
            value = jsVariableReader.replaceNested(value) {
                runJS(this@replaceNested, factory ?: globalContext).join().toString()
            }
            //value = runJS(value, factory ?: globalContext).get().toString()
        }
        if (p != null) {
            value.replacePlaceholder(p)
        } else value
    }

    init {
        register(Delay(), Break(), Cancel(), JavaScript())
        register(Actionbar(), Command(), Console(), Tell(), Title(), Sound())
    }

    fun containsPlaceholder(string: String): Boolean {
        return PLACEHOLDER_API.find(string) != null
    }

    fun parseBoolean(any: Any?): Boolean {
        if (any == null) {
            return false
        }
        return any.toString().lowercase().matches(TRUE)
    }

    fun loaderExecuteGroup(path: String, root: ConfigurationSection?): ExecuteGroup? {
        root ?: return null
        return ExecuteGroup(IGroup.ofGroups(root.get(path)))
    }

    fun loaderExecuteGroup(root: Any?): ExecuteGroup? {
        root ?: return null
        return ExecuteGroup(IGroup.ofGroups(root))
    }

    fun loaderGroup(root: ConfigurationSection?): MutableMap<String, ExecuteGroup> {
        val map = mutableMapOf<String, ExecuteGroup>()
        root?.getKeys(false)?.forEach {
            map[it] = ExecuteGroup(IGroup.ofGroups(root.get(it)))
        }
        return map
    }

    fun registerPlaceholderParse(func: (Player?, String, SimpleScriptContext?) -> String) {
        placeholderSet.add(func)
    }

    fun register(global: Boolean = false, vararg bases: ActionBase) {
        if (global) {
            bases.forEach {
                globalRegistries[it.lowerName] = it
            }
        }
        bases.forEach {
            pluginRegistries.computeIfAbsent(it.plugin) { ConcurrentHashMap() }[it.lowerName] = it
        }
    }

    fun register(vararg bases: ActionBase) {
        register(true, *bases)
    }

    fun unregister(global: Boolean = false, vararg bases: ActionBase) {
        if (global) {
            bases.forEach {
                globalRegistries.remove(it.lowerName)
            }
        }
        bases.forEach {
            val map = pluginRegistries[it.plugin]
            if (map != null) {
                map.remove(it.lowerName)
                if (map.isEmpty()) {
                    pluginRegistries.remove(it.plugin)
                }
            }

        }
    }

    fun unregister(vararg bases: ActionBase) {
        unregister(true, *bases)
    }

    fun getAction(key: String): ActionBase? {
        var action = globalRegistries.values.find { key.lowercase() == it.lowerName || it.regex.matches(key.lowercase()) }
        if (action == null) {
            // 取每个插件容器
            for (map in pluginRegistries) {
                action = map.value.values.find { key.lowercase() == it.lowerName || it.regex.matches(key.lowercase()) }
                if (action != null) break
            }
        }
        return action
    }

    fun registerPersistBindings(map: MutableMap<String, Any>) {
        for (a in ignoreAttributes) {
            if (map.containsKey(a)) {
                error("禁止替换已存在的基本属性... by registerPersistBindings")
            }
        }
        globalContext.getBindings(ScriptContext.ENGINE_SCOPE).putAll(map)
    }

    fun margeContext(context: SimpleScriptContext) {
        // 不要重复合并
        if (context.getAttribute("NeonLibs") != null) return
        context.getBindings(ScriptContext.ENGINE_SCOPE).putAll(globalContext.getBindings(ScriptContext.ENGINE_SCOPE))
    }

    fun runJS(script: String, context: SimpleScriptContext): CompletableFuture<Any> {
        return runJS(null, null, script, context)
    }

    fun runJS(player: Player?, event: Event?, script: String, context: SimpleScriptContext): CompletableFuture<Any> {
        margeContext(context)
        val bind = context.getBindings(ScriptContext.ENGINE_SCOPE)
        if (player != null) {
            bind["player"] = player
        }
        if (event != null) {
            bind["event"] = event
        }
        return eval(script, context)
    }

    fun clearCompiled() {
        cacheCompiled.clear()
    }

    fun getScriptEngineInvocable(): Invocable {
        return scriptEngine as Invocable
    }

    @Throws(ScriptException::class)
    fun compiledScript(
        script: String,
    ): CompiledScript {
        return (scriptEngine as Compilable).compile(script)
    }

    @Throws(ScriptException::class)
    fun compiledScriptAndSave(
        script: String,
    ): CompiledScript {
        return (scriptEngine as Compilable).compile(script).also { cacheCompiled[script] = it }
    }

    private fun eval(script: String, context: SimpleScriptContext): CompletableFuture<Any> {
        val compiled = cacheCompiled.computeIfAbsent(script) {
            try {
                return@computeIfAbsent compiledScript(script)
            } catch (e: ScriptException) {
                throw RuntimeException(e)
            }
        }
        return CompletableFuture.completedFuture(compiled.eval(context))
    }

}