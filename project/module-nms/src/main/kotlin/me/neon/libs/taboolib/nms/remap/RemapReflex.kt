package me.neon.libs.taboolib.nms.remap

import me.neon.libs.io.newFile
import me.neon.libs.taboolib.nms.MinecraftVersion
import org.tabooproject.reflex.ReflexRemapper
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.module.nms.remap.RemapReflex
 *
 * @author 坏黑
 * @since 2024/7/21 17:56
 */
abstract class RemapReflex : ReflexRemapper {

    // 环境信息
    val isUniversal = MinecraftVersion.isUniversal
    val major = MinecraftVersion.major

    // 缓存信息
    val fieldRemapCacheMap = ConcurrentHashMap<String, String>()
    val methodRemapCacheMap = ConcurrentHashMap<String, String>()
    val descriptorTypeCacheMap = ConcurrentHashMap<String, List<Class<*>>>()

    // 映射信息
    val spigotMapping = MinecraftVersion.spigotMapping
    val paperMapping = MinecraftVersion.paperMapping

    /*
    init {
        if (isDevelopmentMode) {
            newFile(getDataFolder(), ".dev/remap.txt").delete()
        }
    }

     */

    fun saveField(namespace: String, old: String, new: String) {
        fieldRemapCacheMap[namespace] = new
        // 开发者模式下保存映射信息
        //if (isDevelopmentMode) {
            //newFile(getDataFolder(), ".dev/remap.txt").appendText("f\t$namespace\t$old\t$new\n")
        //}
    }

    fun saveMethod(namespace: String, old: String, new: String, descriptor: String?) {
        methodRemapCacheMap[namespace] = new
        /*
        // 开发者模式下保存映射信息
        if (isDevelopmentMode) {
            newFile(getDataFolder(), ".dev/remap.txt").appendText("m\t$namespace\t$old\t$new\t$descriptor\n")
        }

         */
    }
}