package me.neon.libs.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.neon.libs.taboolib.core.PrimitiveIO
import me.neon.libs.utils.io.digest
import me.neon.libs.utils.io.newFile
import org.bukkit.entity.Player
import org.tabooproject.reflex.Reflex.Companion.getProperty
import java.io.File
import java.net.URL
import java.util.Properties

/**
 * 获取玩家对应的语言文件
 */
fun Player.getLocaleFile(): LocaleFile? {
    val locale = try {
        locale
    } catch (_: NoSuchMethodError) {
        getProperty("handle/locale")!!
    }
    return LocaleI18n.getLocaleFile(locale) ?: LocaleI18n.getDefaultLocaleFile()
}

/**
 * 语言文件
 */
interface LocaleFile {

    /** 获取文件 */
    fun sourceFile(): File

    /** 获取语言文件值 */
    operator fun get(path: String): String?

}

/**
 * TabooLib
 * taboolib.taboolib.nms.LocaleI18n
 *
 * @author 坏黑
 * @since 2023/8/6 02:44
 */
object LocaleI18n {

    /** 资源文件地址 */
    private var resourceUrl = "https://resources.download.minecraft.net"

    /** 支持的语言文件 */
    private val supportedLanguage = arrayListOf("zh_cn", "zh_tw", "en_gb")

    /** 语言文件 */
    private val localeFiles = hashMapOf<String, LocaleFile>()

    /**
     * 获取语言文件
     */
    fun getLocaleFile(locale: String): LocaleFile? {
        return localeFiles[locale]
    }

    /**
     * 获取默认语言文件
     */
    fun getDefaultLocaleFile(): LocaleFile? {
        return localeFiles["zh_cn"]
    }

    internal fun init() {
        try {
            if (!checkLocaleFile()) {
                info("正在下载 language 目录 ...")
                downloadLocaleFile()
            }
            loadLocaleFile()
        } catch (_: Exception) {
            warning("无法下载 language 文件")
        }
    }

    /** 检查语言文件 */
    private fun checkLocaleFile(): Boolean {
        return getFiles().size == supportedLanguage.size
    }

    /** 下载语言文件 */
    private fun downloadLocaleFile() {
        val manifest = readJson("https://launchermeta.mojang.com/mc/game/version_manifest.json")
        for (ver in manifest.getAsJsonArray("versions")) {
            if (ver.asJsonObject["id"].asString == me.neon.libs.MinecraftVersion.runningVersion) {
                // 获取版本信息
                val versionObject = readJson(ver.asJsonObject["url"].asString)
                // 获取资源信息
                val assetIndexObject = readJson(versionObject["assetIndex"].asJsonObject["url"].asString)
                // 下载语言文件
                val objects = assetIndexObject["objects"].asJsonObject
                supportedLanguage.forEach { language ->
                    // 不同版本语言文件路径不同，可能存在的形式：
                    // - minecraft/lang/zh_CN.lang
                    // - minecraft/lang/zh_cn.lang << 截止到 1.12 版本，为 properties 格式
                    // - minecraft/lang/zh_cn.json
                    val names = arrayOf(
                        "minecraft/lang/${language.substringBefore('_')}_${language.substringAfter('_').uppercase()}.lang",
                        "minecraft/lang/$language.lang",
                        "minecraft/lang/$language.json",
                    )
                    for (name in names) {
                        if (objects[name] != null) {
                            val langHash = objects[name].asJsonObject["hash"].asString
                            // 检查文件是否有效
                            val file = getFile(language)

                            if (file.exists() && file.length() > 0) {
                                break
                            }
                            // 获取语言文件文本并写入本地文件
                            newFile(file).writeText(URL("$resourceUrl/${langHash.substring(0, 2)}/$langHash").readText())
                            break
                        }
                    }
                }
                return
            }
        }
        warning("No language file found.")
    }

    /** 加载语言文件 */
    private fun loadLocaleFile() {
        localeFiles += getFiles().mapValues { (_, v) -> if (me.neon.libs.MinecraftVersion.isHigher(me.neon.libs.MinecraftVersion.V1_12)) LocaleJson(v) else LocaleProperties(v) }
    }

    private fun readJson(url: String): JsonObject {
        return JsonParser().parse(URL(url).readText()).asJsonObject
    }

    private fun getFile(locale: String): File {
        val hash = (me.neon.libs.MinecraftVersion.runningVersion + locale.lowercase()).digest("sha-1")
        return File(PrimitiveIO.getAssetsFile(),"${hash.substring(0, 2)}/$hash")
    }

    private fun getFiles(): Map<String, File> {
        val map = HashMap<String, File>()
        supportedLanguage.forEach {
            val file = getFile(it)
            if (file.exists() && file.length() > 0) {
                map[it] = file
            }
        }
        return map
    }

    /**
     * 1.8 .. 1.12 版本语言文件格式
     */
    class LocaleProperties(val file: File) : LocaleFile {

        val lang = Properties().apply { load(file.reader()) }

        override fun sourceFile(): File {
            return file
        }

        override operator fun get(path: String): String? {
            return lang.getProperty(path)
        }

    }

    /**
     * 1.13 至今语言文件格式
     */
    class LocaleJson(val file: File) : LocaleFile {

        val lang: JsonObject = JsonParser().parse(file.readText()).asJsonObject

        override fun sourceFile(): File {
            return file
        }

        override operator fun get(path: String): String? {
            return lang[path]?.asString
        }
    }
}