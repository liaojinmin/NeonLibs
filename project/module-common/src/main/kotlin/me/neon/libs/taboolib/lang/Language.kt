package me.neon.libs.taboolib.lang

import me.neon.libs.taboolib.lang.type.*
import me.neon.libs.utils.io.forFile
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.*
import kotlin.collections.HashMap

/**
 * TabooLib
 * me.neon.mail.libs.lang.Language
 *
 * @author sky
 * @since 2021/6/18 10:43 下午
 */
object Language {


    /** 语言文件路径 */
    internal var path = "lang"

    /** 默认语言文件 */
    internal var default = "zh_CN"

    /** 语言文件缓存 */
    internal val languageFile = HashMap<Plugin, HashMap<String, LanguageFile>>()

    /** 语言文件代码 */
    private val languageCode = HashSet<String>()

    /** 语言文件类型 */
    @Suppress("SpellCheckingInspection")
    internal val languageType = hashMapOf(
        "text" to TypeText::class.java,
        "raw" to TypeJson::class.java,
        "json" to TypeJson::class.java,
        "title" to TypeTitle::class.java,
        "sound" to TypeSound::class.java,
        "command" to TypeCommand::class.java,
        "actionbar" to TypeActionBar::class.java,
        "action_bar" to TypeActionBar::class.java,
    )

    /** 语言文件代码转换 */
    private val languageCodeTransfer = hashMapOf(
        "zh_hans_cn" to "zh_CN",
        "zh_hant_cn" to "zh_TW",
        "en_ca" to "en_US",
        "en_au" to "en_US",
        "en_gb" to "en_US",
        "en_nz" to "en_US"
    )

    fun onDisable(plugin: Plugin) {
        languageFile.remove(plugin)
    }

    fun onLoader(plugin: Plugin, pluginFile: File) {
        // 加载语言文件类型
        val file = File(plugin.dataFolder, path)
        file.forFile("yml").forEach {
            languageCode += it.name.substringBeforeLast('.')
        }
        languageFile[plugin] = ResourceReader(plugin, pluginFile).files
    }


    /** 获取玩家语言 */
    fun getLocale(player: Player): String {
        //player.getMetadata()
        return languageCodeTransfer[player.locale.lowercase()] ?: player.locale
    }

    /** 获取默认语言 */
    fun getLocale(): String {
        return Locale.getDefault().toLanguageTag().replace("-", "_").lowercase()
    }
}