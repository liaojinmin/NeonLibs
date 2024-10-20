package me.neon.libs.taboolib.nms


import org.bukkit.plugin.Plugin

class AsmClassLoader(val plugin: Plugin) : ClassLoader(plugin::class.java.classLoader) {

    override fun findClass(name: String?): Class<*> {
        try {
            return Class.forName(name, false, AsmClassLoader::class.java.classLoader)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return super.findClass(name)
    }

    fun createNewClass(name: String, arr: ByteArray): Class<*> {
        return defineClass(name.replace('/', '.'), arr, 0, arr.size, AsmClassLoader::class.java.protectionDomain)
    }
}