package me.neon.libs.taboolib.nms

import java.util.concurrent.ConcurrentHashMap

/**
 * 借助 BukkitServicesManager 注册一个全局的 ConcurrentHashMap 用于存储数据
 * 用于解决不同插件之间的数据交换问题，例如 A 加载的映射文件，B 插件无需重复加载
 */
@Suppress("UNCHECKED_CAST")
object Exchanges {

    // Minecraft 语言文件缓存
    const val MINECRAFT_LANGUAGE = "minecraft_language"

    // Spigot 映射表
    const val MAPPING_SPIGOT = "mapping_spigot"

    // Paper 映射表
    const val MAPPING_PAPER = "mapping_paper"

    private val map: MutableMap<String, Any> = ConcurrentHashMap()

    /**
     * 是否存在数据
     */
    operator fun contains(key: String): Boolean {
        return map.containsKey(key)
    }

    /**
     * 读取数据
     */
    operator fun <T> get(key: String): T {
        return map[key] as T
    }

    /**
     * 读取数据，如果不存在则写入默认值
     */
    fun <T> getOrPut(key: String, defaultValue: () -> T): T {
        return map.getOrPut(key) { defaultValue()!! } as T
    }

    /**
     * 写入数据
     */
    operator fun set(key: String, value: Any?) {
        if (value != null) {
            map[key] = value
        } else {
            map.remove(key)
        }
    }

    /**
     * 获取容器
     */
    operator fun invoke(): MutableMap<String, Any> {
        return map
    }
}