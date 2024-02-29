package me.neon.libs.script

import org.bukkit.Bukkit
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
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

    private val scriptEngine: ScriptEngine by lazy {
        scriptEngineFactory.scriptEngine
    }

    private val cacheCompiled = ConcurrentHashMap<String, CompiledScript>()

    private val persistBindings = ConcurrentHashMap<String, Any>()

    private val persistFunctions = ConcurrentHashMap<String, Function<Any, Any>>()

    init {
        persistBindings["server"] = Bukkit.getServer()
        persistBindings["utils"] = JavaScriptUtils()
    }

    fun registerPersistFunction(name: String, function: Function<Any, Any>) {
        persistFunctions[name] = function
    }

    fun registerPersistBindings(bindings: Map<String, Any>) {
        persistBindings.putAll(bindings)
    }

    fun clearCompiled() {
        cacheCompiled.clear()
    }

    fun runJS(script: String, variables: Map<String?, Any?>?): CompletableFuture<Any> {
        return try {
            CompletableFuture.completedFuture(runScript(script, true, variables) { block: SimpleScriptContext ->
                registerFunction(
                    block, { obj: Any? -> println(obj) }, "println"
                )
            })
            } catch (e: ScriptException) {
                throw RuntimeException(e)
            }
        }

    private fun registerFunction(context: SimpleScriptContext, block: Function<Any?, Any>?, vararg names: String?) {
        for (name in names) {
            context.setAttribute(name, block, ScriptContext.ENGINE_SCOPE)
        }
    }

    @Throws(ScriptException::class)
    fun runScript(
        script: String,
        cache: Boolean,
        bindings: Map<String?, Any?>?,
        block: Consumer<SimpleScriptContext>
    ): Any? {
        val context = SimpleScriptContext()
        val b = SimpleBindings(persistBindings)
        b.putAll(bindings!!)
        context.setBindings(b, ScriptContext.ENGINE_SCOPE)
        persistFunctions.forEach { (key: String?, value: Function<Any, Any>?) ->
            context.setAttribute(
                key,
                value,
                ScriptContext.ENGINE_SCOPE
            )
        }
        block.accept(context)
        // 编译脚本
        val compiledScript: CompiledScript = if (cache) {
            cacheCompiled.computeIfAbsent(script) { a: String? ->
                try {
                    return@computeIfAbsent (scriptEngine as Compilable).compile(a)
                } catch (e: ScriptException) {
                    throw RuntimeException(e)
                }
            }
        } else {
            (scriptEngine as Compilable).compile(script)
        }
        // 运行
        return compiledScript.eval(context)
    }
}