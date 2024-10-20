package me.neon.libs.taboolib.lang

import me.neon.libs.taboolib.lang.type.TypeText
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

fun CommandSender.sendLang(plugin: Plugin, node: String, vararg args: Any) {
    val file = getLocaleFile(plugin)
    if (file == null) {
        sendMessage("{$node}")
    } else {
        val type = file.nodes[node]
        if (type != null) {
            type.send(this, *args)
        } else {
            sendMessage("{$node}")
        }
    }
}

fun CommandSender.asLangText(plugin: Plugin, node: String, vararg args: Any): String {
    return asLangTextOrNull(plugin, node, *args) ?: "{$node}"
}

fun CommandSender.asLangTextOrNull(plugin: Plugin, node: String, vararg args: Any): String? {
    val file = getLocaleFile(plugin)
    if (file != null) {
        return (file.nodes[node] as? TypeText)?.asText(this, *args)
    }
    return null
}

fun CommandSender.getLocales(): String {
    return if (this is Player) Language.getLocale(this) else Language.getLocale()
}

fun CommandSender.getLocaleFile(plugin: Plugin): LanguageFile? {
    val map = Language.languageFile[plugin] ?: return null
    return if (this is Player) {
        val locale = getLocales()
        map.entries.firstOrNull { it.key.equals(locale, true) }?.value
            ?: map[Language.default]
            ?: map.values.firstOrNull()
    } else {
        map[Language.default]
    }
}
