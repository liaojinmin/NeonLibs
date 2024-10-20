package me.neon.libs.taboolib.nms

import me.neon.libs.taboolib.nms.remap.RemapTranslation
import me.neon.libs.taboolib.nms.remap.RemapTranslationLegacy
import org.bukkit.plugin.Plugin
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.module.nms.ClassTransfer
 *
 * @author sky
 * @since 2021/6/18 1:49 上午
 */
class AsmClassTransfer(val plugin: Plugin, val source: String) {

    @Synchronized
    fun createNewClass(): Class<*> {
        var inputStream = AsmClassTransfer::class.java.classLoader.getResourceAsStream(source.replace('.', '/') + ".class")
        if (inputStream == null) {
            inputStream = plugin::class.java.classLoader.getResourceAsStream(source.replace('.', '/') + ".class")
        }
        if (inputStream == null) {
            error("Cannot find class: $source")
        }
        val classReader = ClassReader(inputStream)
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
        val remapper = if (MinecraftVersion.isUniversalCraftBukkit) { RemapTranslation() } else RemapTranslationLegacy()
        classReader.accept(ClassRemapper(classWriter, remapper), 0)
        return mapCache.computeIfAbsent(plugin) { AsmClassLoader(plugin) }.createNewClass(source, classWriter.toByteArray())
      //  return AsmClassLoader.createNewClass(source, plugin, classWriter.toByteArray())
    }

    companion object {

        internal val mapCache: ConcurrentHashMap<Plugin, AsmClassLoader> by lazy { ConcurrentHashMap() }

    }
}