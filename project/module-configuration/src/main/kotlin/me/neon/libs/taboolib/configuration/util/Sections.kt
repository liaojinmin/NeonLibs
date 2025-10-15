package me.neon.libs.taboolib.configuration.util

import me.neon.libs.taboolib.configuration.ConfigurationSection
import me.neon.libs.taboolib.configuration.Configuration

/**
 * 将任意 [Map] 或 [Configuration] 转换为 [Map<String, Any?>]
 */
fun Any?.asMap(): Map<String, Any?> {
    return if (this != null) Configuration.parse(this).getValues(false) else emptyMap()
}

fun <V> ConfigurationSection.map(transform: (String, Any) -> V): Map<String, V> {
    return getKeys(false).associateWith { transform(it, get(it) ?: error("$it is null")) }
}

fun <V> ConfigurationSection.map(node: String, transform: (String, Any) -> V): Map<String, V> {
    return getConfigurationSection(node)?.getKeys(false)?.associateWith { transform(it, get("$node.$it") ?: error("$it is null")) } ?: emptyMap()
}

fun <V> ConfigurationSection.mapValue(transform: (Any) -> V): Map<String, V> {
    return getKeys(false).associateWith { transform(get(it) ?: error("$it is null")) }
}

fun <V> ConfigurationSection.mapValue(node: String, transform: (Any) -> V): Map<String, V> {
    return getConfigurationSection(node)?.getKeys(false)?.associateWith { transform(get("$node.$it") ?: error("$it is null")) } ?: emptyMap()
}

fun <V> ConfigurationSection.mapSection(transform: (ConfigurationSection) -> V): Map<String, V> {
    return getKeys(false).associateWith { transform(getConfigurationSection(it) ?: error("$it is null")) }
}

fun <V> ConfigurationSection.mapSection(node: String, transform: (ConfigurationSection) -> V): Map<String, V> {
    return getConfigurationSection(node)?.getKeys(false)?.associateWith { transform(getConfigurationSection("$node.$it") ?: error("$it is null")) } ?: emptyMap()
}

fun <T> ConfigurationSection.mapListAs(path: String, transform: (Map<String, Any?>) -> T): MutableList<T> {
    return getMapList(path).map { transform(it.asMap()) }.toMutableList()
}

/**
 * 将左侧配置文件合并进入右侧配置文件，可以选择是否覆盖相同的节点。
 */
fun ConfigurationSection.mergeTo(section: ConfigurationSection, overwrite: Boolean = true): ConfigurationSection {
    getKeys(false).forEach { key ->
        // 如果当前节点是配置节，则递归合并
        if (isConfigurationSection(key)) {
            val value = getConfigurationSection(key)!!
            val targetSection = section.getConfigurationSection(key) ?: section.createSection(key)
            value.mergeTo(targetSection, overwrite)
        } else {
            // 否则直接覆盖
            if (overwrite || !section.contains(key)) {
                section[key] = get(key)
            }
        }
    }
    return section
}