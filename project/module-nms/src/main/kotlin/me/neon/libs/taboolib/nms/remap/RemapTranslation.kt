package me.neon.libs.taboolib.nms.remap

import me.neon.libs.taboolib.nms.MinecraftVersion
import org.objectweb.asm.commons.Remapper
import org.tabooproject.reflex.util.ClassHelper

/**
 * 对于 TabooLib 内的类，
 * 使用 RemapTranslationTabooLib 进行 Spigot Deobf -> Mojang Obf -> Mojang Deobf 转换。
 *
 * 而插件内的类，已经由 Paper 进行转译了，所以不应该再使用 RemapTranslation (现在为 RemapTranslationPlugin) 进行转译，
 * 应该只需要使用该 RemapTranslation 移除包名中的跨版本信息 (诸如 v1_20_R3) ，
 * 而无需对字段名、方法名进行任何操作。
 *
 * 只有 Paper 1.20.5+ 才会启用该类用于转译插件本体里的类，一般使用其子类 RemapTranslationPlugin。
 * 与 RemapTranslationTabooLib 不同的是，此实现不会对函数名和字段名进行检索转译，避免 Paper 对插件本体的转译失效。
 *
 * @author mical
 * @since 2024/8/17 22:44
 */
open class RemapTranslation : Remapper() {

    /**
     * 只匹配 1.17 之下的 nms 包名
     */
    val nms1 = "net/minecraft/server/v1_.*?/".toRegex()
    val nms2 = "net/minecraft/server/${MinecraftVersion.minecraftVersion}/"

    val obc1 = "org/bukkit/craftbukkit/v1_.*?/".toRegex()
    val obc2 = "org/bukkit/craftbukkit/${MinecraftVersion.minecraftVersion}/"
    val obc3 = "org/bukkit/craftbukkit/"

    override fun mapType(internalName: String): String {
        return super.mapType(translate(internalName))
    }

    override fun map(internalName: String): String {
        return translate(internalName)
    }

    /**
     * 包名转换方法
     */
    open fun translate(key: String): String {
        // obc
        if (key.startsWith("org/bukkit/craftbukkit")) {
            // 若当前使用 Universal CraftBukkit 环境，则移除版本号
            return key.replace(obc1, if (MinecraftVersion.isUniversalCraftBukkit) obc3 else obc2)
        }
        // 统一版本
        return if (MinecraftVersion.isUniversal) {
            // 将低版本包名替换为高版本包名
            // net/minecraft/server/v1_17_R1/EntityPlayer -> net/minecraft/server/level/EntityPlayer
            if (key.startsWith("net/minecraft/server/v1_")) {
                // 先转为 Spigot.FullName
                var spigotName = MinecraftVersion.spigotMapping.classMapSpigotS2F[key.substringAfterLast('/')] ?: return key
                // 如果为 Universal CraftBukkit 环境, 则应进一步转译为 Mojang.FullName
                spigotName = if (MinecraftVersion.isUniversalCraftBukkit) {
                    MinecraftVersion.paperMapping.classMapSpigotToMojang[spigotName] ?: spigotName
                } else spigotName
                spigotName.replace('.', '/')
            } else {
                key
            }
        } else {
            // 将高版本包名替换为低版本包名
            // net/minecraft/server/level/EntityPlayer -> net/minecraft/server/v1_17_R1/EntityPlayer
            val clazz = key.substringAfterLast('/', "")
            if (clazz.isEmpty() || clazz.isBlank()) {
                key.replace(nms1, nms2)
            } else {
                if (key.startsWith("net/minecraft")) {
                    (MinecraftVersion.paperMapping.classMapSpigotS2F[clazz]
                        ?: MinecraftVersion.spigotMapping.classMapSpigotS2F[clazz])?.let {
                       // println("")
                       // println("截取后类名: $clazz")
                       // println("旧的包名: $key")
                       // println("取得新包名: $it")
                       //println( "重置: ${"net/minecraft/server/${MinecraftVersion.minecraftVersion}/${key.substringAfterLast( '/', "" )}"}")
                        //println("")
                        "net/minecraft/server/${MinecraftVersion.minecraftVersion}/${key.substringAfterLast('/', "")}"
                    } ?: key.replace(nms1, nms2)
                } else key
            }
        }
    }

    /**
     * 获取类的所有父类和接口
     * 因为映射信息是以实际所在的类为准，如果不向上追溯，那么调用子类的方法时会找不到映射信息
     */
    fun findParents(owner: String): Set<String> {
        if (owner.startsWith("net.minecraft") || owner.startsWith("com.mojang")) {
            try {
                val find = hashSetOf<String>()
                find += owner
                val forName = ClassHelper.getClass(owner)
                find += forName.interfaces.map { it.name }
                val superclass = forName.superclass
                if (superclass != null && superclass.name != "java.lang.Object") {
                    find += findParents(superclass.name)
                }
                return find
            } catch (_: Throwable) {
            }
        }
        return hashSetOf(owner)
    }
}