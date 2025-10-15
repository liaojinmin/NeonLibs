package me.neon.libs

import java.util.concurrent.ConcurrentHashMap

/**
 * NeonLibs
 * me.neon.libs
 *
 * @author 老廖
 * @since 2025/9/17 13:01
 */
object NeonLibsServiceAPI {

    /** 已被唤醒的类 */
    val awokenMap: ConcurrentHashMap<String, Any> = ConcurrentHashMap()

    /**
     * 获取已被唤醒的 API 实例
     */
    fun <T> getAPI(name: String) = (awokenMap[name] ?: error("API ($name) not found, currently: ${awokenMap.keys}")) as T

    /**
     * 获取已注册的跨平台服务
     */
    fun <T> getService(name: String) = (awokenMap[name] ?: error("Service ($name) not found, currently: ${awokenMap.keys}")) as T

    /**
     * 获取已被唤醒的 API 实例
     */
    inline fun <reified T> getAPI(): T = getAPI(T::class.java.name)

    /**
     * 获取已被唤醒的 API 实例（可能为空）
     */
    inline fun <reified T> getAPIOrNull() = awokenMap[T::class.java.name] as? T

    /**
     * 获取已注册的跨平台服务
     */
    inline fun <reified T> getService(): T = getService(T::class.java.name)

    /**
     * 获取已注册的跨平台服务（可能为空）
     */
    inline fun <reified T> getServiceOrNull() = awokenMap[T::class.java.name] as? T

    /**
     * 注册 API 实例
     */
    inline fun <reified T : Any> registerAPI(instance: T) {
        awokenMap[T::class.java.name] = instance
    }

    /**
     * 注册跨平台服务
     */
    inline fun <reified T : Any> registerService(instance: T) {
        awokenMap[T::class.java.name] = instance
    }
}