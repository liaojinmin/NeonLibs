package me.neon.libs.util

import org.bukkit.configuration.Configuration
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.tabooproject.reflex.Reflex.Companion.setProperty
import java.io.File

/**
 * @作者: 老廖
 * @时间: 2023/5/12 5:54
 * @包: me.geek.regions.utils
 */
class YamlDsl(val yml: ConfigurationSection) {

    companion object {

        fun asSection(any: Any?): Configuration? {
            return when (any) {
                is Configuration -> return any
                is ConfigurationSection -> YamlConfiguration().apply { setProperty("root", any.root) }
                is Map<*, *> -> {
                    YamlConfiguration().also {
                        any.entries.forEach { entry ->
                            asSection(it.createSection(entry.key.toString(), entry.value as Map<*,*>))?.let { it1 ->
                                it.addDefaults(it1)
                            }
                        }
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

        fun dumpAll(key: String, value: Any?, space: Int = 2): String {
            return if (key.contains('.')) {
                "${key.substringBefore('.')}:\n${" ".repeat(space)}${dumpAll(key.substringAfter('.'), value, space + 2)}"
            } else {
                val dump = dump(value)
                when {
                    dump.startsWith("-") -> "$key:\n$dump"
                    value is List<*> && value.isEmpty() -> "$key: []"
                    value is Map<*, *> -> if (value.isEmpty()) "$key: {}" else "$key:\n$dump"
                    value is ConfigurationSection -> "$key:\n  ${dump.replace("\n", "\n  ")}"
                    else -> "$key: $dump"
                }
            }
        }

        fun dump(data: Any?): String {
            if (data == null) {
                return ""
            }
            var single = false
            val dump = YamlConfiguration()
            when (data) {
                is ConfigurationSection -> {
                    data.getValues(false).forEach { (path, value) -> dump[path] = value }
                }
                is Map<*, *> -> {
                    data.forEach { (k, v) -> dump[k.toString()] = v }
                }
                else -> {
                    single = true
                    dump["value"] = data
                }
            }
            val save = if (single) {
                dump.saveToString().substring("value:".length).trim().split('\n').toTypedArray()
            } else {
                dump.saveToString().trim().split('\n').toTypedArray()
            }
            return java.lang.String.join("\n", *save)
        }

        fun loadConfiguration(contents: String): YamlConfiguration {
            val config = YamlConfiguration()
            try {
                config.loadFromString(contents)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            return config
        }

        fun loadConfiguration(file: File): YamlConfiguration {
            val config = YamlConfiguration()
            try {
                config.load(file)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            return config
        }
    }

    infix fun String.to(any: Any?) {
        yml[this] = any
    }
    infix fun String.to(any: List<List<String>>) {
        if (any.size == 1) {
            yml[this] = any[0]
        } else {
            yml[this] = any
        }
    }

    infix fun String.to(action: YamlDsl.() -> Unit) = YamlDsl(yml.createSection(this)).apply {
        action(this)
    }.yml

}

fun yaml(action: YamlDsl.() -> Unit) = YamlDsl(YamlConfiguration()).apply {
    action(this)
}

fun YamlDsl.saveToFile(file: File) {
    (this.yml as FileConfiguration).save(file)
}

@Suppress("UNCHECKED_CAST")
fun <K, V> ConfigurationSection.getMap(path: String): Map<K, V> {
    val map = HashMap<K, V>()
    getConfigurationSection(path)?.let { section ->
        section.getKeys(false).forEach { key ->
            try {
                map[key as K] = section[key] as V
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
    }
    return map
}