package me.neon.libs.taboolib.configuration.visitor

import me.neon.libs.NeonLibsLoader
import me.neon.libs.NeonLibsServiceAPI
import me.neon.libs.core.LifeCycle
import me.neon.libs.core.env.RuntimeDependencies
import me.neon.libs.core.env.RuntimeDependency
import me.neon.libs.core.inject.ClassVisitor
import me.neon.libs.core.inject.Instance
import me.neon.libs.core.inject.Visitor
import me.neon.libs.taboolib.configuration.ConfigFile
import me.neon.libs.taboolib.configuration.ConfigNodeFile
import me.neon.libs.taboolib.configuration.Configuration
import me.neon.libs.taboolib.configuration.YamlFile
import me.neon.libs.taboolib.configuration.annotation.Config
import org.bukkit.plugin.Plugin
import org.tabooproject.reflex.ClassField
import java.io.File
import java.util.function.Supplier

/*
@RuntimeDependencies(
    RuntimeDependency(
        "!org.yaml:snakeyaml:2.2",
        test = "!org.yaml.snakeyaml_2_2.Yaml",
        relocate = ["!org.yaml.snakeyaml", "!org.yaml.snakeyaml_2_2"]
    ),
    RuntimeDependency(
        "!com.typesafe:config:1.4.3",
        test = "!com.typesafe.config_1_4_3.Config",
        relocate = ["!com.typesafe.config", "!com.typesafe.config_1_4_3"]
    ),
    RuntimeDependency(
        "!com.electronwill.night-config:core:3.6.7",
        test = "!com.electronwill.nightconfig_3_6_7.core.Config",
        relocate = ["!com.electronwill.nightconfig", "!com.electronwill.nightconfig_3_6_7", "!com.typesafe.config", "!com.typesafe.config_1_4_3"]
    ),
    RuntimeDependency(
        "!com.electronwill.night-config:toml:3.6.7",
        test = "!com.electronwill.nightconfig_3_6_7.toml.TomlFormat",
        relocate = ["!com.electronwill.nightconfig", "!com.electronwill.nightconfig_3_6_7", "!com.typesafe.config", "!com.typesafe.config_1_4_3"]
    ),
    RuntimeDependency(
        "!com.electronwill.night-config:json:3.6.7",
        test = "!com.electronwill.nightconfig_3_6_7.json.JsonFormat",
        relocate = ["!com.electronwill.nightconfig", "!com.electronwill.nightconfig_3_6_7", "!com.typesafe.config", "!com.typesafe.config_1_4_3"]
    ),
    RuntimeDependency(
        "!com.electronwill.night-config:hocon:3.6.7",
        test = "!com.electronwill.nightconfig_3_6_7.hocon.HoconFormat",
        relocate = ["!com.electronwill.nightconfig", "!com.electronwill.nightconfig_3_6_7", "!com.typesafe.config", "!com.typesafe.config_1_4_3"]
    )
)

 */
@Visitor
@Instance
class ConfigVisitor : ClassVisitor(1) {

    init {
        NeonLibsLoader.info("正在初始化 ConfigVisitor 注入器...")
    }

    override fun visitField(
        plugin: Plugin,
        field: ClassField,
        clazz: Class<*>,
        instance: Supplier<*>?
    ) {
        if (field.isAnnotationPresent(Config::class.java)) {
            val configAnno = field.getAnnotation(Config::class.java)
            val name = configAnno.property("value", "config.yml")
            if (files.containsKey(name)) {
                field.set(instance?.get(), files[name]!!.configuration)
            } else {
                val file = File(plugin.dataFolder, name)
                if (!file.exists()) {
                    plugin.saveResource(name, true)
                }
                // 兼容模式加载
                val conf = if (field.fieldType == ConfigFile::class.java) {
                    YamlFile.loadConfiguration(file)
                } else {
                    Configuration.loadFromFile(file, concurrent = configAnno.property("concurrent", true))
                }
                // 赋值
                field.set(instance?.get(), conf)
                // 自动重载
                /*
                if (configAnno.property("autoReload", false)) {
                    FileWatcher.INSTANCE.addSimpleListener(file) {
                        if (file.exists()) {
                            conf.loadFromFile(file)
                        }
                    }
                }
                 */
                val configFile = ConfigNodeFile(conf, file)
                conf.onReload {
                    val loader = NeonLibsServiceAPI.getAPI<ConfigNodeVisitor>(ConfigNodeVisitor::class.java.name)
                    configFile.nodes.forEach { loader.visitField(plugin, it, clazz, instance) }
                }
                files[name] = configFile
            }
        }
    }

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.NONE
    }

    companion object {

        val files = HashMap<String, ConfigNodeFile>()
    }
}