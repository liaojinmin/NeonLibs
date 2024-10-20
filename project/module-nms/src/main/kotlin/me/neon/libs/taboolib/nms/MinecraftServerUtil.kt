package me.neon.libs.taboolib.nms

import me.neon.libs.NeonLibsLoader
import me.neon.libs.core.runningClassMapWithoutLibrary
import me.neon.libs.core.runningTabooLibClassMap
import me.neon.libs.event.SubscribeEvent
import me.neon.libs.util.unsafeLazy
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.util.ClassHelper
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private val nmsProxyClassMap = ConcurrentHashMap<Plugin, ConcurrentHashMap<String, Class<*>>>()

private val nmsProxyInstanceMap = ConcurrentHashMap<Plugin, ConcurrentHashMap<String, Any>>()

private val packetPool = ConcurrentHashMap<String, ExecutorService>()

/**
 * 获取 MinecraftServer 实例
 */
val minecraftServerObject: Any by unsafeLazy {
    Bukkit.getServer().getProperty("console")!!
}

/**
 * 获取 OBC 类
 */
fun obcClass(name: String): Class<*> {
    return if (MinecraftVersion.isUniversalCraftBukkit) {
        ClassHelper.getClass("org.bukkit.craftbukkit.$name")
    } else {
        ClassHelper.getClass("org.bukkit.craftbukkit.${MinecraftVersion.minecraftVersion}.$name")
    }
}

/**
 * 获取 NMS 类
 */
fun nmsClass(name: String): Class<*> {
    return if (MinecraftVersion.isUniversal) {
        ClassHelper.getClass(MinecraftVersion.spigotMapping.classMapSpigotS2F[name]?.replace('/', '.') ?: throw ClassNotFoundException(name))
    } else {
        ClassHelper.getClass("net.minecraft.server.${MinecraftVersion.minecraftVersion}.$name")
    }
}

inline fun <reified T> nmsProxy(plugin: Plugin, bind: String = "{name}Impl", vararg parameter: Any): T {
    return nmsProxy(T::class.java, plugin, bind, emptyList(), *parameter)
}

@Suppress("UNCHECKED_CAST")
@Synchronized
fun <T> nmsProxy(clazz: Class<T>, plugin: Plugin, bind: String = "{name}Impl", parent: List<String> = emptyList(), vararg parameter: Any): T {
    val key = "${clazz.name}:$bind:${parameter.joinToString(",") { it.javaClass.name.toString() }}"
    // 从缓存中获取
    val cache = nmsProxyInstanceMap.computeIfAbsent(plugin) { ConcurrentHashMap() }
    if (cache.containsKey(key)) {
        return cache[key] as T
    }
    // 获取合适的构造函数并创建实例
    fun <T> createInstance(clazz: Class<T>, parameters: Array<out Any>): T {
        // 遍历所有构造函数
        val constructors = clazz.declaredConstructors
        for (constructor in constructors) {
            val parameterTypes = constructor.parameterTypes
            if (parameterTypes.size != parameters.size) continue
            var isMatch = true
            for (i in parameterTypes.indices) {
                if (!parameterTypes[i].isAssignableFrom(parameters[i].javaClass)) {
                    isMatch = false
                    break
                }
            }
            if (isMatch) {
                constructor.isAccessible = true
                return constructor.newInstance(*parameters) as T
            }
        }
        throw NoSuchMethodException("没有找到匹配的构造函数: ${clazz.name}")
    }

    // 获取代理类并实例化
    val newInstance = createInstance(nmsProxyClass(clazz, plugin, bind, parent), parameter)
    //val newInstance = nmsProxyClass(clazz, plugin, bind).getDeclaredConstructor(*parameter.map { it.javaClass }.toTypedArray()).newInstance(*parameter)
    // 缓存实例
    cache[key] = newInstance!!
    return newInstance
}

@Synchronized
fun <T> nmsProxyClass(clazz: Class<T>, plugin: Plugin, bind: String = "{name}Impl", parent: List<String> = emptyList()): Class<T> {
    parent.forEach { nmsProxyClass(clazz, plugin, it) }
    return nmsProxyClass(clazz, plugin, bind)
}

@Suppress("UNCHECKED_CAST")
@Synchronized
fun <T> nmsProxyClass(clazz: Class<T>, plugin: Plugin, bind: String = "{name}Impl"): Class<T> {
    val key = "${clazz.name}:$bind"
    // 从缓存中获取
    val cache = nmsProxyClassMap.computeIfAbsent(plugin) { ConcurrentHashMap() }
    if (cache.containsKey(key)) {
        return cache[key] as Class<T>
    }
    // 生成代理类
    val bindClass = bind.replace("{name}", clazz.name)
    val newClass = AsmClassTransfer(plugin, bindClass).createNewClass()
    // 同时生成所有的内部类
    if (plugin is NeonLibsLoader) {
        runningClassMapWithoutLibrary.filter { (name, _) ->
            name.startsWith("$bindClass\$")
        }.forEach { (name, _) -> AsmClassTransfer(plugin, name).createNewClass() }
    } else {
        NeonLibsLoader.getLifeCycleLoader(plugin)
            .runningClassMapWithoutLibrary.filter { (name, _) ->
                name.startsWith("$bindClass\$")
            }.forEach { (name, _) -> AsmClassTransfer(plugin, name).createNewClass() }
    }
    // 缓存代理类
    cache[key] = newClass
    return newClass as Class<T>
}


/**
 * 向玩家发送打包数据包（异步，1.19.4+）
 */
fun Player.sendBundlePacket(vararg packet: Any): CompletableFuture<Void> {
    return sendBundlePacket(packet.toList())
}

/**
 * 向玩家发送打包数据包（异步，1.19.4+）
 */
fun Player.sendBundlePacket(packet: List<Any>): CompletableFuture<Void> {
    if (MinecraftVersion.isBundlePacketSupported) {
        val bundlePacket = ProtocolHandler.createBundlePacket(packet)
        if (bundlePacket != null) {
            return sendAsyncPacket(bundlePacket)
        }
    }
    return CompletableFuture.allOf(*packet.map { sendAsyncPacket(it) }.toTypedArray())
}

/**
 * 向玩家发送数据包（异步）
 */
fun Player.sendAsyncPacket(packet: Any): CompletableFuture<Void> {
    val future = CompletableFuture<Void>()
    val pool = packetPool.computeIfAbsent(name) { Executors.newSingleThreadExecutor() }
    pool.submit {
        try {
            sendPacketBlocking(packet)
            future.complete(null)
        } catch (e: Throwable) {
            future.completeExceptionally(e)
            e.printStackTrace()
        }
    }
    return future
}

/**
 * 向玩家发送打包数据包（1.19.4+）
 */
fun Player.sendBundlePacketBlocking(vararg packet: Any) {
    sendBundlePacketBlocking(packet.toList())
}

/**
 * 向玩家发送打包数据包（1.19.4+）
 */
fun Player.sendBundlePacketBlocking(packet: List<Any>) {
    if (MinecraftVersion.isBundlePacketSupported) {
        val bundlePacket = ProtocolHandler.createBundlePacket(packet)
        if (bundlePacket != null) {
            sendPacketBlocking(bundlePacket)
            return
        }
    }
    packet.forEach { sendPacketBlocking(it) }
}

/**
 * 向玩家发送数据包
 */
fun Player.sendPacketBlocking(packet: Any) {
    ProtocolHandler.sendPacket(this, packet)
}

/**
 * 监听器
 */
private object PoolListener {

    @SubscribeEvent
    private fun onQuit(e: PlayerQuitEvent) {
        packetPool.remove(e.player.name)?.shutdownNow()
    }
}