package me.neon.libs.script.action.group

import org.bukkit.configuration.Configuration
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.tabooproject.reflex.Reflex.Companion.setProperty

@Suppress("UNCHECKED_CAST")
internal enum class Property(val default: String, val regex: Regex) {

    /**
     * 优先级
     */
    PRIORITY("priority", "pri(ority)?s?"),

    /**
     * 需求条件
     */
    CONDITION("condition", "(require(ment)?|cond(ition)?)s?"),

    /**
     * 执行动作集合
     */
    ACTIONS("actions", "(list|action|execute)s?"),

    /**
     * (拒绝) 动作
     */
    DENY_ACTIONS("deny-actions", "deny(-)?(list|action|execute)?s?");

    constructor(default: String, regex: String) : this(default, Regex("(?i)$regex"))

    override fun toString(): String = default

    fun ofList(conf: Configuration?): List<Any> {
        val any = of(conf) ?: return mutableListOf()
        val result = mutableListOf<Any>()
        when (any) {
            is List<*> -> any.forEach { it?.let { any -> result.add(any) } }
            else -> result.add(any.toString())
        }
        return result
    }

    private fun of(conf: Configuration?, def: Any? = null): Any? {
        return conf?.get(getKey(conf)) ?: def
    }

    fun getKey(conf: Configuration): String {
        return conf.getKeys(false).firstOrNull { it.matches(this.regex) } ?: this.default
    }

    companion object {

        fun asSection(any: Any?): Configuration? {
            return when (any) {
                is Configuration -> return any
                is ConfigurationSection -> YamlConfiguration().apply { setProperty("root", any.root) }
                is Map<*, *> -> {
                    YamlConfiguration().also {
                        any.entries.forEach { entry -> it[entry.key.toString()] = entry.value }
                    }
                }
                is List<*> -> {
                    YamlConfiguration().also {
                        any.forEach { a ->
                            val args = a.toString().split(Regex(":"), 2)
                            if (args.size == 2) it[args[0]] = args[1]
                        }
                    }
                }
                else -> null
            }
        }

    }

}