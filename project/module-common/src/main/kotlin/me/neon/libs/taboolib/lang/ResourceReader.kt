@file:Suppress("DEPRECATION")

package me.neon.libs.taboolib.lang

import me.neon.libs.NeonLibsLoader
import me.neon.libs.taboolib.lang.type.TypeText
import me.neon.libs.taboolib.lang.type.TypeList
import me.neon.libs.util.YamlDsl
import me.neon.libs.util.asyncRunner
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.jar.JarFile

/**
 * TabooLib
 * me.neon.mail.libs.lang.ResourceReader
 *
 * @author sky
 * @since 2021/6/21 11:48 下午
 */
class ResourceReader(private val plugin: Plugin, pluginFile: File) {

    val files = HashMap<String, LanguageFile>()
    init {
        JarFile(pluginFile).use { jar ->
            jar.entries().iterator().forEachRemaining {
                if (it.name.startsWith("${Language.path}/") && it.name.endsWith(".yml")) {
                    val code = it.name.substringAfterLast('/').substringBeforeLast('.')

                    val source = jar.getInputStream(it).readBytes().toString(StandardCharsets.UTF_8)
                    val nodes = HashMap<String, Type>()

                    val sourceFile = YamlConfiguration()
                    sourceFile.loadFromString(source)

                    // 加载内存中的原件
                    loadNodes(sourceFile, nodes, code)

                    // 释放文件
                    val file = File(plugin.dataFolder,"${Language.path}/$code.yml")
                    if (!file.exists()) {
                        plugin.saveResource("${Language.path}/$code.yml", true)
                    }

                    val exists = HashMap<String, Type>()
                    // 加载文件
                    loadNodes(YamlConfiguration.loadConfiguration(file), exists, code)
                    // 检查缺失
                    val missingKeys = nodes.keys.filter { a -> !exists.containsKey(a) }

                    if (missingKeys.isNotEmpty()) {
                        // 更新文件
                        migrateFile(missingKeys, sourceFile, file)
                    }

                    nodes += exists
                    files[code] = LanguageFile(file, nodes).also { f ->
                        files[code] = f
                        // 文件变动监听
                        // TODO
                    }
                }
            }
        }
    }

    private fun loadNodes(file: YamlConfiguration, nodesMap: HashMap<String, Type>, code: String) {

        file.getKeys(false).forEach { node ->
            when (val obj = file[node]) {
                is String -> {
                    nodesMap[node] = TypeText(obj)
                }
                is List<*> -> {
                    nodesMap[node] = TypeList(obj.mapNotNull { sub ->
                        if (sub is Map<*, *>) {
                            loadNode(sub.map { it.key.toString() to it.value!! }.toMap(), code, node)
                        } else {
                            TypeText(sub.toString())
                        }
                    })
                }
                is ConfigurationSection -> {
                    val type = loadNode(obj.getValues(false).map { it.key to it.value!! }.toMap(), code, node)
                    if (type != null) {
                        nodesMap[node] = type
                    }
                }
                else -> {
                    plugin.logger.warning("Unsupported language node: $node ($code)")
                }
            }
        }
    }

    private fun loadNode(map: Map<String, Any>, code: String, node: String?): Type? {
        return if (map.containsKey("type") || map.containsKey("==")) {
            val type = (map["type"] ?: map["=="]).toString().lowercase()
            val typeInstance = Language.languageType[type]?.getDeclaredConstructor()?.newInstance()
            if (typeInstance != null) {
                typeInstance.init(map)
            } else {
               NeonLibsLoader.warning("Unsupported language type: $node > $type ($code)")
            }
            typeInstance
        } else {
            NeonLibsLoader.warning("Missing language type: $map ($code)")
            null
        }
    }


    @Suppress("DEPRECATION")
    private fun migrateFile(missing: List<String>, source: Configuration, file: File) {
        asyncRunner {
            //println("更新语言文件 -> $file")
            val append = ArrayList<String>()
            append += "# ------------------------- #"
            append += "#  UPDATE ${Language.dateFormat.format(System.currentTimeMillis())}  #"
            append += "# ------------------------- #"
            append += ""

            missing.forEach { key ->
                val obj = source[key]
                if (obj != null) {
                    append += YamlDsl.dumpAll(key, obj)
                }
            }
            file.appendText("\n${append.joinToString("\n")}")
        }
    }

}