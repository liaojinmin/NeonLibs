package me.neon.libs.service

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * NeonLibs
 * me.neon.libs.service
 *
 * @author 老廖
 * @since 2024/8/17 15:10
 */
class ExpiringMap<K, V> {

    private data class ValueWrapper<V>(
        var value: V,
        var future: ScheduledFuture<*>
    )

    private var map = ConcurrentHashMap<K, ValueWrapper<V>>()
    private val scheduler = Executors.newScheduledThreadPool(1)

    // 添加或更新一个键值对，并设置过期时间
    fun put(key: K, value: V, expireTime: Long, timeUnit: TimeUnit) {
        val old = map[key]
        if (old != null) {
            old.future.cancel(false)
            old.value = value
            old.future = scheduler.schedule({
                map.remove(key)
            }, expireTime, timeUnit)
        } else {
            map[key] = ValueWrapper(value, scheduler.schedule({
                map.remove(key)
            }, expireTime, timeUnit))
        }
    }

    // 获取一个键对应的值
    fun get(key: K): V? {
        return map[key]?.value
    }

    // 移除一个键值对
    fun remove(key: K): V? {
        val wrapper = map.remove(key)
        if (wrapper != null) {
            wrapper.future.cancel(false)
            return wrapper.value
        }
        return null
    }

    // 检查是否包含某个键
    fun containsKey(key: K): Boolean {
        return map.containsKey(key)
    }

    // 清除所有键值对
    fun clear() {
        map.values.forEach { it.future.cancel(false) }
        map.clear()
    }

    fun size(): Int {
        return map.size
    }
}