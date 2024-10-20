package me.neon.libs.taboolib.nms

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.neon.libs.NeonLibsLoader
import me.neon.libs.io.digest
import me.neon.libs.io.newFile
import me.neon.libs.core.PrimitiveIO
import org.bukkit.entity.Player
import org.tabooproject.reflex.Reflex.Companion.getProperty
import java.io.File
import java.net.URL
import java.util.*

/**
 * 获取玩家对应的语言文件
 */
fun Player.getLocaleFile(): LocaleFile? {
    val locale = try {
        locale
    } catch (_: NoSuchMethodError) {
        this.getProperty("handle/locale")!!
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

    /** 获取语言文件值 */
    operator fun get(localeKey: LocaleKey) = if (localeKey.extra != null) get(localeKey.path) + " " + get(localeKey.extra) else get(localeKey.path)
}

/**
 * 语言文件节点
 *
 * @param type 类型（N=正常，S=特殊处理，D=缺省）
 * @param path 节点
 * @param extra 额外信息（在低版本中表现为生成蛋的类型）
 */
data class LocaleKey(val type: String, val path: String, val extra: String? = null) {

    override fun toString(): String {
        return "[$type] " + if (extra == null) path else "$path ($extra)"
    }
}

/**
 * TabooLib
 * taboolib.module.nms.LocaleI18n
 *
 * @author 坏黑
 * @since 2023/8/6 02:44
 */
object LocaleI18n {

    /** 资源文件地址 */
    var resourceUrl = "https://resources.download.minecraft.net"

    /** 支持的语言文件 */
    val supportedLanguage = arrayListOf("zh_cn", "zh_tw", "en_gb")

    /** 语言文件 */
    private val localeFiles = hashMapOf<String, LocaleFile>()

    private var isInitializer: Boolean = false

    /**
     * 获取语言文件
     */
    fun getLocaleFile(locale: String): LocaleFile? {
        if (!isInitializer) {
            init()
            isInitializer = true
        }
        return localeFiles[locale]
    }

    /**
     * 获取默认语言文件
     */
    fun getDefaultLocaleFile(): LocaleFile? {
        if (!isInitializer) {
            init()
            isInitializer = true
        }
        return localeFiles["zh_cn"]
    }

    private fun init() {
        try {
            if (!checkLocaleFile()) {
                NeonLibsLoader.info("正在下载 language 目录 ...")
                downloadLocaleFile()
            }
            loadLocaleFile()
        } catch (_: Exception) {
            NeonLibsLoader.warning("无法下载 language 文件")
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
            if (ver.asJsonObject["id"].asString == MinecraftVersion.runningVersion) {
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
                            NeonLibsLoader.info("正在下载 language ... $language")
                            // 获取语言文件文本并写入本地文件
                            newFile(file).writeText(URL("$resourceUrl/${langHash.substring(0, 2)}/$langHash").readText())
                            break
                        }
                    }
                }
                return
            }
        }
        NeonLibsLoader.warning("No language file found.")
    }

    /** 加载语言文件 */
    private fun loadLocaleFile() {
        localeFiles += getFiles().mapValues { (_, v) -> if (MinecraftVersion.isHigher(MinecraftVersion.V1_12)) LocaleJson(v) else LocaleProperties(v) }
    }

    private fun readJson(url: String): JsonObject {
        return JsonParser().parse(URL(url).readText()).asJsonObject
    }

    private fun getFile(locale: String): File {
        val hash = (MinecraftVersion.runningVersion + locale.lowercase()).digest("sha-1")
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